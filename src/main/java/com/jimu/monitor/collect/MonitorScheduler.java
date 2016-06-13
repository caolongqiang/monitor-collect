package com.jimu.monitor.collect;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by yue.liu on 16/5/22.
 */
@Service("monitorScheduler")
public class MonitorScheduler implements Runnable {

    @Resource
    private MeasureFetcher measureFetcher;

    @Resource
    private Store influxdbStoreImpl;


    @Override
    public void run() {
        System.out.println("run....");
        List<Group> groupList = measureFetcher.fetch();

        groupList.forEach(group -> {
            influxdbStoreImpl.write(group);
        });

    }


}
