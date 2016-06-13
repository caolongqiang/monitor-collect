package com.jimu.monitor.collect;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

/**
 * Created by yue.liu on 16/5/22.
 * Created by yue.liu on 16/5/22.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
public class MonitorSchedulerTest {

    @Resource
    private MonitorScheduler monitorScheduler;

    @Test
    public void run() {
        while (true){

        }
    }

}