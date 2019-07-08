package com.aliware.tianchi;

import com.aliware.tianchi.comm.ServerLoadInfo;


public class ProvaderLoadService {
    
    // key host.methodName value 
    private static final ServerLoadInfo SERVER_LOAD_INFO = new ServerLoadInfo();
    
    public static void start(){
        SERVER_LOAD_INFO.getActiveCount().incrementAndGet();
    }
    
    public static void end(long expend, boolean succeeded){
        SERVER_LOAD_INFO.getActiveCount().decrementAndGet();
        SERVER_LOAD_INFO.getReqCount().incrementAndGet();
        SERVER_LOAD_INFO.getSpendTimeTotal().addAndGet(expend);
//        if(succeeded){
//            SERVER_LOAD_INFO.getReqSuccCount().incrementAndGet();
//        }else{
//            SERVER_LOAD_INFO.getReqFailCount().incrementAndGet();
//        }
    }
    
    public static ServerLoadInfo getServerLoadInfo() {
    
        return SERVER_LOAD_INFO;
    }

    public static void resetSpendTime(){
        SERVER_LOAD_INFO.getSpendTimeTotal().set(0L);
        SERVER_LOAD_INFO.getReqCount().set(0L);
    }
}
