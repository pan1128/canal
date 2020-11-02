package com.alibaba.otter.canal.adapter.launcher.rest;

import com.alibaba.otter.canal.adapter.launcher.common.EtlLock;
import com.alibaba.otter.canal.adapter.launcher.common.SyncSwitch;
import com.alibaba.otter.canal.adapter.launcher.config.AdapterCanalConfig;
import com.alibaba.otter.canal.adapter.launcher.loader.CanalAdapterService;
import com.alibaba.otter.canal.adapter.launcher.monitor.remote.MysqlGroup;
import com.alibaba.otter.canal.client.adapter.OuterAdapter;
import com.alibaba.otter.canal.client.adapter.support.*;
import com.alibaba.otter.canal.parse.driver.mysql.MysqlConnector;
import com.alibaba.otter.canal.parse.driver.mysql.MysqlUpdateExecutor;
import com.alibaba.otter.canal.protocol.Message;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 适配器操作Rest
 *
 * @author rewerma @ 2018-10-20
 * @version 1.0.0
 */
@RestController
public class CommonRest {

    private static Logger                 logger           = LoggerFactory.getLogger(CommonRest.class);

    private static final String           ETL_LOCK_ZK_NODE = "/sync-etl/";

    private ExtensionLoader<OuterAdapter> loader;

    @Resource
    private SyncSwitch                    syncSwitch;
    @Resource
    private EtlLock                       etlLock;

    @Resource
    private AdapterCanalConfig            adapterCanalConfig;

    @Autowired
    private CanalAdapterService canalAdapterService;

    @Value("${canal.conf.fileLocation}")
    private String fileLocation;

    @PostConstruct
    public void init() {
        loader = ExtensionLoader.getExtensionLoader(OuterAdapter.class);
    }

    @PostMapping("/getFileLocation")
    public String getFileLocation(){
        return fileLocation;
    }

    /**
     * ETL curl http://127.0.0.1:8081/etl/rdb/oracle1/mytest_user.yml -X POST
     *
     * @param type 类型 hbase, es
     * @param key adapter key
     * @param task 任务名对应配置文件名 mytest_user.yml
     * @param params etl where条件参数, 为空全部导入
     */
    @PostMapping("/etl/{type}/{key}/{task}")
    public EtlResult etl(@PathVariable String type, @PathVariable String key, @PathVariable String task,
                         @RequestParam(name = "params", required = false) String params) {
        OuterAdapter adapter = loader.getExtension(type, key);
        String destination = adapter.getDestination(task);
        String lockKey = destination == null ? task : destination;

        boolean locked = etlLock.tryLock(ETL_LOCK_ZK_NODE + type + "-" + lockKey);
        if (!locked) {
            EtlResult result = new EtlResult();
            result.setSucceeded(false);
            result.setErrorMessage(task + " 有其他进程正在导入中, 请稍后再试");
            return result;
        }
        try {

            boolean oriSwitchStatus;
            if (destination != null) {
                oriSwitchStatus = syncSwitch.status(destination);
                if (oriSwitchStatus) {
                    syncSwitch.off(destination);
                }
            } else {
                // task可能为destination，直接锁task
                oriSwitchStatus = syncSwitch.status(task);
                if (oriSwitchStatus) {
                    syncSwitch.off(task);
                }
            }
            try {
                List<String> paramArray = null;
                if (params != null) {
                    paramArray = Arrays.asList(params.trim().split(";"));
                }
                return adapter.etl(task, paramArray);
            } finally {
                if (destination != null && oriSwitchStatus) {
                    syncSwitch.on(destination);
                } else if (destination == null && oriSwitchStatus) {
                    syncSwitch.on(task);
                }
            }
        } finally {
            etlLock.unlock(ETL_LOCK_ZK_NODE + type + "-" + lockKey);
        }
    }

    /**
     * ETL curl http://127.0.0.1:8081/etl/hbase/mytest_person2.yml -X POST
     *
     * @param type 类型 hbase, es
     * @param task 任务名对应配置文件名 mytest_person2.yml
     * @param params etl where条件参数, 为空全部导入
     */
    @PostMapping("/etl/{type}/{task}")
    public EtlResult etl(@PathVariable String type, @PathVariable String task,
                         @RequestParam(name = "params", required = false) String params) {
        return etl(type, null, task, params);
    }

    /**
     * 统计总数 curl http://127.0.0.1:8081/count/rdb/oracle1/mytest_user.yml
     *
     * @param type 类型 hbase, es
     * @param key adapter key
     * @param task 任务名对应配置文件名 mytest_person2.yml
     * @return
     */
    @GetMapping("/count/{type}/{key}/{task}")
    public Map<String, Object> count(@PathVariable String type, @PathVariable String key, @PathVariable String task) {
        OuterAdapter adapter = loader.getExtension(type, key);
        return adapter.count(task);
    }

