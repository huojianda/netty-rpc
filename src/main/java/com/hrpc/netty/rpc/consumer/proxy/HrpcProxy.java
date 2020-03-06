package com.hrpc.netty.rpc.consumer.proxy;

import com.hrpc.netty.rpc.protocol.InvokerProtocol;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author huoji
 * 客户端 代理
 */
public class HrpcProxy {

    public static <T> T create(Class<?> clazz){
        MethodProxy proxy = new MethodProxy(clazz);
        Class[] interfaces = clazz.isInterface() ? new Class[]{clazz} : clazz.getInterfaces();
        T result = (T) Proxy.newProxyInstance(clazz.getClassLoader(),interfaces,proxy);
        return result;
    }

    private static class MethodProxy implements InvocationHandler{

        private Class<?> clazz;

        public MethodProxy(Class<?> clazz){
            this.clazz = clazz;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            //如果是一个已实现的具体类直接返回
            if(method.getDeclaringClass().toString().startsWith("class")){

                return method.invoke(this,args);
            }else{
                return hrpcInvoke(proxy,method,args);
            }
        }

        private Object hrpcInvoke(Object proxy, Method method, Object[] args) {
            //协议封装
            InvokerProtocol msg = new InvokerProtocol();
            msg.setClassName(this.clazz.getName());
            msg.setMethodName(method.getName());
            msg.setValues(args);
            msg.setParames(method.getParameterTypes());


            final HrpcProxyHandler consumerHandler = new HrpcProxyHandler();
            EventLoopGroup group = new NioEventLoopGroup();
            try{

                Bootstrap b = new Bootstrap();
                b.group(group)
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.TCP_NODELAY,true)
                        .handler(new ChannelInitializer<SocketChannel>() {
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
                                pipeline.addLast("handler",consumerHandler);
                            }
                        });

                ChannelFuture future = b.connect("localhost", 8080).sync();
                future.channel().writeAndFlush(msg).sync();
                future.channel().closeFuture().sync();
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                group.shutdownGracefully();
            }
            return consumerHandler.getResponse();
        }
    }
}
