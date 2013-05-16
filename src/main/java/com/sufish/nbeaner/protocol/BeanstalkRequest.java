package com.sufish.nbeaner.protocol;

import com.sufish.nbeaner.ResponseFuture;

public class BeanstalkRequest {
    protected String firstFrameContent;
    protected byte[] secondFrameContent;
    ResponseFuture future;

    public String firstFrame() {
        return firstFrameContent;
    }

    public byte[] secondFrame() {
        return secondFrameContent;
    }

    public BeanstalkRequest(String firstFrameContent, byte[] secondFrameContent) {
        buildRequest(firstFrameContent, secondFrameContent);
    }

    public void setFuture(ResponseFuture future) {
        this.future = future;
    }

    public ResponseFuture getFuture() {
        return future;
    }

    protected void buildRequest(String firstFrameContent, byte[] secondFrameContent) {
        this.firstFrameContent = firstFrameContent;
        this.secondFrameContent = secondFrameContent;
    }
}
