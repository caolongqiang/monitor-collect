package com.jimu.monitor.collect;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by yue.liu on 16/5/24.
 */
@Service("influxdbStoreImpl")
public class InfluxdbStoreImpl implements Store {

    @Resource
    private Configs configs;

    private static InfluxDB influxDB;

    @PostConstruct
    private void initInfluxdb() {
        influxDB = InfluxDBFactory.connect(configs.getMonitorDBUrl(), configs.getMonitorDBUsername(), configs.getMonitorDBPassword());
    }

    @Override
    public void write(Group group) {
        BatchPoints batchPoints = BatchPoints
                .database(configs.getMonitorDBName())
                .retentionPolicy("default")
                .consistency(InfluxDB.ConsistencyLevel.ALL)
                .build();
        List<Domain> domainList = group.getDomainList();
        domainList.forEach(domain -> buildPoint(group.getName(), domain, batchPoints));
        influxDB.write(batchPoints);
    }

    private void buildPoint(String measurement, Domain domain, BatchPoints batchPoints) {
        Map<String, Double> measurements = domain.getMeasurements();
        measurements.forEach((key, name) -> {
            Point point = Point.measurement(measurement)
                    .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                    .addField(key, name)
                    .build();
            batchPoints.point(point);
        });


    }
}
