package com.sufish.nbeaner.protocol;

public class BeanstalkResponse {
    protected String responseText;
    protected byte[] responseData;
    protected String responseParm;
    protected int secondFrameLength;

    public BeanstalkResponse(String responseText) {
        this.responseText = responseText;
    }

    public String getResponseText() {
        return responseText;
    }

    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }

    public byte[] getResponseData() {
        return responseData;
    }

    public void setResponseData(byte[] responseData) {
        this.responseData = responseData;
    }

    public String getResponseParm() {
        return responseParm;
    }

    public void setResponseParm(String responseArgument) {
        this.responseParm = responseArgument;
    }

    public int getSecondFrameLength() {
        return secondFrameLength;
    }

    public void setSecondFrameLength(int secondFrameLength) {
        this.secondFrameLength = secondFrameLength;
    }

    public void markHasSecondFrame(int secondFrameLength) {
        setSecondFrameLength(secondFrameLength);
        setResponseData(new byte[secondFrameLength]);
    }
}
