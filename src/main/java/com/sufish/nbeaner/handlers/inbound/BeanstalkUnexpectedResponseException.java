package com.sufish.nbeaner.handlers.inbound;

import com.sufish.nbeaner.pool.BeanstalkConnectionException;

public class BeanstalkUnexpectedResponseException extends BeanstalkConnectionException {

    public BeanstalkUnexpectedResponseException(String message) {
        super(message);
    }
}
