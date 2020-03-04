package com.hrpc.netty.rpc.provider;

import com.hrpc.netty.rpc.api.IRpcService;

public class HrpcServiceImpl implements IRpcService {
    public int add(int a, int b) {
        return a+b;
    }

    public int sub(int a, int b) {
        return a-b;
    }

    public int mult(int a, int b) {
        return a*b;
    }

    public int div(int a, int b) {
        return a/b;
    }
}
