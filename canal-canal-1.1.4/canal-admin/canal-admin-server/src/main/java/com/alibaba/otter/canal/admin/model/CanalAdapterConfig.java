package com.alibaba.otter.canal.admin.model;

import io.ebean.Finder;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.Date;

/**
 * @author Rivan
 * @ClassName CanalAdapterConfig
 * @Description TODO
 * @Date 2020/10/10 11:29
 */
@Entity
public class CanalAdapterConfig extends Model{
    public static final CanalAdapterConfig.CanalAdapterConfigFinder find = new CanalAdapterConfig.CanalAdapterConfigFinder();
    public static class CanalAdapterConfigFinder extends Finder<Long, CanalAdapterConfig> {
        public CanalAdapterConfigFinder(){
            super(CanalAdapterConfig.class);
        }

    }
    @Id
    private Long id;
    private String category;
    private String name;
    private String status;
    private String content;
    private Date modifiedTime;

    @Transient
    private RdbConfig rdbConfig;

    public RdbConfig getRdbConfig() {
        return rdbConfig;
    }

    public void setRdbConfig(RdbConfig rdbConfig) {
        this.rdbConfig = rdbConfig;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }
}
