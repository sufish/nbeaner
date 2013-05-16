package com.sufish.nbeaner.protocol;

public class ReleaseRequest extends BeanstalkRequest {
    public ReleaseRequest(int jobId, int priority, int delay) {
        super(String.format("release %d %d %d", jobId, priority, delay), null);
    }
}
