package com.alibaba.otter.canal.adapter.launcher.monitor.remote;

/**
 * @author Rivan
 * @ClassName MysqlGroup
 * @Description TODO
 * @Date 2020/10/15 17:05
 */
public class MysqlGroup {

    //主库url
    private String masterUrl;

    //从库url
    private String slaveUrl;

    //用户名
    private String username;

    private String password;

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
