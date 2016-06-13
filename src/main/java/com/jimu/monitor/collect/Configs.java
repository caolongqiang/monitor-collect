package com.jimu.monitor.collect;

/**
 * Created by yue.liu on 16/5/22.
 */
public class Configs {

    private String monitorPath;
    private String monitorDBUrl;
    private String monitorDBUsername;
    private String monitorDBPassword;
    private String monitorDBName;

    public String getMonitorDBName() {
        return monitorDBName;
    }

    public void setMonitorDBName(String monitorDBName) {
        this.monitorDBName = monitorDBName;
    }

    public String getMonitorPath() {
        return monitorPath;
    }

    public void setMonitorPath(String monitorPath) {
        this.monitorPath = monitorPath;
    }

    public String getMonitorDBUrl() {
        return monitorDBUrl;
    }

    public void setMonitorDBUrl(String monitorDBUrl) {
        this.monitorDBUrl = monitorDBUrl;
    }

    public String getMonitorDBUsername() {
        return monitorDBUsername;
    }

    public void setMonitorDBUsername(String monitorDBUsername) {
        this.monitorDBUsername = monitorDBUsername;
    }

    public String getMonitorDBPassword() {
        return monitorDBPassword;
    }

    public void setMonitorDBPassword(String monitorDBPassword) {
        this.monitorDBPassword = monitorDBPassword;
    }
}
