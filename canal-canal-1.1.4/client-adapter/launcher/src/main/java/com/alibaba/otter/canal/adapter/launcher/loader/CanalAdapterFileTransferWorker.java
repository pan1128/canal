package com.alibaba.otter.canal.adapter.launcher.loader;


import com.alibaba.otter.canal.client.adapter.OuterAdapter;
import com.alibaba.otter.canal.client.adapter.support.CanalClientConfig;
import com.alibaba.otter.canal.client.file.CanalFileAdapterPostionDto;
import com.alibaba.otter.canal.client.file.FileCanalConnector;
import com.alibaba.otter.canal.protocol.Message;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
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
                    List<List<Message>> messageFileList =Lists.newArrayList();
                    int tatal =0;
                    for (File binLogFile:binLogFiles){
                        Message message = null;
                        ObjectInputStream is=null;
                        List<Message> messageListCallAble=Lists.newArrayList();
                        is=new ObjectInputStream(new FileInputStream(binLogFile));
                        try {
                            while ((message=(Message)is.readObject())!=null){
                                message.setFileName(binLogFile.getName());
                                if (message.getId()!=-1&&message.getEntries().size()!=0){
                                    messageListCallAble.add(message);
                                    tatal++;
                                }
                            }
                        }catch (EOFException e){
                            //message 读取结束 暂不做处理
                        }finally {
                            if (is!=null){
                                is.close();
                            }
                        }
                        messageFileList.add(messageListCallAble);
                    }
                    fileCanalConnector.init();
                    int runndeTatal =0;
                    /*for (List<Message> messageList:messageFileList){
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
                    List<Message> messageTotalList =Lists.newArrayList();
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

                    }
                    writeOutByMessageList(messageTotalList);
                    if (runndeTatal!=tatal){//说明binlog回放没有结束
                    }


                }
            } catch (Exception e) {
                logger.error(e.getMessage());
                e.printStackTrace();
            }finally {
            }
        }
    }
}
