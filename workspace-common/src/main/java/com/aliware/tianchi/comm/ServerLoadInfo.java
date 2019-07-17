package com.aliware.tianchi.comm;

import java.util.concurrent.atomic.AtomicLong;

public class ServerLoadInfo {

    private String quota = null;
    private int providerThread = 0;
    private volatile int weight = 0;
    private AtomicLong reqCount = new AtomicLong(0);
    private AtomicLong activeCount = new AtomicLong(0);
    private AtomicLong spendTimeTotal = new AtomicLong(0);
    private volatile int avgSpendTime;

    public ServerLoadInfo() {
    }

    public ServerLoadInfo(String quota, int providerThread) {
        this.quota = quota;
        this.providerThread = (int) (providerThread * 0.9);
        if ("small".equals(quota)) {
            this.weight = 2;
        } else if ("medium".equals(quota)) {
            this.weight = 5;
        } else if ("large".equals(quota)) {
            this.weight = 8;
        } else {
            this.weight = 1;
        }
    }

    public String getQuota() {

        return quota;
    }

    public void setQuota(String quota) {

        this.quota = quota;
    }

    public int getWeight() {

        return weight;
    }

    public void setWeight(int weight) {

        this.weight = weight;
    }

    public AtomicLong getReqCount() {

        return reqCount;
    }

    public void setReqCount(AtomicLong reqCount) {

        this.reqCount = reqCount;
    }

    public AtomicLong getActiveCount() {

        return activeCount;
    }

    public void setActiveCount(AtomicLong activeCount) {

        this.activeCount = activeCount;
    }

    public AtomicLong getSpendTimeTotal() {

        return spendTimeTotal;
    }

    public void setSpendTimeTotal(AtomicLong spendTimeTotal) {

        this.spendTimeTotal = spendTimeTotal;
    }

    public int getAvgSpendTime() {

        return avgSpendTime;
    }

    public void setAvgSpendTime(int avgSpendTime) {

        this.avgSpendTime = avgSpendTime;
    }

    public int getProviderThread() {

        return providerThread;
    }

    public void setProviderThread(int providerThread) {

        this.providerThread = providerThread;
    }

}
