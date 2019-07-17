package com.aliware.tianchi;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.dubbo.rpc.Invoker;

import com.aliware.tianchi.comm.CustomerInfo;

public class CustomerInfoManager {

    public static final Map<String, CustomerInfo> LOAD_INFO = new ConcurrentHashMap<>();
    private static final Map<String, AtomicInteger> AVAIL_MAP = new ConcurrentHashMap<>();

    private static final String HOST_REFIX = "provider-";

    public static CustomerInfo getServerLoadInfo(Invoker<?> invoker) {

        String host = invoker.getUrl().getHost();
        CustomerInfo customerInfo = LOAD_INFO.get(host);
        return customerInfo;
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
        CustomerInfo customerInfo = LOAD_INFO.get(key);
        if (customerInfo == null) {
            // 初始化
            customerInfo = new CustomerInfo(quota, providerThread);
            LOAD_INFO.put(key, customerInfo);
        }
        customerInfo.getActiveCount().set(activeCount);
        // 服务端可用线程数 = 总数-活跃线程数
        int availCount = customerInfo.getProviderThread() - activeCount;
        customerInfo.setAvgSpendTime(avgTime);

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
