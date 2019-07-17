package com.aliware.tianchi;

import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;

@Activate(group = Constants.PROVIDER)
public class TestServerFilter implements Filter {
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {

        boolean isSuccess = false;
        long begin = System.currentTimeMillis();
        try {
            ProviderManager.startTime();
            Result result = invoker.invoke(invocation);
            isSuccess = true;
            return result;
        } catch (Exception e) {
            throw e;
        } finally {
            ProviderManager.endTime(System.currentTimeMillis() - begin, isSuccess);
        }

    }

    @Override
    public Result onResponse(Result result, Invoker<?> invoker, Invocation invocation) {
        return result;
    }

}
