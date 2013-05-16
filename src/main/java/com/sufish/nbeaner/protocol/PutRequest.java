package com.sufish.nbeaner.protocol;

import com.sufish.nbeaner.Job;

public class PutRequest extends BeanstalkRequest {
    public PutRequest(Job job) {
        super(String.format("put %d %d %d %d", job.getPriority(), job.getDelay(), job.getTTR(), job.getJobData().length), job.getJobData());
    }
}
