package com.alibaba.otter.canal.admin.service.impl;

import com.alibaba.otter.canal.admin.model.CanalAdapterConfig;
import com.alibaba.otter.canal.admin.model.OuterAdaptersConfig;
import com.alibaba.otter.canal.admin.model.Pager;
import com.alibaba.otter.canal.admin.model.RdbConfig;
import com.alibaba.otter.canal.admin.service.CanalDbsyncService;
import io.ebean.Query;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Rivan
 * @ClassName CanalDbsyncServiceImpl
 * @Description TODO
 * @Date 2020/10/10 11:45
 */
@Service
public class CanalDbsyncServiceImpl implements CanalDbsyncService {
    @Override
    public Pager<CanalAdapterConfig> findList(Pager<CanalAdapterConfig> pager, String name) {
        Query<CanalAdapterConfig> query = CanalAdapterConfig.find.query();
        if (StringUtils.isNotEmpty(name)) {
            query.where().like("name", "%" + name + "%");
        }
        List<CanalAdapterConfig> nodeServers = query.order()
                .asc("id")
                .setFirstRow(pager.getOffset().intValue())
                .setMaxRows(pager.getSize())
                .findList();

        for (CanalAdapterConfig config:nodeServers){
            Yaml yaml = new Yaml();
            Map<String, Object> load = (Map<String, Object>)yaml.load(config.getContent());
            RdbConfig rdbConfig=new RdbConfig();
            rdbConfig.setDestination((String) load.get("destination"));
            rdbConfig.setOuterAdapterKey((String)load.get("outerAdapterKey"));
            rdbConfig.setGroupId((String)load.get("groupId"));
            String database = (String) (((LinkedHashMap<String, Object>) load.get("dbMapping")).get("database"));
            rdbConfig.setDbMappingDatabase(database);
            config.setRdbConfig(rdbConfig);
        }
        Query<CanalAdapterConfig> queryCnt = query.copy();
        int count = queryCnt.findCount();
        pager.setCount((long) count);
        pager.setItems(nodeServers);
        return pager;
    }

    @Override
    public void delete(CanalAdapterConfig canalAdapterConfig) {
        CanalAdapterConfig config=CanalAdapterConfig.find.byId(canalAdapterConfig.getId());
        if (config!=null){
            config.delete();
        }
    }

    @Override
    public void update(CanalAdapterConfig canalAdapterConfig) {
        canalAdapterConfig.update("name","content","modifiedTime");
    }

    @Override
    public void saveOuterAdaptersConfig(OuterAdaptersConfig outerAdaptersConfig) {
        outerAdaptersConfig.save();
    }

    @Override
    public void createRdbConfig() {

    }
}
