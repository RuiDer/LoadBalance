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
        listener.receiveServerMsg(getNotifyStr());
    }

    public String getNotifyStr() {
        // todo 协议和线程数的关系
        Optional<ProtocolConfig> protocolConfig = ConfigManager.getInstance().getProtocol(Constants.DUBBO_PROTOCOL);
        int providerThread = protocolConfig.get().getThreads();
        String env = System.getProperty("quota");
        ServerLoadInfo serverLoadInfo = ProvaderLoadService.getServerLoadInfo();
        long activeCount = serverLoadInfo.getActiveCount().get();
        long spendTimeTotal = serverLoadInfo.getSpendTimeTotal().get();
        long reqCount = serverLoadInfo.getReqCount().get();
        long avgTime = 0;
        if (reqCount != 0) {
            avgTime = spendTimeTotal / reqCount;
        }
        StringBuilder notifyStr = new StringBuilder();
        notifyStr.append(env).append(",").append(providerThread).append(",").append(activeCount).append(",").append(avgTime).append(",").append(reqCount);

        return notifyStr.toString();
    }

}
