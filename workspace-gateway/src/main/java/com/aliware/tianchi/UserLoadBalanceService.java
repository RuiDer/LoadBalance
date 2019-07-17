package com.aliware.tianchi;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.dubbo.rpc.Invoker;

import com.aliware.tianchi.comm.ServerLoadInfo;

public class UserLoadBalanceService {

    public static final Map<String, ServerLoadInfo> LOAD_INFO = new ConcurrentHashMap<>();
    private static final Map<String, AtomicInteger> AVAIL_MAP = new ConcurrentHashMap<>();

    private static final String HOST_REFIX = "provider-";

    public static ServerLoadInfo getServerLoadInfo(Invoker<?> invoker) {

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
//        int reqCount = Integer.valueOf(severLoadArr[4]);
        String key = HOST_REFIX + quota;
        ServerLoadInfo serverLoadInfo = LOAD_INFO.get(key);
        if (serverLoadInfo == null) {
            // 初始化
            serverLoadInfo = new ServerLoadInfo(quota, providerThread);
            LOAD_INFO.put(key, serverLoadInfo);
        }
        serverLoadInfo.getActiveCount().set(activeCount);
        // 服务端可用线程数 = 总数-活跃线程数
        int availCount = serverLoadInfo.getProviderThread() - activeCount;
        serverLoadInfo.setAvgSpendTime(avgTime);

        AtomicInteger avail = AVAIL_MAP.get(key);
        if (avail == null) {
            avail = new AtomicInteger(availCount);
            AVAIL_MAP.put(key, avail);
        }
    }

    /**
     * 获取活跃线程数
     */
    public static AtomicInteger getAvailThread(Invoker<?> invoker) {
        String host = invoker.getUrl().getHost();
        return AVAIL_MAP.get(host);
    }

}
