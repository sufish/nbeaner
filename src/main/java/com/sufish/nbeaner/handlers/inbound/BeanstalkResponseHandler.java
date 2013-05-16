package com.sufish.nbeaner.handlers.inbound;

import com.sufish.nbeaner.ResponseFuture;
import com.sufish.nbeaner.pool.BeanstalkConnectionException;
import com.sufish.nbeaner.protocol.BeanstalkResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;

import java.util.concurrent.ConcurrentLinkedQueue;

public class BeanstalkResponseHandler extends ChannelInboundMessageHandlerAdapter<BeanstalkResponse> {

    @Override
    public void messageReceived(ChannelHandlerContext channelHandlerContext, BeanstalkResponse beanstalkResponse) throws Exception {
        ResponseFuture future = getFuture(channelHandlerContext);
        if (future != null && !future.isDone()) {
            future.setSuccess(beanstalkResponse);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable cause) throws Exception {
        System.out.println("FUCK this shit1");
        ResponseFuture future = getFuture(channelHandlerContext);
        if (future != null && !future.isDone()) {
            future.setFailure(new BeanstalkConnectionException(cause));
        }
        channelHandlerContext.close();
    }

    private ResponseFuture getFuture(ChannelHandlerContext channelHandlerContext) {
        ConcurrentLinkedQueue<ResponseFuture> futures = channelHandlerContext.channel().attr(ResponseFuture.FUTURE_KEY).get();
        if (futures != null) {
            return futures.poll();
        } else {
            return null;
        }
    }

}
