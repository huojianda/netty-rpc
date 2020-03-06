package com.hrpc.netty.rpc.registry;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

/**
 * @author huoji
 * 服务端 ，注册中心
 */
public class HrpcRegistry {

    private int port;

    public HrpcRegistry(int port) {
        this.port = port;
    }

    public void start(){
        EventLoopGroup boosGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boosGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel channel) throws Exception {
                            ChannelPipeline pipeline = channel.pipeline();
                            //自定义协议解码器
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,0,4,0,4));
                            //自定义协议编码器
                            pipeline.addLast(new LengthFieldPrepender(4));
                            //对象参数编码器
                            pipeline.addLast("encoder",new ObjectEncoder());
                            //对象参数解码器
                            pipeline.addLast("decoder",new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
                            pipeline.addLast(new RegistryHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG,128)
                    .childOption(ChannelOption.SO_KEEPALIVE,true);
            ChannelFuture future = bootstrap.bind(port).sync();
            System.out.println("HRPC register start listen at "+port);
            future.channel().closeFuture().sync();
        }catch (Exception e){
            boosGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        new HrpcRegistry(8080).start();
    }
}
