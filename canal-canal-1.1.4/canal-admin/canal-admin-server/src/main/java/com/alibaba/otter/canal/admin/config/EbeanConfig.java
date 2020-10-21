package com.alibaba.otter.canal.admin.config;

import io.ebean.EbeanServer;
import io.ebean.EbeanServerFactory;
import io.ebean.config.ServerConfig;
import io.ebean.config.UnderscoreNamingConvention;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * Ebean 配置
 *
 * @author rewerma 2019-07-13 下午05:12:16
 * @version 1.0.0
 */
@Configuration
public class EbeanConfig {

    @Bean("ebeanServer")
    public EbeanServer ebeanServer(DataSource dataSource) {
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setDefaultServer(true);
        serverConfig.setNamingConvention(new UnderscoreNamingConvention());
        List<String> packages = new ArrayList<>();
        packages.add("com.alibaba.otter.canal.admin.model");
        serverConfig.setPackages(packages);
        serverConfig.setName("ebeanServer");
        serverConfig.setDataSource(dataSource);
        serverConfig.setDatabaseSequenceBatchSize(1);
        serverConfig.setDdlGenerate(false);
        serverConfig.setDdlRun(false);
        return EbeanServerFactory.create(serverConfig);
    }

    @Bean(value = "restTemplate")
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder){
        RestTemplate restTemplate = restTemplateBuilder.build();
        return restTemplate;
    }
}
