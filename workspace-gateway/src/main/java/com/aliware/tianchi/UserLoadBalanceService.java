package com.aliware.tianchi;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.dubbo.rpc.Invoker;

import com.aliware.tianchi.comm.ServerLoadInfo;

public class UserLoadBalanceService {
    
    // key:quota value:ServerLoadInfo
    private static final Map<String,ServerLoadInfo> LOAD_INFO = new ConcurrentHashMap<String,ServerLoadInfo>();
    private static final Map<String, AtomicInteger> LIMIT_MAP = new ConcurrentHashMap<String,AtomicInteger>();

    private static final String HOST_REFIX = "provider-";
    public static ServerLoadInfo getServerLoadInfo(Invoker<?> invoker){
        
        String host = invoker.getUrl().getHost();
        ServerLoadInfo serverLoadInfo = LOAD_INFO.get(host);
        return serverLoadInfo;
    }
    
    public static void putLoadInfo(String notiftStr) {
        
        String[] severLoadArr = notiftStr.split(",");
    
        // 环境,线程总数,活跃线程数,平均耗时
        String quota = severLoadArr[0];
        int providerThread = Integer.valueOf(severLoadArr[1]);
        int activeCount = Integer.valueOf(severLoadArr[2]);
        int avgTime = Integer.valueOf(severLoadArr[3]);
        int reqCount = Integer.valueOf(severLoadArr[4]);
        String key = HOST_REFIX+quota;
        ServerLoadInfo serverLoadInfo = LOAD_INFO.get(key);
        if(serverLoadInfo == null){
            // 初始化
            serverLoadInfo = new ServerLoadInfo(quota,providerThread);
            LOAD_INFO.put(key, serverLoadInfo);
        }
        serverLoadInfo.getActiveCount().set(activeCount);
        // 服务端可用线程数 = 总数-活跃线程数
        int availCount = serverLoadInfo.getProviderThread() - activeCount;
        serverLoadInfo.setAvgSpendTime(avgTime);
        
//        // 权重默认为可用线程数
//        serverLoadInfo.setWeight(availCount);
        AtomicInteger limiter = LIMIT_MAP.get(key);
        if(limiter == null){
            limiter = new AtomicInteger(availCount);
            LIMIT_MAP.put(key, limiter);
        }else{
            //limiter.set(availCount);
        }
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String nowStr = sdf.format(now);
        System.out.println(String.format("时间:%s,环境:%s,活跃线程数:%s,可用线程数:%s,请求数:%s,平均耗时:%s,权重:%s", 
            nowStr,quota,activeCount,availCount,reqCount,avgTime,serverLoadInfo.getWeight()));
    }
    
    public static AtomicInteger getAtomicInteger(Invoker<?> invoker) {
        String host = invoker.getUrl().getHost();
        AtomicInteger limiter = LIMIT_MAP.get(host);
        return limiter;
    }
    
}
