package com.sufish.nbeaner.handlers.inbound;

public class BeanstalkUnexpectedResponseException extends BeanStalkException {
    public BeanstalkUnexpectedResponseException() {
    }

    public BeanstalkUnexpectedResponseException(String message) {
        super(message);
    }
}
