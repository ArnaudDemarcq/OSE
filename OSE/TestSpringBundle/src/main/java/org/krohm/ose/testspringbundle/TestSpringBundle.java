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

    public void init() {
        logger.error("We are in the TestSpringBundle init : Spring has done its job");
        Thread thread = new Thread(this);
        thread.start();
    }

    public void run() {
        try {
            while (true) {
                logger.error("The test Bundle is still running !");

                Thread.sleep(10000);
            }
        } catch (InterruptedException ex) {
            logger.error("Ein grosse error");
        }
        logger.error("The test Bundle is now almost stoped");
    }
}
