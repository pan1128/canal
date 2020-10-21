package com.alibaba.otter.canal.admin.model;

import java.util.Date;

/**
 * @author Rivan
 * @ClassName BinLogFile
 * @Description TODO
 * @Date 2020/10/15 19:17
 */
public class BinLogFile {

    private String fileName;

    private Date updateTime;

    private Date endTime;

    //是否回放结束
    private String status;

    //进度
    private int process;

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public int getProcess() {
        return process;
    }

    public void setProcess(int process) {
        this.process = process;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
