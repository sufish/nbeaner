package com.sufish.nbeaner.protocol;

public class StatsTubeRequest extends BeanstalkRequest {
    public StatsTubeRequest(String tubeName) {
        super("stats-tube " + tubeName, null);
    }
}
