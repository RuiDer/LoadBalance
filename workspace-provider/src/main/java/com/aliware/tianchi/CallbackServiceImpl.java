package com.aliware.tianchi;

import org.apache.dubbo.common.Constants;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.context.ConfigManager;
import org.apache.dubbo.rpc.listener.CallbackListener;
import org.apache.dubbo.rpc.service.CallbackService;

import com.aliware.tianchi.comm.ServerLoadInfo;

import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author daofeng.xjf
 * <p>
 * 服务端回调服务
 * 可选接口
 * 用户可以基于此服务，实现服务端向客户端动态推送的功能
 */
public class CallbackServiceImpl implements CallbackService {

    public CallbackServiceImpl() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                
                String notifyStr = getNotifyStr();
                
                if (!listeners.isEmpty()) {
                    for (Map.Entry<String, CallbackListener> entry : listeners.entrySet()) {
                        try {
                            entry.getValue().receiveServerMsg(notifyStr);
                        } catch (Throwable t1) {
                            listeners.remove(entry.getKey());
                        }
                    }
                    ProvaderLoadService.resetSpendTime();
                }
            }
        }, 0, 1000);
    }

    private Timer timer = new Timer();

    /**
     * key: listener type
     * value: callback listener
     */
    private final Map<String, CallbackListener> listeners = new ConcurrentHashMap<>();

    @Override
    public void addListener(String key, CallbackListener listener) {
        listeners.put(key, listener);
        
        listener.receiveServerMsg(getNotifyStr()); // send notification for change
    }
    
    public String getNotifyStr(){
        
        Optional<ProtocolConfig> protocolConfig = ConfigManager.getInstance().getProtocol(Constants.DUBBO_PROTOCOL);
        int providerThread = protocolConfig.get().getThreads();
        String env = System.getProperty("quota");
        ServerLoadInfo serverLoadInfo = ProvaderLoadService.getServerLoadInfo();
        long activeCount = serverLoadInfo.getActiveCount().get();
        long spendTimeTotal = serverLoadInfo.getSpendTimeTotal().get();
        long reqCount = serverLoadInfo.getReqCount().get();
        long avgTime = 0;
        if(reqCount  != 0){
            avgTime = spendTimeTotal/reqCount;
        }
        // 环境,线程总数,活跃线程数,平均耗时
        String notifyStr = String.format("%s,%s,%s,%s,%s", 
            env,
            providerThread,
            activeCount,
            avgTime,
            reqCount);
        
        return notifyStr;
    }
    
//    private String getSystemLoadInfo(){
//        OperatingSystemMXBean system = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
//        double systemLoadAvg = system.getSystemCpuLoad();
//        double processCpuLoad = system.getProcessCpuLoad();
//        long totalMem = system.getTotalPhysicalMemorySize();
//        long freeMem =  system.getFreePhysicalMemorySize();
//        return "freeMem:"+freeMem+",systemLoadAvg:"+systemLoadAvg+",processCpuLoad:"+processCpuLoad;
//    }
}
