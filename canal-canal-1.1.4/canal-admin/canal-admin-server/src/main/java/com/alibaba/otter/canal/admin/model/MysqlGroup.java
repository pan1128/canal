package com.alibaba.otter.canal.admin.model;

import io.ebean.Finder;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * mysql集群
 * @author Rivan
 * @ClassName MysqlGroup
 * @Description TODO
 * @Date 2020/10/14 14:33
 */
@Entity
public class MysqlGroup  extends Model{
    public static final MysqlGroup.MysqlGroupFinder find = new MysqlGroup.MysqlGroupFinder();
    public static class MysqlGroupFinder extends Finder<Long, MysqlGroup> {
        public MysqlGroupFinder(){
            super(MysqlGroup.class);
        }

    }
    @Id
    private Long id;
    //集群名称
    private String name;

    //主库url
    private String masterUrl;

    //从库url
    private String slaveUrl;

    //用户名
    private String username;

    private String password;

    //修改时间
    private Date modifiedTime;

    //是否被全局配置所关联
    private String isUsed;

    private String comment;

    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getIsUsed() {
        return isUsed;
    }

    public void setIsUsed(String isUsed) {
        this.isUsed = isUsed;
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMasterUrl() {
        return masterUrl;
    }

    public void setMasterUrl(String masterUrl) {
        this.masterUrl = masterUrl;
    }

    public String getSlaveUrl() {
        return slaveUrl;
    }

    public void setSlaveUrl(String slaveUrl) {
        this.slaveUrl = slaveUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
