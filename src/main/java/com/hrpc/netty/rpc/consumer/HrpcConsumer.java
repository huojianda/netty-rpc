package com.hrpc.netty.rpc.consumer;

import com.hrpc.netty.rpc.api.IHelloService;
import com.hrpc.netty.rpc.api.IRpcService;
import com.hrpc.netty.rpc.consumer.proxy.HrpcProxy;
import com.hrpc.netty.rpc.provider.HelloServiceImpl;

/**
 * @author huoji
 */
public class HrpcConsumer {

    public static void main(String[] args) {
        IHelloService helloService = HrpcProxy.create(IHelloService.class);

        System.out.println(helloService.sayHello("danielHuo"));

        IRpcService rpcService = HrpcProxy.create(IRpcService.class);

        System.out.println("8 + 2 = " + rpcService.add(8,2));
        System.out.println("8 - 2 = " + rpcService.sub(8,2));
        System.out.println("8 * 2 = " + rpcService.mult(8,2));
        System.out.println("8 / 2 = " + rpcService.div(8,2));
    }
}
