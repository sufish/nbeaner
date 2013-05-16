package com.sufish.nbeaner;

import com.sufish.nbeaner.handlers.callback.BeanstalkResponseCallback;
import com.sufish.nbeaner.pool.BeanstalkException;
import com.sufish.nbeaner.protocol.BeanstalkResponse;
import io.netty.util.AttributeKey;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

public class ResponseFuture {

    final public static AttributeKey<ConcurrentLinkedQueue<ResponseFuture>> FUTURE_KEY = new AttributeKey<>("futures");

    private BeanstalkResponse response;
    private CountDownLatch readyLatch;
    private Exception cause;
    private List<BeanstalkResponseCallback> callbackList = new LinkedList<>();

    public ResponseFuture() {
        readyLatch = new CountDownLatch(1);
    }

    public void setSuccess(BeanstalkResponse response) {
        this.response = response;
        readyLatch.countDown();
        notifyListeners();
    }

    public void setFailure(Exception cause) {
        this.cause = cause;
        readyLatch.countDown();
        notifyListeners();
    }

    public boolean isSuccessful() {
        return cause == null;
    }

    public boolean isDone() {
        return readyLatch.getCount() == 0;
    }

    public BeanstalkResponse get() throws BeanstalkException, InterruptedException {
        readyLatch.await();
        if (isSuccessful()) {
            return response;
        } else {
            throw new BeanstalkException(cause);
        }
    }

    private void notifyListeners() {
        for (BeanstalkResponseCallback callback : callbackList) {
            invokeCallback(callback);
        }
    }

    private void invokeCallback(BeanstalkResponseCallback callback) {
        if (isSuccessful()) {
            callback.onSuccess(response);
        } else {
            callback.onFailure(cause);
        }
    }

    public void addCallback(BeanstalkResponseCallback callback) {
        if (isDone()) {
            invokeCallback(callback);
        } else {
            callbackList.add(callback);
        }
    }

}
