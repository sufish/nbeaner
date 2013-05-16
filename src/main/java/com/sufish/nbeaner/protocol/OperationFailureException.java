package com.sufish.nbeaner.protocol;

import com.sufish.nbeaner.pool.BeanstalkException;

public class OperationFailureException extends BeanstalkException {

    private String responseText;

    public OperationFailureException(String responseText) {
        this.responseText = responseText;
    }

    public String getResponseText() {
        return responseText;
    }
}
