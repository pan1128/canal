package com.alibaba.otter.canal.client.file;


public class CanalFileAdapterPostionDto {
    private String instance;
    private String groupId;
    private String fileName;
    private Integer messageNum;

    public CanalFileAdapterPostionDto(String instance, String groupId, String fileName, Integer messageNum) {
        this.instance = instance;
        this.groupId = groupId;
        this.fileName = fileName;
        this.messageNum = messageNum;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getMessageNum() {
        return messageNum;
    }

    public void setMessageNum(Integer messageNum) {
        this.messageNum = messageNum;
    }
}
