package com.sufish.nbeaner.handlers.inbound;

import com.sufish.nbeaner.protocol.Beanstalk;
import com.sufish.nbeaner.protocol.BeanstalkResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.MessageBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.nio.charset.Charset;

public class BeanstalkResponseDecoder extends ReplayingDecoder<BeanstalkResponseDecoderStatus> {


    private BeanstalkResponse response;

    public BeanstalkResponseDecoder() {
        super(BeanstalkResponseDecoderStatus.READ_FIRST_FRAME);
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, MessageBuf<Object> objects) throws Exception {
        switch (state()) {
            case READ_FIRST_FRAME:
                int eol = findEndOfLine(byteBuf);
                if (eol < 0) {
                    //force Signal;
                    byteBuf.getByte(byteBuf.writerIndex());
                    return;
                }
                final ByteBuf firstFrame = byteBuf.readBytes(eol - byteBuf.readerIndex());
                byteBuf.skipBytes(2);
                response = buildResponse(firstFrame.toString(Charset.defaultCharset()));
                if (response.getSecondFrameLength() == 0) {
                    objects.add(response);
                    checkpoint(BeanstalkResponseDecoderStatus.READ_FIRST_FRAME);
                    return;
                } else {
                    checkpoint(BeanstalkResponseDecoderStatus.READ_SECOND_FRAME);
                }
            case READ_SECOND_FRAME:
                byteBuf.readBytes(response.getResponseData());
                checkpoint(BeanstalkResponseDecoderStatus.READ_FRAME_END);
            case READ_FRAME_END:
                byteBuf.skipBytes(2);
                objects.add(response);
                checkpoint(BeanstalkResponseDecoderStatus.READ_FIRST_FRAME);
                break;
            default:
                throw new Error("Shouldn't reach here.");
        }
    }

    private BeanstalkResponse buildResponse(String firstFrame) {
        String[] headers = firstFrame.split(" ");
        if (headers.length == 0) {
            throw new BeanstalkUnexpectedResponseException("responses has no header:" + firstFrame);
        }
        BeanstalkResponse response = new BeanstalkResponse(headers[0]);
        switch (response.getResponseText()) {
            case Beanstalk.RESERVED:
                if (headers.length != 3) {
                    throw new BeanstalkUnexpectedResponseException("response header is not correct:" + firstFrame);
                }
                response.setResponseParm(headers[1]);
                response.markHasSecondFrame(Integer.valueOf(headers[2]));
                break;
            case Beanstalk.INSERTED:
                if (headers.length != 2) {
                    throw new BeanstalkUnexpectedResponseException("response header is not correct:" + firstFrame);
                }
                response.setResponseParm(headers[1]);
                break;
            case Beanstalk.OK:
                if (headers.length != 2) {
                    throw new BeanstalkUnexpectedResponseException("response header is not correct:" + firstFrame);
                }
                response.markHasSecondFrame(Integer.valueOf(headers[1]));
                break;
        }
        return response;
    }

    private static int findEndOfLine(final ByteBuf buffer) {
        final int n = buffer.writerIndex();
        for (int i = buffer.readerIndex(); i < n; i++) {
            final byte b = buffer.getByte(i);
            if (b == '\r' && i < n - 1 && buffer.getByte(i + 1) == '\n') {
                return i;  // \r\n
            }
        }
        return -1;  // Not found.
    }
}
