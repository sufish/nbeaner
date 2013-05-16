package com.sufish.nbeaner.handlers.callback;

import com.sufish.nbeaner.protocol.Beanstalk;
import com.sufish.nbeaner.protocol.BeanstalkResponse;

public abstract class PutRequestCallback extends AbstractResponseCallback {
    public PutRequestCallback() {
        super(Beanstalk.INSERTED);
    }

    @Override
    public void onSuccessResponse(BeanstalkResponse response) {
        onPutSuccess(Integer.valueOf(response.getResponseParm()));
    }

    public abstract void onPutSuccess(int jobId);
}
