package com.sufish.nbeaner.protocol;

public class ReserveRequest extends BeanstalkRequest {
    public ReserveRequest() {
        super("reserve", null);
    }
}
