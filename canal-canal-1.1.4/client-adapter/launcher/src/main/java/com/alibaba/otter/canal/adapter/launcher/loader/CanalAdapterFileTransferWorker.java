package com.alibaba.otter.canal.adapter.launcher.loader;


import com.alibaba.otter.canal.adapter.launcher.config.SpringContext;
import com.alibaba.otter.canal.adapter.launcher.monitor.remote.MysqlGroup;
import com.alibaba.otter.canal.client.adapter.OuterAdapter;
import com.alibaba.otter.canal.client.adapter.support.CanalClientConfig;
import com.alibaba.otter.canal.client.adapter.support.Dml;
import com.alibaba.otter.canal.client.adapter.support.MessageUtil;
import com.alibaba.otter.canal.client.file.FileCanalConnector;
import com.alibaba.otter.canal.parse.driver.mysql.MysqlConnector;
import com.alibaba.otter.canal.parse.driver.mysql.MysqlUpdateExecutor;
import com.alibaba.otter.canal.protocol.Message;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * file对应的client适配器工作线程
 */
public class CanalAdapterFileTransferWorker extends  AbstractCanalAdapterWorker{

    private FileCanalConnector fileCanalConnector;

    public CanalAdapterFileTransferWorker(List<List<OuterAdapter>> canalOuterAdapters, CanalClientConfig canalClientConfig, String canalDestination) {
        super(canalOuterAdapters);
        this.canalClientConfig = canalClientConfig;
        this.canalDestination=canalDestination;
        fileCanalConnector=new FileCanalConnector(
                canalClientConfig.getDriverName(),
                canalClientConfig.getJdbcUrl(),
                canalClientConfig.getJdbcUsername(),
                canalClientConfig.getJdbcPassword()
                );
    }
    public void destory(){
        if (fileCanalConnector!=null)
            fileCanalConnector.destroy();
    }
    @Override
    protected void process() {
        while (running){
            long start=System.currentTimeMillis();
            try {
                syncSwitch.get(canalDestination);
                my:while (running){
                    String url= fileCanalConnector.getTask();
                    if (StringUtils.isBlank(url)){
                        continue my;
                    }
                    File file = new File(url);
                    File[] binLogFiles = file.listFiles();
                    fileCanalConnector.init();
                    List<File> files = Arrays.asList(binLogFiles);
                    List<String> fileNameList =fileCanalConnector.getHeartFileName();
                    files=files.stream().filter(s->!fileNameList.contains(s.getName())).sorted((o1,o2)-> o1.lastModified()>o2.lastModified()?1:-1).collect(Collectors.toList());
                    for (File binLogFile:files){
                        int tatal =0;
                        Message message = null;
                        ObjectInputStream is=null;
                        List<Message> messageListCallAble=Lists.newArrayList();
                        is=new ObjectInputStream(new FileInputStream(binLogFile));
                        try {
                            while ((message=(Message)is.readObject())!=null){
                                message.setFileName(binLogFile.getName());
                                if (message.getId()!=-1&&message.getEntries().size()!=0){
                                    List<Dml> dmls = MessageUtil.parse4Dml(canalDestination, groupId, message);
                                   for (Dml dml:dmls){
                                       int p=0;
                                       p++;
                                       if (dml.getSql()!=null){
                                           if (StringUtils.isNotBlank(dml.getDatabase())){
                                               int num = fileCanalConnector.getConfig(dml.getDatabase().replace("`", ""));
                                               if (num==0){
                                                   fileCanalConnector.insertConfig(dml.getDatabase().replace("`", ""));
                                                   Thread.sleep(15000);
                                               }
                                           }
                                           if (dml.getSql().toUpperCase().contains("CREATE DATABASE")){//||dml.getSql().contains("create database")
                                               //执行创库语句 开始 进行数据库创建
                                               CanalAdapterService canalAdapterService =(CanalAdapterService)SpringContext.getBean(CanalAdapterService.class);
                                               MysqlGroup mysqlGroup=canalAdapterService.getMysqlGroupOne();
                                               String address = mysqlGroup.getMasterUrl().substring(0, mysqlGroup.getMasterUrl().indexOf(":"));
                                               String port=mysqlGroup.getMasterUrl().substring(mysqlGroup.getMasterUrl().indexOf(":")+1);
                                               MysqlConnector connector = new MysqlConnector(new InetSocketAddress(address,Integer.parseInt(port)), mysqlGroup.getUsername(), mysqlGroup.getPassword());
                                               try {
                                                   connector.connect();
                                                   MysqlUpdateExecutor executor = new MysqlUpdateExecutor(connector);
                                                   executor.update(dml.getSql());
                                               } catch (Exception e) {
                                                   //回放数据库如果挂掉 则更新task任务状态 停止回放
                                                   fileCanalConnector.updateTaskStatus();
                                                   logger.error(e.getMessage());
                                                   continue my;
                                               } finally {
                                                   try {
                                                       connector.disconnect();
                                                   } catch (Exception e) {
                                                       logger.error(e.getMessage());
                                                   }
                                               }
                                           }
                                       }
                                   }
                                    int num =fileCanalConnector.selectSqlPostion( message.getFileName(), String.valueOf(message.getId()));
                                    if (num>0){
                                        continue;
                                    }
                                    try {
                                        writeOut(message);
                                    }catch (Exception e){
                                        //如果线程池内的线程出现异常 则更新task任务状态 停止回放
                                        fileCanalConnector.updateTaskStatus();
                                        logger.error(e.getMessage());
                                        continue my;
                                    }
                                    fileCanalConnector.insertSqlPosition(message.getFileName(),String.valueOf(message.getId()),0);
                                    tatal++;
                                }
                            }
                        }catch (EOFException e){
                            fileCanalConnector.insertHeartFile(message.getFileName());
                            //每一个binlog执行完，删除canal_file_adapter_postion对应的数据
                            fileCanalConnector.deleteCanalFileAdapterPostion(message.getFileName());
                            //每一个binlog文件回放结束后查看任务最新状态 如果已经停止 则跳到my标识位处
                            String url1= fileCanalConnector.getTask();
                            if (StringUtils.isBlank(url1)){
                                continue my;
                            }
                        }finally {
                            if (is!=null){
                                is.close();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }finally {
                logger.info("此次binlog文件扫描回放结束,耗时:{}",System.currentTimeMillis()-start);
            }
        }
    }
}
