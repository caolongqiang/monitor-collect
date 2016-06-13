package com.jimu.monitor.collect;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jimu.monitor.collect.utils.HttpClientHelper;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Created by yue.liu on 16/5/22.
 */
@Service
public class MeasureFetcher {


    private static List<Group> monitorFileGroups;
    @Resource
    private MonitorFileLoader monitorFileLoader;

    @PostConstruct
    public void initMonitorFile() {
        monitorFileGroups = monitorFileLoader.load();
    }


    public List<Group> fetch() {
        List<Group> groupList = Lists.newArrayList(monitorFileGroups);
        groupList.parallelStream().forEach(this::parserGroup);
        return groupList;
    }

    private void parserGroup(Group group) {
        group.getDomainList().forEach(this::fetchDomain);
    }

    private void fetchDomain(Domain domain) {
        String url = domain.getUrl();
        String content = HttpClientHelper.getString(url);
        Map<String, Double> measurements = parserMeasurement(content);
        domain.setMeasurements(measurements);
    }

    private Map<String, Double> parserMeasurement(String content) {
        String[] measures = content.split("\n");
        Map<String, Double> measureMap = Maps.newHashMap();
        for (String measure : measures) {
            String[] item = measure.split("=");
            String key = item[0];
            double value = Double.valueOf(item[1]);
            measureMap.put(key, value);
        }
        return measureMap;
    }

}
