package com.hrpc.netty.rpc.provider;

import com.hrpc.netty.rpc.api.IHelloService;

public class HelloServiceImpl implements IHelloService {


    public String sayHello(String name) {
        return "你好,"+name;
    }
}
