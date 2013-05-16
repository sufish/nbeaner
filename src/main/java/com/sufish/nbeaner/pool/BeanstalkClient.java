package com.sufish.nbeaner.pool;

import com.sufish.nbeaner.handlers.inbound.BeanstalkResponseDecoder;
import com.sufish.nbeaner.handlers.inbound.BeanstalkResponseHandler;
import com.sufish.nbeaner.handlers.outbound.BeanstalkRequestEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

public class BeanstalkClient {
    private GenericObjectPool<BeanstalkConnection> connectionPool;
    private GenericObjectPool.Config poolConfig = new GenericObjectPool.Config();
    private Bootstrap bootstrap;

    private int port;
    private String host;
    private String tube;
    private NioEventLoopGroup group;
    private DefaultEventExecutorGroup eventExecutors;

    public BeanstalkClient(int port, String host, String tube) {
        this.port = port;
        this.host = host;
        this.tube = tube;
        init();
    }

    public BeanstalkClient(int port, String host) {
        this.port = port;
        this.host = host;
        this.tube = "default";
        init();
    }

    private void init() {
        group = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<Channel>() {
                    public void initChannel(Channel channel) {
                        channel.pipeline().addLast("message-encoder", new BeanstalkRequestEncoder());
                        channel.pipeline().addLast("message-decoder", new BeanstalkResponseDecoder());
                        eventExecutors = new DefaultEventExecutorGroup(16);
                        channel.pipeline().addLast(eventExecutors, "response-handler", new BeanstalkResponseHandler());
                    }
                });
        poolConfig.testOnBorrow = true;
        poolConfig.whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_GROW;

        connectionPool = new GenericObjectPool<>(new BeanstalkConnectionFactory(), poolConfig);
    }

    public void shutDown() throws Exception {
        connectionPool.close();
        eventExecutors.shutdownGracefully();
        group.shutdownGracefully();

    }

    public BeanstalkConnection getConnection() throws Exception {
        return connectionPool.borrowObject();
    }

    class BeanstalkConnectionFactory extends BasePoolableObjectFactory<BeanstalkConnection> {

        @Override
        public BeanstalkConnection makeObject() throws Exception {
            ChannelFuture future = bootstrap.connect(host, port);
            future.awaitUninterruptibly();
            if (future.isSuccess()) {
                BeanstalkConnection beanstalkConnection = new BeanstalkConnection(future.channel(), connectionPool);
                beanstalkConnection.use(tube);
                return beanstalkConnection;
            } else {
                throw new BeanstalkConnectionException(future.cause());
            }
        }

        @Override
        public boolean validateObject(BeanstalkConnection connection) {
            return connection.isValidate();
        }

        @Override
        public void destroyObject(BeanstalkConnection connection) {
            connection.close();
        }
    }

}
