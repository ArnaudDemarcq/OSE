package org.krohm.ose.launcher;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.log4j.xml.DOMConfigurator;

public class Launcher implements BundleActivator, Runnable {

    private final static String rootKey = "org.krohm";
    private final static String homeKey = rootKey + ".home";
    private final static String systemKey = rootKey + ".system.root";
    private final static String log4jKey = rootKey + ".config.log4j";
    private final static String launcherKey = rootKey + ".config.laucher";
    private Logger logger = null;
    private final static String home = System.getProperty(homeKey, ".");
    private final static String systemRoot = System.getProperty(systemKey, home);
    private final static String configLogPath = System.getProperty(log4jKey, home + "/config/log4j.xml");
    private final static String configLauncherPath = System.getProperty(launcherKey, home + "/config/launcher.xml");
    private final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    private DocumentBuilder builder = null;
    private Thread launcherThread = null;
    BundleContext bundleContext = null;
    Map<String, BundleInfo> bundleLoaded = new HashMap<String, BundleInfo>();
    FilenameFilter filter = new FilenameFilter() {

        public boolean accept(File dir, String name) {
            return name.endsWith("war") || name.endsWith("jar");
        }
    };

    private class BundleInfo {

        public Bundle bundle = null;
        public boolean started = false;
        public long lastModified = -1;
        public Object tick = null;
    }

    private Element getElement(Node elem) {
        if (elem.getNodeType() == Node.ELEMENT_NODE) {
            return (Element) elem;
        }

        Node node = elem.getNextSibling();
        while (node != null && node.getNodeType() != Node.ELEMENT_NODE) {
            node = node.getNextSibling();
        }
        return (Element) node;
    }

    public void start(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        System.out.println("Starting bundle launcher");
        launcherThread = new Thread(this);
        launcherThread.start();
    }

    public void run() {
        // WORKS ONLY WITH DTD
        //factory.setIgnoringComments(true);
        //factory.setIgnoringElementContentWhitespace(true); 

        System.out.println("Setting log4j configuration: " + configLogPath);
        DOMConfigurator.configureAndWatch(configLogPath, 6000);
        logger = LoggerFactory.getLogger(Launcher.class);
        logger.info("Configuration logger [" + configLogPath + "] has been loaded.");

        try {
            builder = factory.newDocumentBuilder();
            File configFile = new File(configLauncherPath);
            Document document = builder.parse(configFile);
            logger.info("Configuration launcher [" + configLauncherPath + "] has been loaded.");

            while (true) {
                Element item = getElement(document.getDocumentElement().getFirstChild());
                while (item != null) {
                    if ("bundles-folder".equals(item.getNodeName())) {
                        String folder = item.getAttribute("folder");
                        launchBundlesFolder(folder, "true".equals(item.getAttribute("start")));
                    }
                    item = getElement(item.getNextSibling());
                }

                Object[] keys = bundleLoaded.keySet().toArray();
                for (Object key : keys) {
                    BundleInfo bundleInfo = bundleLoaded.get(key);
                    try {
                        String filePath = bundleInfo.bundle.getLocation().substring(7);
                        File file = new File(filePath);
                        if (!file.exists()) {
                            bundleLoaded.remove(key);
                            if (bundleInfo.started) {
                                logger.debug("stop " + bundleInfo.bundle.getLocation());
                                bundleInfo.bundle.stop();
                            }
                            logger.debug("uninstall " + bundleInfo.bundle.getLocation());
                            bundleInfo.bundle.uninstall();
                        }
                    } catch (BundleException e) {
                        logger.error("Error on bundle: [" + bundleInfo.bundle.getLocation() + "]", e);
                    }
                }
                Thread.sleep(3000);
            }
        } catch (SAXException e) {
            logger.error("Configuration launcher [" + configLauncherPath + "] cannot be parsed.", e);
        } catch (Exception e) {
            logger.error("Configuration launcher [" + configLauncherPath + "] cannot be read.", e);
        }
    }

    private void launchBundlesFolder(String path, boolean start) {
        File folder = new File(systemRoot + "/" + path);
        if (!folder.isDirectory()) {
            logger.warn("Ignoring [" + systemRoot + "/" + path + "] is not a folder");
            return;
        }

        Object tick = new Object();

        for (File jarBundle : folder.listFiles(filter)) {
            String jarPath = "file:///" + jarBundle.getAbsolutePath();
            try {
                BundleInfo bundleInfo = bundleLoaded.get(jarPath);
                boolean doInstall = true;
                if (bundleInfo != null) {
                    if (bundleInfo.lastModified != jarBundle.lastModified()) {
                        if (bundleInfo.started) {
                            logger.debug("stop " + bundleInfo.bundle.getLocation());
                            bundleInfo.bundle.stop();
                        }
                        logger.debug("uninstall " + bundleInfo.bundle.getLocation());
                        bundleInfo.bundle.uninstall();
                    } else {
                        doInstall = false;
                    }
                }
                if (doInstall) {
                    logger.debug("install " + jarPath);
                    Bundle bundle = bundleContext.installBundle(jarPath);
                    bundleInfo = new BundleInfo();
                    bundleInfo.bundle = bundle;
                    bundleInfo.lastModified = jarBundle.lastModified();
                    if (start) {
                        bundle.start();
                        bundleInfo.started = true;
                    }
                    bundleLoaded.put(jarPath, bundleInfo);
                }
                bundleInfo.tick = tick;
            } catch (BundleException e) {
                logger.error("Error on bundle: [" + jarPath + "]", e);
            }
        }
    }

    public void stop(BundleContext context) {
    }
}
