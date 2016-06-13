package com.jimu.monitor.exception;

/**
 * Created by zhenbao.zhou on 16/5/25.
 */
public class MonitorException extends RuntimeException {
    public MonitorException() {
    }

    public MonitorException(String message) {
        super(message);
    }

    public MonitorException(String message, Exception e) {
        super(message, e);
    }

    public MonitorException(MonitorException e) {
        super(e);
    }
}
