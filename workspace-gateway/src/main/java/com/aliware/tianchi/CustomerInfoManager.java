package com.aliware.tianchi;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.dubbo.rpc.Invoker;

import com.aliware.tianchi.comm.CustomerInfo;

/**
 * @author 阿里巴巴金融核心 -- 布玮
 */
public class CustomerInfoManager {

    public static final Map<String, CustomerInfo> LOAD_INFO = new ConcurrentHashMap<>();
    private static final Map<String, AtomicInteger> AVAIL_MAP = new ConcurrentHashMap<>();

    public static CustomerInfo getServerLoadInfo(Invoker<?> invoker) {

        String host = invoker.getUrl().getHost();
        CustomerInfo customerInfo = LOAD_INFO.get(host);
        return customerInfo;
    }

    public static void updateInfo(String notiftStr) {

        String[] severLoadArr = notiftStr.split(",");

        String quota = severLoadArr[0];
        int providerThread = Integer.valueOf(severLoadArr[1]);
        int activeCount = Integer.valueOf(severLoadArr[2]);
        int avgTime = Integer.valueOf(severLoadArr[3]);
        String key = "provider-" + quota;
        CustomerInfo customerInfo = LOAD_INFO.get(key);
        if (customerInfo == null) {
            customerInfo = new CustomerInfo(quota, providerThread);
            LOAD_INFO.put(key, customerInfo);
        }
        customerInfo.getAllActiveCount().set(activeCount);
        int availCount = customerInfo.getProviderAllThreads() - activeCount;
        customerInfo.setAvgSpendTime(avgTime);

        AtomicInteger avail = AVAIL_MAP.get(key);
        if (avail == null) {
            avail = new AtomicInteger(availCount);
            AVAIL_MAP.put(key, avail);
        }
    }

    public static AtomicInteger getAvailThread(Invoker<?> invoker) {
        String host = invoker.getUrl().getHost();
        return AVAIL_MAP.get(host);
    }

}
