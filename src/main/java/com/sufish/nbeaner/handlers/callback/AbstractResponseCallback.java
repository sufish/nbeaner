package com.sufish.nbeaner.handlers.callback;

import com.sufish.nbeaner.protocol.BeanstalkResponse;

public abstract class AbstractResponseCallback implements BeanstalkResponseCallback {
    protected String expectedStatusText;

    public AbstractResponseCallback(String expectedStatusText) {
        this.expectedStatusText = expectedStatusText;
    }

    @Override
    public void onSuccess(BeanstalkResponse response) {
        try {
            CallbackUtil.checkResponse(response, expectedStatusText);
            onSuccessResponse(response);
        } catch (Exception e) {
            onException(e);
        }
    }

    @Override
    public void onFailure(Exception cause) {
        onException(cause);
    }

    abstract public void onSuccessResponse(BeanstalkResponse response);

    abstract public void onException(Exception cause);
}
