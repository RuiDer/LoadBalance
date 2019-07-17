package com.aliware.tianchi.comm;

import java.util.concurrent.atomic.AtomicLong;

public class CustomerInfo {

    private String env = null;
    private volatile int serverWeight = 0;
    private int providerAllThreads = 0;
    private AtomicLong allReqCount = new AtomicLong(0);
    private volatile int avgSpendTime;
    private AtomicLong allSpendTimeTotal = new AtomicLong(0);
    private AtomicLong allActiveCount = new AtomicLong(0);


    public CustomerInfo() {
    }

    public CustomerInfo(String env, int providerThread) {
        this.env = env;
        this.providerAllThreads = (int) (providerThread * 0.9);
        if ("large".equals(env)) {
            this.serverWeight = 13;
        }





















        else if ("medium".equals(env)) {
            this.serverWeight = 9;
        } else if ("small".equals(env)) {
            this.serverWeight = 4;
        }
        else {
            this.serverWeight = 1;
        }
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public int getServerWeight() {
        return serverWeight;
    }

    public void setServerWeight(int serverWeight) {
        this.serverWeight = serverWeight;
    }

    public int getProviderAllThreads() {
        return providerAllThreads;
    }

    public void setProviderAllThreads(int providerAllThreads) {
        this.providerAllThreads = providerAllThreads;
    }

    public AtomicLong getAllReqCount() {
        return allReqCount;
    }

    public void setAllReqCount(AtomicLong allReqCount) {
        this.allReqCount = allReqCount;
    }

    public int getAvgSpendTime() {
        return avgSpendTime;
    }

    public void setAvgSpendTime(int avgSpendTime) {
        this.avgSpendTime = avgSpendTime;
    }

    public AtomicLong getAllSpendTimeTotal() {
        return allSpendTimeTotal;
    }

    public void setAllSpendTimeTotal(AtomicLong allSpendTimeTotal) {
        this.allSpendTimeTotal = allSpendTimeTotal;
    }

    public AtomicLong getAllActiveCount() {
        return allActiveCount;
    }

    public void setAllActiveCount(AtomicLong allActiveCount) {
        this.allActiveCount = allActiveCount;
    }
}
