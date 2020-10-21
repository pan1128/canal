package com.alibaba.otter.canal.admin.model;

import io.ebean.Finder;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * canal dbsync心跳
 */
@Entity
@Table(name = "canal_heart")
public class CanalHeart extends Model{
    public static final CanalHeart.CanalHeartFinder find = new CanalHeart.CanalHeartFinder();

    public static class CanalHeartFinder extends Finder<Long, CanalHeart> {
        public CanalHeartFinder(){
            super(CanalHeart.class);
        }

    }
    @Id
    private int id;
    private String fileName;
    private Integer precent;
    private Date lastRunTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getPrecent() {
        return precent;
    }

    public void setPrecent(Integer precent) {
        this.precent = precent;
    }

    public Date getLastRunTime() {
        return lastRunTime;
    }

    public void setLastRunTime(Date lastRunTime) {
        this.lastRunTime = lastRunTime;
    }
}
