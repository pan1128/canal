package com.alibaba.otter.canal.admin.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.ebean.Finder;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.Date;

/**
 * @author Rivan
 * @ClassName Task
 * @Description TODO
 * @Date 2020/10/19 9:20
 */
@Entity
@JsonInclude(JsonInclude.Include.ALWAYS)
public class Task extends Model{
    public static final Task.TaskFinder find = new Task.TaskFinder();
    public static class TaskFinder extends Finder<Long, Task> {
        public TaskFinder(){
            super(Task.class);
        }

    }
    @Id
    private Long id;

    private String name;

    private String fileUrl;

    private String comment;

    private Long mysqlGroupId;

    private Date startTime;

    //运行状态
    private String status;

    private Date modifiedTime;

    //binlog文件总数
    @Transient
    private String fileTotal="0";

    //已回放结束的binlog文件总数
    @Transient
    private String fileOverTotal="0";

    @Transient
    private String fileOverTotalAndTotal;

    //进度
    @Transient
    private int progress=0;

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

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public Long getMysqlGroupId() {
        return mysqlGroupId;
    }

    public void setMysqlGroupId(Long mysqlGroupId) {
        this.mysqlGroupId = mysqlGroupId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
