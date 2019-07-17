package com.aliware.tianchi;

import org.apache.dubbo.common.Constants;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.context.ConfigManager;
import org.apache.dubbo.rpc.listener.CallbackListener;
import org.apache.dubbo.rpc.service.CallbackService;

import com.aliware.tianchi.comm.CustomerInfo;

import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 布玮
 */
public class CallbackServiceImpl implements CallbackService {

    private String getInfo() {
        CustomerInfo customerInfo = ProviderManager.getServerInfo();
        Optional<ProtocolConfig> protocolConfig = ConfigManager.getInstance().getProtocol(Constants.DUBBO_PROTOCOL);
        String env = System.getProperty("quota");
        int providerThread = protocolConfig.get().getThreads();
        long allSpendTimeTotal = customerInfo.getAllSpendTimeTotal().get();
        long allReqCount = customerInfo.getAllReqCount().get();
        long allAvgTime = 0;
        long allActiveCount = customerInfo.getAllActiveCount().get();
        if (allReqCount != 0) {
            allAvgTime = allSpendTimeTotal / allReqCount;
        }
        StringBuilder info = new StringBuilder();
        info.append(env).append(",").append(providerThread).append(",").append(allActiveCount).append(",").append(allAvgTime).append(",").append(allReqCount);

        return info.toString();
    }

    public CallbackServiceImpl() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                if (!listeners.isEmpty()) {
                    for (Map.Entry<String, CallbackListener> entry : listeners.entrySet()) {
                        try {
                            entry.getValue().receiveServerMsg(getInfo());
                        } catch (Throwable t1) {
                            listeners.remove(entry.getKey());
                        }
                    }
                    ProviderManager.resetTime();
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
        listener.receiveServerMsg(getInfo());
    }

}
