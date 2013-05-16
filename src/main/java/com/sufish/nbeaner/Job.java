package com.sufish.nbeaner;

public class Job {
    private byte[] jobData;
    private int priority = 65535;
    private int delay = 0;
    private int ttr = 300;
    private int id;

    public Job(byte[] jobData, int priority, int delay, int ttr) {
        this.jobData = jobData;
        this.priority = priority;
        this.delay = delay;
        this.ttr = ttr;
    }

    public Job() {
    }

    public Job(byte[] jobData, int jobId) {
        this.jobData = jobData;
        this.id = jobId;
    }

    public byte[] getJobData() {
        return jobData;
    }

    public void setJobData(byte[] jobData) {
        this.jobData = jobData;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public int getTTR() {
        return ttr;
    }

    public void setTTR(int ttr) {
        this.ttr = ttr;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
