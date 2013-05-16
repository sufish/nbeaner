package com.sufish.nbeaner.handlers.callback;

import com.sufish.nbeaner.protocol.BeanstalkResponse;

public interface BeanstalkResponseCallback {
    public void onSuccess(BeanstalkResponse response);

    public void onFailure(Exception cause);
}
