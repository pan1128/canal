package com.alibaba.otter.canal.adapter.launcher.loader;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.otter.canal.adapter.launcher.common.SyncSwitch;
import com.alibaba.otter.canal.adapter.launcher.config.AdapterCanalConfig;
import com.alibaba.otter.canal.adapter.launcher.config.SpringContext;
import com.alibaba.otter.canal.adapter.launcher.monitor.remote.ConfigItem;
import com.alibaba.otter.canal.adapter.launcher.monitor.remote.MysqlGroup;
import com.alibaba.otter.canal.client.adapter.support.DatasourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * 适配器启动业务类
 *
 * @author rewerma @ 2018-10-20
 * @version 1.0.0
 */
@Component
@RefreshScope
public class CanalAdapterService {

    private static final Logger logger  = LoggerFactory.getLogger(CanalAdapterService.class);

    private CanalAdapterLoader  adapterLoader;

    @Resource
    private ContextRefresher    contextRefresher;

    @Resource
    private AdapterCanalConfig  adapterCanalConfig;
    @Resource
    private Environment         env;

    // 注入bean保证优先注册
    @Resource
    private SpringContext       springContext;
    @Resource
    private SyncSwitch          syncSwitch;

    private volatile boolean    running = false;

    @PostConstruct
    public synchronized void init() {
        if (running) {
            return;
        }
        try {
            logger.info("## start the canal client adapters.");
            adapterLoader = new CanalAdapterLoader(adapterCanalConfig);
            adapterLoader.init();
            running = true;
            logger.info("## the canal client adapters are running now ......");
        } catch (Exception e) {
            logger.error("## something goes wrong when starting up the canal client adapters:", e);
        }
    }

    /**
     * 返回当前扫描线程运行标识
     * @return
     */
    public synchronized boolean getRunFlag(){
        return running;
    }

    @PreDestroy
    public synchronized void destroy() {
        if (!running) {
            return;
        }
        try {
            running = false;
            logger.info("## stop the canal client adapters");

            if (adapterLoader != null) {
                adapterLoader.destroy();
                adapterLoader = null;
            }
            for (DruidDataSource druidDataSource : DatasourceConfig.DATA_SOURCES.values()) {
                try {
                    druidDataSource.close();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
            DatasourceConfig.DATA_SOURCES.clear();
        } catch (Throwable e) {
            logger.warn("## something goes wrong when stopping canal client adapters:", e);
        } finally {
            logger.info("## canal client adapters are down.");
        }
    }

    public MysqlGroup getMysqlGroup(Long id) {
        String jdbcUrl = env.getProperty("canal.manager.jdbc.url");
        String driverName = env.getProperty("canal.manager.jdbc.driverName");
        String jdbcUsername = env.getProperty("canal.manager.jdbc.username");
        String jdbcPassword = env.getProperty("canal.manager.jdbc.password");
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl(jdbcUrl);
        dataSource.setUsername(jdbcUsername);
        dataSource.setPassword(jdbcPassword);
        dataSource.setInitialSize(1);
        dataSource.setMinIdle(1);
        dataSource.setMaxActive(1);
        dataSource.setMaxWait(60000);
        dataSource.setTimeBetweenEvictionRunsMillis(60000);
        dataSource.setMinEvictableIdleTimeMillis(300000);
        try {
            dataSource.init();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        Map<String, ConfigItem> remoteConfigStatus = new HashMap<>();
        String sql = "select master_url, username, password from mysql_group where id = "+id;
        MysqlGroup mysqlGroup=new MysqlGroup();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();

             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                mysqlGroup.setMasterUrl(rs.getString("master_url"));
                mysqlGroup.setUsername(rs.getString("username"));
                mysqlGroup.setPassword(rs.getString("password"));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        if (dataSource!=null&&!dataSource.isClosed()){
            dataSource.close();
        }
       return mysqlGroup;
    }

    public MysqlGroup getMysqlGroupOne() {
        {
            String jdbcUrl = env.getProperty("canal.manager.jdbc.url");
            String driverName = env.getProperty("canal.manager.jdbc.driverName");
            String jdbcUsername = env.getProperty("canal.manager.jdbc.username");
            String jdbcPassword = env.getProperty("canal.manager.jdbc.password");
            DruidDataSource dataSource = new DruidDataSource();
            dataSource.setDriverClassName("com.mysql.jdbc.Driver");
            dataSource.setUrl(jdbcUrl);
            dataSource.setUsername(jdbcUsername);
            dataSource.setPassword(jdbcPassword);
            dataSource.setInitialSize(1);
            dataSource.setMinIdle(1);
            dataSource.setMaxActive(1);
            dataSource.setMaxWait(60000);
            dataSource.setTimeBetweenEvictionRunsMillis(60000);
            dataSource.setMinEvictableIdleTimeMillis(300000);
            try {
                dataSource.init();
            } catch (SQLException e) {
                throw new RuntimeException(e.getMessage(), e);
            }

            Map<String, ConfigItem> remoteConfigStatus = new HashMap<>();
            String sql = "select master_url, username, password from mysql_group limit 1";
            MysqlGroup mysqlGroup=new MysqlGroup();
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement();

                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    mysqlGroup.setMasterUrl(rs.getString("master_url"));
                    mysqlGroup.setUsername(rs.getString("username"));
                    mysqlGroup.setPassword(rs.getString("password"));
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            if (dataSource!=null&&!dataSource.isClosed()){
                dataSource.close();
            }
            return mysqlGroup;
        }
    }
}
