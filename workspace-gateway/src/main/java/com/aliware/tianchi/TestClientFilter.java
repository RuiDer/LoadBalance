package com.aliware.tianchi;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.AsyncRpcResult;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;

@Activate(group = Constants.CONSUMER)
public class TestClientFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {

        AtomicInteger availThread = CustomerInfoManager.getAvailThread(invoker);
        if (availThread == null) {
            return invoker.invoke(invocation);
        }
        //并发数-1
        availThread.decrementAndGet();
        Result result = invoker.invoke(invocation);
        if (result instanceof AsyncRpcResult) {
            AsyncRpcResult asyncResult = (AsyncRpcResult) result;
            asyncResult.getResultFuture().whenComplete((actual, t) -> {
                // 服务端可用线程数+1
                availThread.incrementAndGet();
            });
        }
        return result;
    }

    @Override
    public Result onResponse(Result result, Invoker<?> invoker, Invocation invocation) {
        return result;
    }
}
