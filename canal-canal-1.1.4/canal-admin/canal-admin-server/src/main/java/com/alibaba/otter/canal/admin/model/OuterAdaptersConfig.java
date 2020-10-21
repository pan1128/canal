package com.alibaba.otter.canal.admin.model;

import io.ebean.Finder;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * @author Rivan
 * @ClassName outerAdaptersConfig
 * @Description TODO
 * @Date 2020/10/10 10:03
 */
@Entity
public class OuterAdaptersConfig extends Model {
    public static final OuterAdaptersConfig.OuterAdaptersConfigFinder find = new OuterAdaptersConfig.OuterAdaptersConfigFinder();
    public static class OuterAdaptersConfigFinder extends Finder<Long, OuterAdaptersConfig> {
        public OuterAdaptersConfigFinder(){
            super(OuterAdaptersConfig.class);
        }

    }
    @Id
    private Long id;
    private String name;
    private String key;
    private String driverClassName;
    private String url;
    private String username;
    private String password;
    private String content;
    private Date modifiedTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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
