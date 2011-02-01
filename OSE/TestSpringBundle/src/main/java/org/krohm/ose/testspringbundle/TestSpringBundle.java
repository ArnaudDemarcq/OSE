/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.krohm.ose.testspringbundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author arnaud
 */
public class TestSpringBundle implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(TestSpringBundle.class);
    // default sleep time
    private long sleepTime = 10000;

    public long getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
    }

    public void init() {
        logger.info("We are in the TestSpringBundle init : Spring has done its job");
        Thread thread = new Thread(this);
        thread.start();
    }

    public void run() {
        try {
            while (true) {
                logger.info("The test Bundle is still running !");

                Thread.sleep(sleepTime);
            }
        } catch (InterruptedException ex) {
            logger.info("Ein grosse error");
        }
        logger.info("The test Bundle is now almost stoped");
    }
}
