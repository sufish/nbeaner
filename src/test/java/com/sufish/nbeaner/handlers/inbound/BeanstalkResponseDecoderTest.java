package com.sufish.nbeaner.handlers.inbound;

import com.sufish.nbeaner.protocol.BeanstalkResponse;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedByteChannel;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;


public class BeanstalkResponseDecoderTest {
    private EmbeddedByteChannel ch = new EmbeddedByteChannel(new BeanstalkResponseDecoder());

    @Test
    public void testSequenceDecode() {
        byte[] data = "RESERVED 2 2\r\n12\r\n".getBytes();
        ch.writeInbound(Unpooled.wrappedBuffer(data));
        BeanstalkResponse response = (BeanstalkResponse) ch.readInbound();
        assertThat(response).isNotNull();
        assertThat(response.getResponseParm()).isEqualTo("2");
        assertThat(response.getSecondFrameLength()).isEqualTo(2);
        assertThat(response.getResponseData()).isEqualTo(new byte[]{'1', '2'});

        data = "DELETED\r\n".getBytes();
        ch.writeInbound(Unpooled.wrappedBuffer(data));
        response = (BeanstalkResponse) ch.readInbound();
        assertThat(response).isNotNull();
        assertThat(response.getResponseText()).isEqualTo("DELETED");
        assertThat(response.getSecondFrameLength()).isEqualTo(0);
        assertThat(response.getResponseData()).isNull();
    }
}
