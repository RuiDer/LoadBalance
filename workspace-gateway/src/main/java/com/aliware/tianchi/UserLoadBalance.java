package com.aliware.tianchi;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.cluster.LoadBalance;

import com.aliware.tianchi.comm.ServerLoadInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author daofeng.xjf
 *
 * 负载均衡扩展接口
 * 必选接口，核心接口
 * 此类可以修改实现，不可以移动类或者修改包名
 * 选手需要基于此类实现自己的负载均衡算法
 */
public class UserLoadBalance implements LoadBalance{
    
    
    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        
        int size = invokers.size();
        // 总权重
        int totalWeight = 0;
        List<Integer> hasPermitArr = new ArrayList<>();
        List<Integer> weightArr = new ArrayList<>();
        // 首先获取invoker对应的服务端耗时最大的索引
        for(int index=0;index<size;index++){
            Invoker<T> invoker = invokers.get(index);
            ServerLoadInfo serverLoadInfo = UserLoadBalanceService.getServerLoadInfo(invokers.get(index));
            AtomicInteger limiter = UserLoadBalanceService.getAtomicInteger(invoker);
            
            if(serverLoadInfo != null){
                int permits = limiter.get();
                int weight = serverLoadInfo.getWeight();
                if(permits > 0 ){
                    //根据耗时重新计算权重(基本权重*(1秒/单个请求耗时))
                    int clientTimeAvgSpendCurr = serverLoadInfo.getAvgSpendTime();
                    if(clientTimeAvgSpendCurr == 0){
                        // 没有请求数据
                        return invokers.get(ThreadLocalRandom.current().nextInt(invokers.size()));
                    }
                    weight = weight*(200/clientTimeAvgSpendCurr);
                    hasPermitArr.add(index);
                    weightArr.add(weight);
                    totalWeight = totalWeight+weight;
                }
            }
        }
        // 服务都被打满了,根据耗时选一个时间较短的服务端
        if(hasPermitArr.size() == 0){
            for(int index=0;index<size;index++){
                ServerLoadInfo serverLoadInfo = UserLoadBalanceService.getServerLoadInfo(invokers.get(index));
                if(serverLoadInfo != null){
                    int weight = serverLoadInfo.getWeight();
                        //根据耗时重新计算权重(基本权重*(1秒/单个请求耗时))
                        int clientTimeAvgSpendCurr = serverLoadInfo.getAvgSpendTime();
                        if(clientTimeAvgSpendCurr == 0){
                            // 没有请求数据
                            return invokers.get(ThreadLocalRandom.current().nextInt(invokers.size()));
                        }
                        weight = weight*(200/clientTimeAvgSpendCurr);
                        hasPermitArr.add(index);
                        weightArr.add(weight);
                        totalWeight = totalWeight+weight;
                }
            }
        }
        // 根据服务端配置和平均耗时计算权重
        int offsetWeight = ThreadLocalRandom.current().nextInt(totalWeight);
        
        for(int i=0;i<hasPermitArr.size();i++){
            int index = hasPermitArr.get(i);
            int currentWeight = weightArr.get(i);
            offsetWeight  = offsetWeight - currentWeight;
            if (offsetWeight < 0) {
                return invokers.get(index);
            }
        }
        
        return invokers.get(ThreadLocalRandom.current().nextInt(invokers.size()));
    }
    
}
