package com.sufish.nbeaner.handlers.outbound;

import com.sufish.nbeaner.ResponseFuture;
import com.sufish.nbeaner.protocol.Beanstalk;
import com.sufish.nbeaner.protocol.BeanstalkRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundMessageHandlerAdapter;

import java.util.concurrent.ConcurrentLinkedQueue;

public class BeanstalkRequestEncoder extends ChannelOutboundMessageHandlerAdapter<BeanstalkRequest> {

    @Override
    public void flush(ChannelHandlerContext channelHandlerContext, BeanstalkRequest beanstalkRequest) throws Exception {
        ByteBuf buf = channelHandlerContext.nextOutboundByteBuffer();
        buf.writeBytes(beanstalkRequest.firstFrame().getBytes());
        buf.writeBytes(Beanstalk.FRAME_DELIMITER);
        if (beanstalkRequest.secondFrame() != null) {
            buf.writeBytes(beanstalkRequest.secondFrame());
            buf.writeBytes(Beanstalk.FRAME_DELIMITER);
        }
        if (beanstalkRequest.getFuture() != null) {
            ConcurrentLinkedQueue<ResponseFuture> futures = channelHandlerContext.channel().attr(ResponseFuture.FUTURE_KEY).get();
            if (futures != null) {
                futures.add(beanstalkRequest.getFuture());
            }
        }
    }
}
