package com.sufish.nbeaner.pool;

import com.sufish.nbeaner.Job;
import com.sufish.nbeaner.ResponseFuture;
import com.sufish.nbeaner.TubeStatus;
import com.sufish.nbeaner.handlers.callback.BeanstalkResponseCallback;
import com.sufish.nbeaner.handlers.callback.CallbackUtil;
import com.sufish.nbeaner.handlers.callback.PutRequestCallback;
import com.sufish.nbeaner.protocol.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.apache.commons.pool.impl.GenericObjectPool;

import java.util.concurrent.ConcurrentLinkedQueue;

public class BeanstalkConnection {
    private Channel channel;
    private GenericObjectPool<BeanstalkConnection> connectionPool;

    ConcurrentLinkedQueue<ResponseFuture> futures = new ConcurrentLinkedQueue<>();


    public BeanstalkConnection(Channel channel, GenericObjectPool<BeanstalkConnection> connectionPool) {
        this.channel = channel;
        this.connectionPool = connectionPool;
        this.channel.attr(ResponseFuture.FUTURE_KEY).set(futures);
    }

    protected BeanstalkResponse executeRequest(BeanstalkRequest request, BeanstalkResponseCallback callback) throws InterruptedException, BeanstalkException {
        final ResponseFuture future = new ResponseFuture();
        request.setFuture(future);
        if (callback != null) {
            future.addCallback(callback);
        }
        channel.write(request).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if (!channelFuture.isSuccess()) {
                    futures.remove(future);
                    future.setFailure(new BeanstalkConnectionException(channelFuture.cause()));
                }
            }
        });
        if (callback == null)
            return future.get();
        else {
            return null;
        }
    }


    public void close() {
        channel.closeFuture().awaitUninterruptibly();
    }

    public void release() {
        try {
            connectionPool.returnObject(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int put(Job job) throws InterruptedException, BeanstalkException {
        BeanstalkResponse response = executeRequest(new PutRequest(job), null);
        CallbackUtil.checkResponse(response, Beanstalk.INSERTED);
        return Integer.valueOf(response.getResponseParm());
    }

    public void put(Job job, PutRequestCallback callback) throws InterruptedException, BeanstalkException {
        executeRequest(new PutRequest(job), callback);
    }

    public Job reserve() throws InterruptedException, BeanstalkException {
        BeanstalkResponse response = executeRequest(new ReserveRequest(), null);
        CallbackUtil.checkResponse(response, Beanstalk.RESERVED);
        return new Job(response.getResponseData(), Integer.valueOf(response.getResponseParm()));
    }

    public void delete(int jobId) throws InterruptedException, BeanstalkException {
        BeanstalkResponse response = executeRequest(new DeleteRequest(jobId), null);
        CallbackUtil.checkResponse(response, Beanstalk.DELETED);
    }

    public void use(String tubeName) throws InterruptedException, BeanstalkException {
        BeanstalkResponse response = executeRequest(new UseRequest(tubeName), null);
        CallbackUtil.checkResponse(response, Beanstalk.USING);
    }

    public void releaseJob(int jobId, int priority, int delay) throws InterruptedException, BeanstalkException {
        BeanstalkResponse response = executeRequest(new ReleaseRequest(jobId, priority, delay), null);
        CallbackUtil.checkResponse(response, Beanstalk.RELEASED);
    }

    public TubeStatus statsTube(String tubeName) throws InterruptedException, BeanstalkException {
        BeanstalkResponse response = executeRequest(new StatsTubeRequest(tubeName), null);
        CallbackUtil.checkResponse(response, Beanstalk.OK);
        return new TubeStatus(response);
    }

    public boolean isValidate() {
        return channel.isActive();
    }
}
