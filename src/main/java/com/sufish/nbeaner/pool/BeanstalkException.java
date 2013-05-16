package com.sufish.nbeaner.pool;

public class BeanstalkException extends Exception {
    public BeanstalkException(String message) {
        super(message);
    }

    public BeanstalkException(Throwable cause) {
        super(cause);
    }

    public BeanstalkException() {

    }
}
