package com.alibaba.otter.canal.admin.common.eum;

public enum CanalDbsyncStatus {
    running("运行中","1"),dead("已结束","2");
    private String name;
    private String code;
    CanalDbsyncStatus(String name,String code){
        this.name=name;
        this.code=code;
    }
    public String getStatus(CanalDbsyncStatus status){
        return status.name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
