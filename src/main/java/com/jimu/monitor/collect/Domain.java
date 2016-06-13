package com.jimu.monitor.collect;

import com.google.common.collect.Maps;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by yue.liu on 16/5/22.
 */
public class Domain implements Serializable {

    private static final long serialVersionUID = 6778251799931369141L;
    private String host;
    private String url;
    private Map<String, Double> measurements = Maps.newHashMap();

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, Double> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(Map<String, Double> measurements) {
        this.measurements = measurements;
    }

    @Override
    public String toString() {
        return "Domain{" +
                "host='" + host + '\'' +
                ", url='" + url + '\'' +
                ", measurements=" + measurements +
                '}';
    }
}
