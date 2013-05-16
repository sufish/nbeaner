package com.sufish.nbeaner.pool;

public class BeanstalkConnectionException extends BeanstalkException {
    public BeanstalkConnectionException(Throwable cause) {
        super(cause);
    }

    public BeanstalkConnectionException(String message) {
        super(message);
    }
}
