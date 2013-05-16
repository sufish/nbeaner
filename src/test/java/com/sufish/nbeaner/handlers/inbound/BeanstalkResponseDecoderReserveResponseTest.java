package com.sufish.nbeaner.handlers.inbound;

import com.sufish.nbeaner.protocol.BeanstalkResponse;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundMessageHandlerAdapter;
import io.netty.channel.embedded.EmbeddedByteChannel;
import org.junit.Test;

import java.util.Arrays;

import static org.fest.assertions.Assertions.assertThat;

public class BeanstalkResponseDecoderReserveResponseTest {

    private EmbeddedByteChannel ch = new EmbeddedByteChannel(new BeanstalkResponseDecoder());

    @Test
    public void testWithFullFrame() {
        byte[] data = "RESERVED 2 2\r\n12\r\n".getBytes();
        ch.writeInbound(Unpooled.wrappedBuffer(data));
        BeanstalkResponse response = (BeanstalkResponse) ch.readInbound();
        assertThat(response).isNotNull();
        assertThat(response.getResponseParm()).isEqualTo("2");
        assertThat(response.getSecondFrameLength()).isEqualTo(2);
        assertThat(response.getResponseData()).isEqualTo(new byte[]{'1', '2'});
    }

    @Test
    public void testWithPartialFrames() {
        byte[] data = "RESERVED 2 2\r\n12\r\n".getBytes();
        byte[] part1 = Arrays.copyOfRange(data, 0, 3);
        byte[] part2 = Arrays.copyOfRange(data, 3, 8);
        byte[] part3 = Arrays.copyOfRange(data, 8, data.length);

        ch.writeInbound(Unpooled.wrappedBuffer(part1));
        assertThat(ch.readInbound()).isNull();
        ch.writeInbound(Unpooled.wrappedBuffer(part2));
        assertThat(ch.readInbound()).isNull();
        ch.writeInbound(Unpooled.wrappedBuffer(part3));
        BeanstalkResponse response = (BeanstalkResponse) ch.readInbound();
        assertThat(response).isNotNull();
        assertThat(response.getResponseParm()).isEqualTo("2");
        assertThat(response.getSecondFrameLength()).isEqualTo(2);
        assertThat(response.getResponseData()).isEqualTo(new byte[]{'1', '2'});
    }

    @Test
    public void testWithPartialFramesWithCRLFData() {
        byte[] data = "RESERVED 2 6\r\n\r\n12\r\n\r\n".getBytes();
        byte[] part1 = Arrays.copyOfRange(data, 0, 5);
        byte[] part2 = Arrays.copyOfRange(data, 5, 10);
        byte[] part3 = Arrays.copyOfRange(data, 10, data.length);

        ch.writeInbound(Unpooled.wrappedBuffer(part1));
        assertThat(ch.readInbound()).isNull();
        ch.writeInbound(Unpooled.wrappedBuffer(part2));
        assertThat(ch.readInbound()).isNull();
        ch.writeInbound(Unpooled.wrappedBuffer(part3));
        BeanstalkResponse response = (BeanstalkResponse) ch.readInbound();
        assertThat(response).isNotNull();
        assertThat(response.getResponseParm()).isEqualTo("2");
        assertThat(response.getSecondFrameLength()).isEqualTo(6);
        assertThat(response.getResponseData()).isEqualTo(new byte[]{'\r', '\n', '1', '2', '\r', '\n'});
    }

    @Test
    public void testExceptions() {
        final Throwable[] actual = new Throwable[1];
        ch = new EmbeddedByteChannel(new BeanstalkResponseDecoder(), new ChannelInboundMessageHandlerAdapter() {

            @Override
            public void messageReceived(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                actual[0] = cause.getCause();
            }
        });
        ch.writeInbound(Unpooled.wrappedBuffer("RESERVED\r\n".getBytes()));
        assertThat(actual[0]).isInstanceOf(BeanstalkUnexpectedResponseException.class);
    }
}