    /**
     * 统计总数 curl http://127.0.0.1:8081/count/hbase/mytest_person2.yml
     *
     * @param type 类型 hbase, es
     * @param task 任务名对应配置文件名 mytest_person2.yml
     * @return
     */
    @GetMapping("/count/{type}/{task}")
    public Map<String, Object> count(@PathVariable String type, @PathVariable String task) {
        return count(type, null, task);
    }

    /**
     * 返回所有实例 curl http://127.0.0.1:8081/destinations
     */
    @GetMapping("/destinations")
    public List<Map<String, String>> destinations() {
        List<Map<String, String>> result = new ArrayList<>();
        Set<String> destinations = adapterCanalConfig.DESTINATIONS;
        for (String destination : destinations) {
            Map<String, String> resMap = new LinkedHashMap<>();
            boolean status = syncSwitch.status(destination);
            String resStatus;
            if (status) {
                resStatus = "on";
            } else {
                resStatus = "off";
            }
            resMap.put("destination", destination);
            resMap.put("status", resStatus);
            result.add(resMap);
        }
        return result;
    }

    /**
     * 实例同步开关 curl http://127.0.0.1:8081/syncSwitch/example/off -X PUT
     *
     * @param destination 实例名称
     * @param status 开关状态: off on
     * @return
     */
    @PutMapping("/syncSwitch/{destination}/{status}")
    public Result etl(@PathVariable String destination, @PathVariable String status) {
        if (status.equals("on")) {
            syncSwitch.on(destination);
            logger.info("#Destination: {} sync on", destination);
            return Result.createSuccess("实例: " + destination + " 开启同步成功");
        } else if (status.equals("off")) {
            syncSwitch.off(destination);
            logger.info("#Destination: {} sync off", destination);
            return Result.createSuccess("实例: " + destination + " 关闭同步成功");
        } else {
            Result result = new Result();
            result.setCode(50000);
            result.setMessage("实例: " + destination + " 操作失败");
            return result;
        }
    }

    /**
     * 获取实例开关状态 curl http://127.0.0.1:8081/syncSwitch/example
     *
     * @param destination 实例名称
     * @return
     */
    @GetMapping("/syncSwitch/{destination}")
    public Map<String, String> etl(@PathVariable String destination) {
        boolean status = syncSwitch.status(destination);
        String resStatus;
        if (status) {
            resStatus = "on";
        } else {
            resStatus = "off";
        }
        Map<String, String> res = new LinkedHashMap<>();
        res.put("stauts", resStatus);
        return res;
    }

    /**
     * 关闭或者开启扫描binlog文件线程
     * @param status
     * @return
     */
    @PostMapping(value = "/startOrStop/{status}")
    public Result startOrStopThread(@PathVariable String status){
        Result result=new Result();
        if ("off".equals(status)){
            canalAdapterService.destroy();
            result.setCode(200);
            result.setMessage("关闭成功");
        }else if ("on".equals(status)){
            canalAdapterService.init();
            result.setCode(200);
            result.setMessage("启动成功");
        }
        return result;
    }

    /**
     * 获取适配器线程运行状态
     * @return
     */
    @PostMapping("/getRunningflag")
    public String getRunningflag(){
        Result result=new Result();
        String flag="";
        boolean runFlag = canalAdapterService.getRunFlag();
        result.setCode(2);//已经暂停运行
        flag="2";
        if (runFlag){
            result.setCode(1);//正在运行
            flag="1";
        }
        return flag;
    }

