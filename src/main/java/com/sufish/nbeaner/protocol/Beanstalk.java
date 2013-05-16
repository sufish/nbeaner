package com.sufish.nbeaner.protocol;

public class Beanstalk {
    public static final byte[] FRAME_DELIMITER = "\r\n".getBytes();
    public static final String INSERTED = "INSERTED";
    public static final String RESERVED = "RESERVED";
    public static final String DELETED = "DELETED";
    public static final String RELEASED = "RELEASED";
    public static final String USING = "USING";
    public static final String OK = "OK";
    public static final String NOT_FOUND = "NOT_FOUND";
}
