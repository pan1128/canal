package com.alibaba.otter.canal.adapter.launcher.loader;


import com.alibaba.otter.canal.adapter.launcher.config.SpringContext;
import com.alibaba.otter.canal.adapter.launcher.monitor.remote.MysqlGroup;
import com.alibaba.otter.canal.client.adapter.OuterAdapter;
import com.alibaba.otter.canal.client.adapter.support.CanalClientConfig;
import com.alibaba.otter.canal.client.adapter.support.Dml;
import com.alibaba.otter.canal.client.adapter.support.MessageUtil;
import com.alibaba.otter.canal.client.file.CanalFileAdapterPostionDto;
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
            try {
                syncSwitch.get(canalDestination);
                while (running){
                    String url= fileCanalConnector.getTask();
                    if (StringUtils.isBlank(url)){
                        continue;
                    }
                    File file = new File(url);
                    File[] binLogFiles = file.listFiles();
                    /*Arrays.sort(binLogFiles, new Comparator<File>() {
                        @Override
                        public int compare(File o1, File o2) {
                            return o1.lastModified()>o2.lastModified()?1:-1;
                        }
                    });*/

                    Arrays.sort(binLogFiles,(o1,o2)-> o1.lastModified()>o2.lastModified()?1:-1);
                    fileCanalConnector.init();
                    //List<List<Message>> messageFileList =Lists.newArrayList();
                    for (File binLogFile:binLogFiles){
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
                                                   fileCanalConnector.insertSqlPosition(message.getFileName(),String.valueOf(message.getId()),p);
                                                   Thread.sleep(10000);
                                               }
                                           }

                                           if (dml.getSql().contains("CREATE DATABASE")||dml.getSql().contains("create database")){
                                               /*String dataBase = dml.getSql().substring(17);//截取数据库名称
                                               dataBase=dataBase.substring(0, dataBase.indexOf("`"));
                                               int num = fileCanalConnector.getConfig(dataBase);*/
                                               //if (num==0){
                                                   //执行创表语句 开始 进行数据库创建
                                                   CanalAdapterService canalAdapterService =(CanalAdapterService)SpringContext.getBean(CanalAdapterService.class);
                                                   MysqlGroup mysqlGroup=canalAdapterService.getMysqlGroupOne();
                                                   String address = mysqlGroup.getMasterUrl().substring(0, mysqlGroup.getMasterUrl().indexOf(":"));
                                                   String port=mysqlGroup.getMasterUrl().substring(mysqlGroup.getMasterUrl().indexOf(":")+1);
                                                   MysqlConnector connector = new MysqlConnector(new InetSocketAddress(address,Integer.parseInt(port)), mysqlGroup.getUsername(), mysqlGroup.getPassword());
                                                   try {
                                                       connector.connect();
                                                       MysqlUpdateExecutor executor = new MysqlUpdateExecutor(connector);
                                                       executor.update(dml.getSql());
                                                       //记录
                                                       //删除配置文件
                                                       //fileCanalConnector.insertConfig(dataBase);
                                                   } catch (IOException e) {
                                                       logger.error(e.getMessage());
                                                   } finally {
                                                       try {
                                                           connector.disconnect();
                                                           //Thread.sleep(10000);
                                                           //return;
                                                       } catch (Exception e) {
                                                           logger.error(e.getMessage());
                                                       }
                                                   }
                                                   //执行创表语句 结束
                                               //}





                                           }




                                       }
                                   }


                                   int num =fileCanalConnector.selectSqlPostion( message.getFileName(), String.valueOf(message.getId()));
                                    writeOut2(message,num);
                                    tatal++;
                                }
                            }
                        }catch (EOFException e){
                            //message 读取结束
                            CanalFileAdapterPostionDto canalFileAdapterPostionDto=new CanalFileAdapterPostionDto(
                                    canalDestination,"g1",message.getFileName(),tatal
                            );
                            fileCanalConnector.insertAck(canalFileAdapterPostionDto);
                            fileCanalConnector.insertHeartFile(message.getFileName());
                        }finally {
                            if (is!=null){
                                is.close();
                            }
                        }
                        //messageFileList.add(messageListCallAble);
                    }
                    /*int runndeTatal =0;
                    for (List<Message> messageList:messageFileList){
                        int num = fileCanalConnector.selectAck(messageList.get(0).getFileName());
                        messageList=messageList.subList(num,messageList.size());
                        if (messageList.size()==0) {
                        } else {
                            runndeTatal=runndeTatal+messageList.size();
                            long begin = System.currentTimeMillis();
                            writeOutByMessageList(messageList);
                            CanalFileAdapterPostionDto canalFileAdapterPostionDto=new CanalFileAdapterPostionDto(
                                    canalDestination,"g1",messageList.get(0).getFileName(),messageList.size()
                            );
                            fileCanalConnector.insertAck(canalFileAdapterPostionDto);

                            //fileCanalConnector.insertHeart(messageList.get(0).getFileName());
                            fileCanalConnector.insertHeartFile(messageList.get(0).getFileName());
                        }
                    }*/
                    /*List<Message> messageTotalList =Lists.newArrayList();
                    for (List<Message> messageList:messageFileList){
                        int num = fileCanalConnector.selectAck(messageList.get(0).getFileName());
                        messageList=messageList.subList(num,messageList.size());
                        if (messageList.size()==0) {
                        } else {
                            runndeTatal=runndeTatal+messageList.size();
                            long begin = System.currentTimeMillis();
//                            writeOutByMessageList(messageList);
                            messageTotalList.addAll(messageList);
                            CanalFileAdapterPostionDto canalFileAdapterPostionDto=new CanalFileAdapterPostionDto(
                                    canalDestination,"g1",messageList.get(0).getFileName(),messageList.size()
                            );
                            fileCanalConnector.insertAck(canalFileAdapterPostionDto);

                            //fileCanalConnector.insertHeart(messageList.get(0).getFileName());
                            fileCanalConnector.insertHeartFile(messageList.get(0).getFileName());
                        }

                    }*/
                    /*writeOutByMessageList(messageTotalList);
                    if (runndeTatal!=tatal){//说明binlog回放没有结束
                    }*/
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }finally {
            }
        }
    }
}
