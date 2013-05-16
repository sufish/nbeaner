package com.sufish.nbeaner.protocol;

public class UseRequest extends BeanstalkRequest {
    public UseRequest(String tube) {
        super("use " + tube, null);
    }
}
