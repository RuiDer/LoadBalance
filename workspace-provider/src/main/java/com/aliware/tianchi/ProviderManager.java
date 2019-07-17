package com.aliware.tianchi;

import com.aliware.tianchi.comm.CustomerInfo;


public class ProviderManager {

    private static final CustomerInfo SERVER_LOAD_INFO = new CustomerInfo();

    public static void start() {
        SERVER_LOAD_INFO.getActiveCount().incrementAndGet();
    }

    public static void end(long expend, boolean succeeded) {
        SERVER_LOAD_INFO.getActiveCount().decrementAndGet();
        SERVER_LOAD_INFO.getReqCount().incrementAndGet();
        SERVER_LOAD_INFO.getSpendTimeTotal().addAndGet(expend);
    }

    public static CustomerInfo getServerLoadInfo() {

        return SERVER_LOAD_INFO;
    }

    public static void resetSpendTime() {
        SERVER_LOAD_INFO.getSpendTimeTotal().set(0L);
        SERVER_LOAD_INFO.getReqCount().set(0L);
    }
}
