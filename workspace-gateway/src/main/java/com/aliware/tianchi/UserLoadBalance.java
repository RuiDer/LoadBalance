package com.aliware.tianchi;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.LoadBalance;

import com.aliware.tianchi.comm.CustomerInfo;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class UserLoadBalance implements LoadBalance {

    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {

        //1、所有服务器争取每个打一次请求
        Invoker invoker = doSelectInFreeInvokers(invokers);

        //2、根据服务端信息分配权重
        invoker = invoker != null ? invoker : doSelectWithWeigth(invokers);

        return invoker;
    }

    /**
     * 落实优先每个机器都有流量请求
     */
    private <T> Invoker<T> doSelectInFreeInvokers(List<Invoker<T>> invokers) {

        if (CustomerInfoManager.LOAD_INFO.size() < invokers.size()) {
            for (Invoker invoker : invokers) {

                CustomerInfo customerInfo = CustomerInfoManager.getServerLoadInfo(invoker);

                if (customerInfo != null) break;

                return invoker;
            }
        }

        return null;
    }

    /**
     * 根据服务端配置和平均耗时计算权重
     */
    private <T> Invoker<T> doSelectWithWeigth(List<Invoker<T>> invokers) {

        // 总权重
        int totalWeight = 0;

        // 重新分配权重的<服务,权重>映射
        int[] serviceWeight = new int[invokers.size()];

        // 1、计算总权重
        for (int index = 0, size = invokers.size(); index < size; index++) {

            Invoker<T> invoker = invokers.get(index);

            CustomerInfo customerInfo = CustomerInfoManager.getServerLoadInfo(invoker);
            AtomicInteger availThreadAtomic = CustomerInfoManager.getAvailThread(invoker);

            if (customerInfo != null) {

                if (availThreadAtomic.get() > 0) {
                    int weight = customerInfo.getWeight();
                    //根据耗时重新计算权重(基本权重*(1秒/单个请求耗时))
                    int clientTimeAvgSpendCurr = customerInfo.getAvgSpendTime();
                    if (clientTimeAvgSpendCurr == 0) {
                        // 耗时为0，性能优，请求直接打到该机器
                        // 也有可能是性能差，采用随机
                        return invokers.get(ThreadLocalRandom.current().nextInt(invokers.size()));
                    }

                    // 计算权重
                    weight = weight * (500 / clientTimeAvgSpendCurr);
                    serviceWeight[index] = weight;
                    totalWeight = totalWeight + weight;
                }
            }
        }

        // 2、按照新的权重选择服务，权重加权随机算法
        int offsetWeight = ThreadLocalRandom.current().nextInt(totalWeight);

        for (int i = 0; i < invokers.size(); ++i) {
            offsetWeight -= serviceWeight[i];
            if (offsetWeight < 0) {
                return invokers.get(i);
            }
        }

        //兜底采用随机算法
        return invokers.get(ThreadLocalRandom.current().nextInt(invokers.size()));
    }
}
