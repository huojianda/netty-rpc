package com.hrpc.netty.rpc.protocol;

import lombok.Data;

import java.io.Serializable;

/**
 * @author huoji
 * 协议层
 */
@Data
public class InvokerProtocol  implements Serializable{

    private String className;
    private String methodName;
    private Class<?>[] parames;
    private Object[] values;
}
