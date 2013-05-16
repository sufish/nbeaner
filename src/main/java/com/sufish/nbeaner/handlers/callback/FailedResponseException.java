package com.sufish.nbeaner.handlers.callback;

import com.sufish.nbeaner.pool.BeanstalkException;

public class FailedResponseException extends BeanstalkException {

    private String responseText;

    public FailedResponseException(String responseText) {
        this.responseText = responseText;
    }

    public String getResponseText() {
        return responseText;
    }
}
