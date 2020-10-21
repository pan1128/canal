package com.alibaba.otter.canal.admin.model;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author Rivan
 * @ClassName RdbConfig
 * @Description TODO
 * @Date 2020/10/10 9:36
 */
public class RdbConfig {

    @ApiModelProperty("默认值：defaultDS")
    private String dataSourceKey;
    @ApiModelProperty(value = "instance实例名称")
    private String destination;
    @ApiModelProperty(value = "groupId,对应application.yml配置groups中的groupId")
    private String groupId;
    @ApiModelProperty(value = "adapter key, 对应application.yml配置outAdapters中的key")
    private String outerAdapterKey;
    @ApiModelProperty("默认值：true")
    private String concurrent;
    @ApiModelProperty("默认值：true")
    private String dbMappingMirrorDb;
    @ApiModelProperty(value = "数据库名称")
    private String dbMappingDatabase;

    public String getDataSourceKey() {
        return dataSourceKey;
    }

    public void setDataSourceKey(String dataSourceKey) {
        this.dataSourceKey = dataSourceKey;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getOuterAdapterKey() {
        return outerAdapterKey;
    }

    public void setOuterAdapterKey(String outerAdapterKey) {
        this.outerAdapterKey = outerAdapterKey;
    }

    public String getConcurrent() {
        return concurrent;
    }

    public void setConcurrent(String concurrent) {
        this.concurrent = concurrent;
    }

    public String getDbMappingMirrorDb() {
        return dbMappingMirrorDb;
    }

    public void setDbMappingMirrorDb(String dbMappingMirrorDb) {
        this.dbMappingMirrorDb = dbMappingMirrorDb;
    }

    public String getDbMappingDatabase() {
        return dbMappingDatabase;
    }

    public void setDbMappingDatabase(String dbMappingDatabase) {
        this.dbMappingDatabase = dbMappingDatabase;
    }
}
