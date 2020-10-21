package com.alibaba.otter.canal.adapter.launcher.config;

import com.alibaba.otter.canal.adapter.launcher.monitor.remote.RemoteConfigLoader;
import com.alibaba.otter.canal.adapter.launcher.monitor.remote.RemoteConfigLoaderFactory;
import com.alibaba.otter.canal.client.file.FileCanalConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Bootstrap级别配置加载
 *
 * @author rewerma @ 2019-01-05
 * @version 1.0.0
 */
public class BootstrapConfiguration {

    @Autowired
    private Environment        env;

    private RemoteConfigLoader remoteConfigLoader = null;

    @Autowired(required = false)
    private FileCanalConnector fileCanalConnector;

    @PostConstruct
    public void loadRemoteConfig() {
        remoteConfigLoader = RemoteConfigLoaderFactory.getRemoteConfigLoader(env);
        if (remoteConfigLoader != null) {
            remoteConfigLoader.loadRemoteConfig();
            remoteConfigLoader.loadRemoteAdapterConfigs();
            remoteConfigLoader.startMonitor(); // 启动监听
        }
    }

    @PreDestroy
    public synchronized void destroy() {
        if (remoteConfigLoader != null) {
            remoteConfigLoader.destroy();
        }
        if (fileCanalConnector!=null){
            fileCanalConnector.destroy();
        }
    }
}
