package com.alibaba.otter.canal.admin.model;

import java.util.Date;

/**
 * @author Rivan
 * @ClassName CanalDbsync
 * @Description TODO
 * @Date 2020/9/25 14:50
 */
public class CanalDbsync {
    private String instance;

    /*
    进度
     */
    private int progress;

    //运行状态名称
    private String status;

    //运行状态编码
    private String statusCode;

    //binlog文件总数
    private String fileTotal;

    //已回放结束的binlog文件总数
    private String fileOverTotal;

    //启动时间
    private Date startTime;

    private String fileOverTotalAndTotal;

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public String getFileOverTotalAndTotal() {
        return fileOverTotal+"/"+fileTotal;
    }

    public void setFileOverTotalAndTotal(String fileOverTotalAndTotal) {
        this.fileOverTotalAndTotal = fileOverTotalAndTotal;
    }

    public String getFileTotal() {
        return fileTotal;
    }

    public void setFileTotal(String fileTotal) {
        this.fileTotal = fileTotal;
    }

    public String getFileOverTotal() {
        return fileOverTotal;
    }

    public void setFileOverTotal(String fileOverTotal) {
        this.fileOverTotal = fileOverTotal;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
