package com.alibaba.otter.canal.admin.service;

import com.alibaba.otter.canal.admin.model.BaseModel;
import com.alibaba.otter.canal.admin.model.CanalHeart;

import java.util.List;

public interface CanalHeartService<T> {

    BaseModel<List<CanalHeart>> getDbsyncStatus();

    String getLog() throws Exception;
}
