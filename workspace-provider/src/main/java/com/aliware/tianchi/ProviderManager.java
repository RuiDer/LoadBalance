package com.aliware.tianchi;

import com.aliware.tianchi.comm.CustomerInfo;


public class ProviderManager {

    private static final CustomerInfo SERVER_INFO = new CustomerInfo();


    public static CustomerInfo getServerInfo() {

        return SERVER_INFO;
    }

    public static void endTime(long expend, boolean succeeded) {
        SERVER_INFO.getAllActiveCount().decrementAndGet();
        SERVER_INFO.getAllReqCount().incrementAndGet();
        SERVER_INFO.getAllSpendTimeTotal().addAndGet(expend);
    }

    public static void resetTime() {
        SERVER_INFO.getAllSpendTimeTotal().set(0L);
        SERVER_INFO.getAllReqCount().set(0L);
    }

    public static void startTime() {
        SERVER_INFO.getAllActiveCount().incrementAndGet();
    }
}
