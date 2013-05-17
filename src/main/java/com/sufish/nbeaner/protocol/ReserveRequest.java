package com.sufish.nbeaner.protocol;

public class ReserveRequest extends BeanstalkRequest {
    public ReserveRequest() {
        super("reserve", null);
    }

    public ReserveRequest(int timeout) {
        super("reserve-with-timeout " + timeout, null);
    }
}
