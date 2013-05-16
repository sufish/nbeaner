package com.sufish.nbeaner.protocol;

public class DeleteRequest extends BeanstalkRequest {
    public DeleteRequest(int jobId) {
        super("delete " + jobId, null);
    }
}