    //获取binglog数据库
    @PostMapping("/getDataBase")
    public List<String> getDatabaseList( String url,Long id) throws Exception{


        List<String> dataBaseList = Lists.newArrayList();
        File file = new File(url);
        if (!file.exists()){
            return dataBaseList;
        }
        File[] binLogFiles = file.listFiles();
        //List<List<Message>> messageFileList =Lists.newArrayList();
        List<Message> messageListCallAble=Lists.newArrayList();
        for (File binLogFile:binLogFiles){
            Message message = null;
            ObjectInputStream is=null;
            is=new ObjectInputStream(new FileInputStream(binLogFile));
            try {
                while ((message=(Message)is.readObject())!=null){
                    message.setFileName(binLogFile.getName());
                    if (message.getId()!=-1&&message.getEntries().size()!=0){
                        //messageListCallAble.add(message);
                        List<Dml> dmlList = new ArrayList<>();
                        List<Dml> dmls = MessageUtil.parse4Dml("example", "g1", message);
                        dmlList.addAll(dmls);
                        dmlList.stream().forEach(item ->{
                            if (!StringUtils.isEmpty(item.getDatabase())){
                                //为空说明没有此数据库则进行数据库创建
                                String dataBase =item.getDatabase().replace("`","");
                                if (!dataBaseList.contains(dataBase)){
                                    dataBaseList.add(dataBase);
                                }
                            }
                            if (item.getSql()!=null&&StringUtils.isEmpty(item.getDatabase())&&item.getSql().contains("CREATE DATABASE")){
                                //进行数据库创建
                                MysqlGroup mysqlGroup=canalAdapterService.getMysqlGroup(id);
                                String address = mysqlGroup.getMasterUrl().substring(0, mysqlGroup.getMasterUrl().indexOf(":"));
                                String port=mysqlGroup.getMasterUrl().substring(mysqlGroup.getMasterUrl().indexOf(":")+1);
                                MysqlConnector connector = new MysqlConnector(new InetSocketAddress(address,Integer.parseInt(port)), mysqlGroup.getUsername(), mysqlGroup.getPassword());
                                try {
                                    connector.connect();
                                    MysqlUpdateExecutor executor = new MysqlUpdateExecutor(connector);
                                    executor.update(item.getSql());
                                } catch (IOException e) {
                                    logger.error(e.getMessage());
                                    //Assert.fail(e.getMessage());
                                } finally {
                                    try {
                                        connector.disconnect();
                                    } catch (IOException e) {
                                        logger.error(e.getMessage());
                                        // Assert.fail(e.getMessage());
                                    }
                                }
                                String dataBase = item.getSql().substring(17);//截取数据库名称
                                dataBase=dataBase.substring(0, dataBase.indexOf("`"));
                                if (!dataBaseList.contains(dataBase)){
                                    dataBaseList.add(dataBase);
                                }
                            }
                        });
                    }
                }
            }catch (EOFException e){
                //message 读取结束 暂不做处理
            }finally {
                if (is!=null){
                    is.close();
                }
            }
        }
        /*List<Dml> dmlList = new ArrayList<>();
        messageListCallAble.forEach(item ->{
            List<Dml> dmls = MessageUtil.parse4Dml("example", "g1", item);
            dmlList.addAll(dmls);
        });
        dmlList.stream().forEach(item ->{
            if (!StringUtils.isEmpty(item.getDatabase())){
                //为空说明没有此数据库则进行数据库创建
                dataBaseList.add(item.getDatabase().replace("`",""));
            }
            if (item.getSql()!=null&&StringUtils.isEmpty(item.getDatabase())&&item.getSql().contains("CREATE DATABASE")){
                //进行数据库创建
                MysqlGroup mysqlGroup=canalAdapterService.getMysqlGroup(id);
                String address = mysqlGroup.getMasterUrl().substring(0, mysqlGroup.getMasterUrl().indexOf(":"));
                String port=mysqlGroup.getMasterUrl().substring(mysqlGroup.getMasterUrl().indexOf(":")+1);
                MysqlConnector connector = new MysqlConnector(new InetSocketAddress(address,Integer.parseInt(port)), mysqlGroup.getUsername(), mysqlGroup.getPassword());
                try {
                    connector.connect();
                    MysqlUpdateExecutor executor = new MysqlUpdateExecutor(connector);
                    executor.update(item.getSql());
                } catch (IOException e) {
                    logger.error(e.getMessage());
                    //Assert.fail(e.getMessage());
                } finally {
                    try {
                        connector.disconnect();
                    } catch (IOException e) {
                        logger.error(e.getMessage());
                       // Assert.fail(e.getMessage());
                    }
                }
                String dataBase = item.getSql().substring(17);//截取数据库名称
                dataBase=dataBase.substring(0, dataBase.indexOf("`"));
                dataBaseList.add(dataBase);
            }
        });*/
        List<String> collect = dataBaseList.stream().distinct().collect(Collectors.toList());
        return collect;
    }

    public static void main(String[] args) {
        /*String string="CREATE DATABASE `lzl_test3` CHARACTER SET 'utf8'";

        String dataBase =string.substring(17);
        String result =dataBase.substring(0,dataBase.indexOf("`"));
        System.out.println(dataBase);
        System.out.println(result);*/

        String s="111.231.110.80:3306";
        String address = s.substring(0, s.indexOf(":"));

        String port=s.substring(s.indexOf(":")+1);

        System.out.println(address);
        System.out.println(port);

    }
}
