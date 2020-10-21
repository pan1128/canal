package com.alibaba.otter.canal.admin.service;

import com.alibaba.otter.canal.admin.model.CanalAdapterConfig;
import com.alibaba.otter.canal.admin.model.OuterAdaptersConfig;
import com.alibaba.otter.canal.admin.model.Pager;

/**
 * @author bpyin
 * @ClassName CanalDbsyncService
 * @Description TODO
 * @Date 2020/10/10 11:44
 */
public interface CanalDbsyncService {
    Pager<CanalAdapterConfig> findList(Pager<CanalAdapterConfig> pager, String name);

    void delete(CanalAdapterConfig canalAdapterConfig);

    void update(CanalAdapterConfig canalAdapterConfig);

    void saveOuterAdaptersConfig(OuterAdaptersConfig outerAdaptersConfig);

    void createRdbConfig();
}
