package com.sufish.nbeaner.handlers.inbound;

public abstract class BeanStalkException extends RuntimeException {
    protected BeanStalkException() {
    }

    protected BeanStalkException(String message) {
        super(message);
    }
}
