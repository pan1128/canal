package com.alibaba.otter.canal.admin.controller;


import com.alibaba.otter.canal.admin.common.eum.CanalDbsyncStatus;
import com.alibaba.otter.canal.admin.model.*;
import com.alibaba.otter.canal.admin.service.CanalDbsyncService;
import com.alibaba.otter.canal.admin.service.CanalHeartService;
import com.alibaba.otter.canal.admin.service.impl.CanalHeartServiceImpl;
import com.alibaba.otter.canal.protocol.SecurityUtil;
import com.google.common.collect.Lists;
import io.ebean.Query;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.Resource;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 三区admin接口
 * @author bpyin
 */
@RestController
@RequestMapping(value = "/api/v1/dbsync")
@Api(tags = "三区接口")
public class CanalDbsyncController {
    private static final Logger LOGGER= LoggerFactory.getLogger(CanalDbsyncController.class);

    @Lazy
    @Resource(type = CanalHeartServiceImpl.class, name = "canalHeartServiceImpl")
    private CanalHeartService canalHeartService;

    @Autowired
    private CanalDbsyncService canalDbsyncService;

    @Value("${rdb.location:}")
    private String rdbLocation;
    @Value("${applicationYml.location:}")
    private String applicationYmlLocation;

    @Value("${binLogFile.location}")
    private String binLogFileLocation;

    @Resource
    private RestTemplate restTemplate;

    private static String FILE_CONTENT_SPLIT_MARK="\r\n";

    /**
     * adapter适配器 rest服务地址
     */
    private static String adapterServiceUrl="http://localhost:8081";


    @ApiOperation("新增三区任务")
    @PostMapping("/addTask")
    public  BaseModel<String> addTask(@RequestBody Task task){
        File file=new File(task.getFileUrl());
        if(!file.exists()){
            BaseModel<String> baseModel=new BaseModel<>();
            baseModel.setCode(49999);
            baseModel.setMessage("文件路径不存在！");
            baseModel.setData("新增任务失败");
            return baseModel;
        }

        task.setModifiedTime(new Date());
        task.setStatus("0");//未启用
        task.save();


        /*List<String> list = restTemplate.postForObject(adapterServiceUrl+"/getDataBase?url="+task.getFileUrl()+"&id="+task.getMysqlGroupId(), null, List.class);
        List<CanalAdapterConfig> canalAdapterConfigList = CanalAdapterConfig.find.query().findList();
        //先清除数据库中的rdb文件
        canalAdapterConfigList.stream().forEach(canalAdapterConfig -> {
            canalAdapterConfig.delete();
        });

        list.stream().forEach(dataBase ->{
            RdbConfig rdbConfig =new RdbConfig();
            rdbConfig.setGroupId("g1");
            rdbConfig.setDestination("example");
            rdbConfig.setDbMappingDatabase(dataBase);
            rdbConfig.setOuterAdapterKey("mysql");

            String rdbConfigString=generateRdbConfigString(rdbConfig);
            CanalAdapterConfig canalAdapterConfig=new CanalAdapterConfig();
            canalAdapterConfig.setCategory("rdb");
            canalAdapterConfig.setContent(rdbConfigString);
            canalAdapterConfig.setName(rdbConfig.getDbMappingDatabase()+"-"+rdbConfig.getOuterAdapterKey()+".yml");
            canalAdapterConfig.setStatus("0");//0 未删除  1已删除
            canalAdapterConfig.save();
        });*/

        /*MysqlGroup mysqlGroup=MysqlGroup.find.byId(task.getMysqlGroupId());
        mysqlGroup.setIsUsed("1");
        OuterAdaptersConfig outerAdaptersConfig=new OuterAdaptersConfig();
        //设置主库 mysql jdbc链接地址
        outerAdaptersConfig.setUrl("jdbc:mysql://"+mysqlGroup.getMasterUrl()+"/mysql?useUnicode=true&useSSL=false");
        outerAdaptersConfig.setUsername(mysqlGroup.getUsername());
        outerAdaptersConfig.setPassword(mysqlGroup.getPassword());
        outerAdaptersConfig.setKey("mysql");//默认适配kek为mysql
        outerAdaptersConfig.setName("rdb");
        outerAdaptersConfig.setModifiedTime(new Date());
        String configString=generateApplicationDataBaseConfigString(outerAdaptersConfig);
        outerAdaptersConfig.setContent(configString);
        // canalDbsyncService.saveOuterAdaptersConfig(outerAdaptersConfig);
        CanalConfig canalConfig = CanalConfig.find.byId(2l);
        canalConfig.setContent(canalConfig.getContent()+FILE_CONTENT_SPLIT_MARK+configString);
        String contentMd5 = SecurityUtil.md5String(canalConfig.getContent());
        canalConfig.setContentMd5(contentMd5);
        canalConfig.setModifiedTime(new Date());
        canalConfig.update("modifiedTime", "content", "contentMd5");
        mysqlGroup.update();*/
        return BaseModel.getInstance("新增三区任务成功");
    }

    @ApiOperation("删除三区任务")
    @GetMapping("/deleteTask")
    public BaseModel<String> deleteTask(Task task){
        task.delete();
        return BaseModel.getInstance("删除三区任务成功");
    }

    @ApiOperation("更新三区任务")
    @PostMapping("/updateTask")
    public BaseModel<String> updateTask(@RequestBody Task task ){
        task.setModifiedTime(new Date());
        task.setStatus("0");//未启用
        task.update();
        return BaseModel.getInstance("更新三区任务成功");
    }

    @ApiOperation("启动或者停止三区任务")
    @GetMapping("/TurnOnOrOff/{id}/{status}")
    public BaseModel<String> TurnOnOrOff(@PathVariable String status, @PathVariable Long id) throws Exception{
        Task task=Task.find.byId(id);
        task.setModifiedTime(new Date());
        if ("on".equals(status)){
            task.setStatus("1");//启动
            task.setStartTime(new Date());

            /*List<String> list = restTemplate.postForObject(adapterServiceUrl+"/getDataBase?url="+task.getFileUrl()+"&id="+task.getMysqlGroupId(), null, List.class);
            List<CanalAdapterConfig> canalAdapterConfigList = CanalAdapterConfig.find.query().findList();
            //先清除数据库中的rdb文件
            canalAdapterConfigList.stream().forEach(canalAdapterConfig -> {
                canalAdapterConfig.delete();
            });

            list.stream().forEach(dataBase ->{
                RdbConfig rdbConfig =new RdbConfig();
                rdbConfig.setGroupId("g1");
                rdbConfig.setDestination("example");
                rdbConfig.setDbMappingDatabase(dataBase);
                rdbConfig.setOuterAdapterKey("mysql");

                String rdbConfigString=generateRdbConfigString(rdbConfig);
                CanalAdapterConfig canalAdapterConfig=new CanalAdapterConfig();
                canalAdapterConfig.setCategory("rdb");
                canalAdapterConfig.setContent(rdbConfigString);
                canalAdapterConfig.setName(rdbConfig.getDbMappingDatabase()+"-"+rdbConfig.getOuterAdapterKey()+".yml");
                canalAdapterConfig.setStatus("0");//0 未删除  1已删除
                canalAdapterConfig.save();
            });*/
        }else if("off".equals(status)){
            task.setStatus("0");//停止
            task.setStartTime(null);
        }
        Thread.sleep(500);
        task.update("status","modifiedTime","startTime");
        return BaseModel.getInstance("任务启动或停止成功");
    }

    @ApiOperation("三区任务详情")
    @GetMapping("/getTaskDetail/{id}")
    public BaseModel<Map<String, Object>> getTaskDetail(@PathVariable Long id){
        Task task=Task.find.byId(id);
        MysqlGroup mysqlGroup=null;
        if (task!=null){
             mysqlGroup=MysqlGroup.find.byId(task.getMysqlGroupId());
        }
        HashMap map=new HashMap<String, Object>();
        map.put("task",task);
        map.put("mysqlGroup",mysqlGroup);
        return BaseModel.getInstance(map,"获取三区任务详情成功");
    }

    @ApiOperation("获取任务列表")
    @GetMapping("/getTaskList")
    public BaseModel<Pager<Task>> getTaskList(Pager<Task> pager,Task task){
        Query<Task> query = Task.find.query();
        if (StringUtils.isNotEmpty(task.getName())) {
            query.where().like("name", "%" + task.getName() + "%");
        }
        List<Task> nodeServers = query.order()
                .desc("id")
                .setFirstRow(pager.getOffset().intValue())
                .setMaxRows(pager.getSize())
                .findList();
        nodeServers.stream().forEach(item->{
            File file = new File(item.getFileUrl());
            File[] binLogFiles = file.listFiles();
            item.setFileTotal(String.valueOf(binLogFiles.length));
            if (!"0".equals(item.getFileTotal())){
                BigDecimal tatal = new BigDecimal(item.getFileTotal());
                //获取已完成的binlog文件数
                int runOverNum =0;
                for (File logFile : binLogFiles) {
                    List<CanalHeart> list = CanalHeart.find.query().where().eq("fileName", logFile.getName()).
                            orderBy().desc("lastRunTime").findList();

                    if (list!=null&&list.size()>0){
                        /*CanalHeart canalHeart = list.get(0);
                        int c=(int)(System.currentTimeMillis()-canalHeart.getLastRunTime().getTime())/1000;
                        if (c>2){//两秒没心跳 默认 文件回放结束*/
                        runOverNum++;
                        //}
                    }

                }
                BigDecimal over = new BigDecimal(runOverNum);
                int i = over.divide(tatal, 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).intValue();
                item.setProgress(i);
                item.setFileOverTotal(String.valueOf(runOverNum));
                item.setFileNotOverTatal(String.valueOf(binLogFiles.length-runOverNum));
            }
        });

        Query<Task> queryCnt = query.copy();
        int count = queryCnt.findCount();
        pager.setCount((long) count);
        pager.setItems(nodeServers);
        return BaseModel.getInstance(pager,"获取三区任务列表成功");
    }
    /**
     * 获取当前sync任务执行情况
     * @param id
     * @return
     */
    @GetMapping(value = "/status")
    @ApiOperation("获取当前sync任务执行情况")
    public BaseModel<CanalDbsync> getDbsyncStatus(String id) {
        CanalDbsync canalDbsync=new CanalDbsync();
        canalDbsync.setInstance("example");
        canalDbsync.setProgress(0);
        try {
            String flag = restTemplate.postForObject(adapterServiceUrl+"/getRunningflag", null, String.class);
            if ("1".equals(flag)) {
                canalDbsync.setStatus(CanalDbsyncStatus.running.getName());
                canalDbsync.setStatusCode(CanalDbsyncStatus.running.getCode());
            }else if("2".equals(flag)){
                canalDbsync.setStatus(CanalDbsyncStatus.dead.getName());
                canalDbsync.setStatusCode(CanalDbsyncStatus.dead.getCode());
            }
            canalDbsync.setFileOverTotal("0");
            if ("1".equals(flag)){
                canalDbsync.setProgress(100);
                canalDbsync.setFileOverTotal("1");
            }
        }catch (Exception e){
            canalDbsync.setStatus(CanalDbsyncStatus.dead.getName());
            canalDbsync.setStatusCode(CanalDbsyncStatus.dead.getCode());
            LOGGER.error(e.getMessage());
        }
        File file = new File(binLogFileLocation);
        File[] binLogFiles = file.listFiles();
        if (binLogFiles==null||binLogFiles.length==0){
            canalDbsync.setFileOverTotal("0");
            canalDbsync.setFileTotal("0");
        }else {
            //canalDbsync.setFileOverTotal("0");
            canalDbsync.setFileTotal(binLogFiles.length+"");
        }


        canalDbsync.setStartTime(new Date());
        return BaseModel.getInstance(canalDbsync,"获取当前sync任务执行情况成功");
    }


    @ApiOperation("获取binlog文件列表")
    @GetMapping("/getFileList/{id}")
    public BaseModel<List<BinLogFile>> getFileList(@PathVariable Long id,String name){//
        Task task=Task.find.byId(id);
        if (task==null){
            return BaseModel.getInstance(Lists.newArrayList());
        }
        File file = new File(task.getFileUrl());
        if (!file.exists()){
            return BaseModel.getInstance(Lists.newArrayList());
        }
        File[] binLogFiles = file.listFiles();
        List<BinLogFile> binLogFileList=Lists.newArrayList();

        for (File binLogFile : binLogFiles) {
            BinLogFile logFile=new BinLogFile();
            logFile.setFileName(binLogFile.getName());
            logFile.setUpdateTime(new Date(binLogFile.lastModified()));
            logFile.setStatus("0");//回放未结束
            try{
                /*String flag = restTemplate.postForObject(adapterServiceUrl+"/getRunningflag", null, String.class);
                if ("1".equals(flag)){
                    logFile.setStatus("1");//回放结束
                }*/
                List<CanalHeart> list = CanalHeart.find.query().where().eq("fileName", binLogFile.getName()).
                        orderBy().desc("lastRunTime").findList();
                if (list!=null&&list.size()>0){
                    CanalHeart canalHeart = list.get(0);
                    int c=(int)(System.currentTimeMillis()-canalHeart.getLastRunTime().getTime())/1000;
                    if (c>2){//两秒没心跳 默认 文件回放结束
                        logFile.setStatus("1");//回放结束
                        logFile.setEndTime(canalHeart.getLastRunTime());
                    }
                }

            }catch (Exception e){
                e.printStackTrace();
            }
            binLogFileList.add(logFile);
        }
        if (StringUtils.isNotBlank(name)){
            binLogFileList = binLogFileList.stream().filter(item -> item.getFileName().contains(name)).collect(Collectors.toList());
        }
        return BaseModel.getInstance(binLogFileList);
    }

    public static void main(String[] args) {
        List<String> list = Arrays.asList(new String[]{"ad", "ac", "bc", "bc"});
        List<String> a = list.stream().filter(item -> item.contains("a")).collect(Collectors.toList());

        System.out.println(list);
        System.out.println(a);
    }

    @GetMapping("/createRdbConfig")
    @ApiOperation("根据binlog文件生成rdb配置文件")
    public BaseModel<String> createRdbConfig(){
        List<String> list = restTemplate.postForObject(adapterServiceUrl+"/getDataBase", null, List.class);

        List<CanalAdapterConfig> canalAdapterConfigList = CanalAdapterConfig.find.query().findList();

        //先清除数据库中的rdb文件
        canalAdapterConfigList.stream().forEach(canalAdapterConfig -> {
            canalAdapterConfig.delete();
        });

        list.stream().forEach(dataBase ->{
            RdbConfig rdbConfig =new RdbConfig();
            rdbConfig.setGroupId("g1");
            rdbConfig.setDestination("example");
            rdbConfig.setDbMappingDatabase(dataBase);
            rdbConfig.setOuterAdapterKey("mysql");

            String rdbConfigString=generateRdbConfigString(rdbConfig);
            CanalAdapterConfig canalAdapterConfig=new CanalAdapterConfig();
            canalAdapterConfig.setCategory("rdb");
            canalAdapterConfig.setContent(rdbConfigString);
            canalAdapterConfig.setName(rdbConfig.getDbMappingDatabase()+"-"+rdbConfig.getOuterAdapterKey()+".yml");
            canalAdapterConfig.setStatus("0");//0 未删除  1已删除
            canalAdapterConfig.save();
        });

        return BaseModel.getInstance("生成rdb配置文件成功");
    }


    /**
     * 获取运行日志
     * @return
     */
    @ApiOperation("获取运行日志")
    @GetMapping(value = "/getLog")
    public BaseModel<String> getLog()  {
        try{
            String logContent=canalHeartService.getLog();
            return BaseModel.getInstance(logContent,"获取日志成功");
        }catch (Exception e){
            LOGGER.error(e.getMessage());
            return BaseModel.getInstance("暂无日志");
        }
    }

    /**
     * 添加rdb配置文件
     * @return
     */
    @PostMapping(value = "/addRdbConfig")
    @ApiOperation("添加rdb配置文件")
    public BaseModel<String> addRdbConfig(@RequestBody  RdbConfig rdbConfig){
        String rdbConfigString=generateRdbConfigString(rdbConfig);
        try {
            CanalAdapterConfig canalAdapterConfig=new CanalAdapterConfig();
            canalAdapterConfig.setCategory("rdb");
            canalAdapterConfig.setContent(rdbConfigString);
            canalAdapterConfig.setName(rdbConfig.getDbMappingDatabase()+"-"+rdbConfig.getOuterAdapterKey()+".yml");
            canalAdapterConfig.setStatus("0");//0 未删除  1已删除
            canalAdapterConfig.save();
           /* File file=new File(rdbLocation+File.separator+rdbConfig.getDbMappingDatabase()+".yml");
            FileOutputStream os = new FileOutputStream(file);
            os.write(rdbConfigString.getBytes("UTF-8"));
            os.flush();
            os.close();*/
            return BaseModel.getInstance("添加rdb配置文件成功！");
        }catch (Exception e){
            LOGGER.error(e.getMessage());
            return  BaseModel.getInstance("添加rdb配置文件失败！");
        }
    }

    /**
     * adapter配置文件列表
     * @param pager
     * @return
     */
    @ApiOperation("adapter配置文件列表")
    @GetMapping(value = "/selectAll")
    public BaseModel<Pager<CanalAdapterConfig>> selectAll(Pager<CanalAdapterConfig> pager,String name){
        return BaseModel.getInstance(canalDbsyncService.findList(pager,name));
    }

    /**
     * 根据id获得adapter配置文件详情
     * @param canalAdapterConfig
     * @return
     */
    @ApiOperation("根据id获得adapter配置文件详情")
    @GetMapping(value = "/selectOneById")
    public BaseModel<Map<String, Object>> selectOne(CanalAdapterConfig canalAdapterConfig){
        CanalAdapterConfig adapterConfig = CanalAdapterConfig.find.byId(canalAdapterConfig.getId());
        Yaml yaml = new Yaml();
        Map<String, Object> load = (Map<String, Object>)yaml.load(adapterConfig.getContent());
        RdbConfig rdbConfig=new RdbConfig();
        rdbConfig.setDestination((String) load.get("destination"));
        rdbConfig.setOuterAdapterKey((String)load.get("outerAdapterKey"));
        rdbConfig.setGroupId((String)load.get("groupId"));
        LinkedHashMap<String, Object> dbMapping = (LinkedHashMap<String, Object>)load.get("dbMapping");
        rdbConfig.setDbMappingDatabase((String)dbMapping.get("database"));
        HashMap<String, Object> result=new HashMap<>();
        result.put("adapterConfig",adapterConfig);
        result.put("rdbConfig",rdbConfig);
        return BaseModel.getInstance(result);
    }

    @ApiOperation("获取keys")
    @GetMapping(value = "/getkeys")
    public BaseModel<List<String>> getKeys(){
        CanalConfig canalConfig = CanalConfig.find.byId(2l);
        try {
            Yaml yaml = new Yaml();
            Map<String, Object> map = (Map<String, Object>) yaml.load(canalConfig.getContent());
            LinkedHashMap<String, Object> canalAdapters = ((ArrayList<LinkedHashMap<String, Object>>) ((LinkedHashMap<String, Object>) map.get("canal.conf")).get("canalAdapters")).get(0);
            LinkedHashMap<String, Object> groups = (LinkedHashMap<String, Object>) ((ArrayList<Object>) canalAdapters.get("groups")).get(0);
            ArrayList<LinkedHashMap<String, Object>> outerAdapters = (ArrayList<LinkedHashMap<String, Object>>) groups.get("outerAdapters");
            ArrayList<String> list=new ArrayList<>();
            for (LinkedHashMap linkedHashMap:outerAdapters){
                list.add((String)linkedHashMap.get("key"));
            }
            if(list.size()!=0){
                //剔除已选择的key
                List<CanalAdapterConfig> configList = CanalAdapterConfig.find.query().findList();
                ArrayList<String> existsList=new ArrayList<>();
                for (CanalAdapterConfig adapterConfig:configList){
                    Yaml y = new Yaml();
                    Map<String, Object> map1 = (Map<String, Object>)y.load(adapterConfig.getContent());
                    String outerAdapterKey = (String)map1.get("outerAdapterKey");
                    existsList.add(outerAdapterKey);
                }
                Iterator iterator=list.iterator();
                for (String s:existsList){
                    while (iterator.hasNext()){
                        if (s.equals(iterator.next())){
                            iterator.remove();
                        }
                    }
                }
            }
            return BaseModel.getInstance(list);
        }catch (Exception e){
            LOGGER.error("application.yml文件内容：{}",canalConfig.getContent());
            LOGGER.error(e.getMessage());
        }
        return BaseModel.getInstance(Lists.newArrayList());
    }

    @ApiOperation("获取全局配置列表")
    @GetMapping("/getGlobalConfigList")
    public BaseModel<List<OuterAdaptersConfig>> getGlobalConfigList(){
        List<OuterAdaptersConfig> outerAdaptersConfigList = Lists.newArrayList();
        CanalConfig canalConfig = CanalConfig.find.byId(2l);
        Yaml yaml = new Yaml();
        Map<String, Object> map = (Map<String, Object>) yaml.load(canalConfig.getContent());
        LinkedHashMap<String, Object> canalAdapters = ((ArrayList<LinkedHashMap<String, Object>>) ((LinkedHashMap<String, Object>) map.get("canal.conf")).get("canalAdapters")).get(0);
        LinkedHashMap<String, Object> groups = (LinkedHashMap<String, Object>) ((ArrayList<Object>) canalAdapters.get("groups")).get(0);
        ArrayList<LinkedHashMap<String, Object>> outerAdapters = (ArrayList<LinkedHashMap<String, Object>>) groups.get("outerAdapters");
        for (LinkedHashMap linkedHashMap:outerAdapters){
            OuterAdaptersConfig outerAdaptersConfig=new OuterAdaptersConfig();
            outerAdaptersConfig.setName((String)linkedHashMap.get("name"));
            outerAdaptersConfig.setKey((String)linkedHashMap.get("key"));
            LinkedHashMap<String, String> pro =(LinkedHashMap<String, String>) linkedHashMap.get("properties");
            if (pro==null){
                continue;
            }
            outerAdaptersConfig.setUrl(pro.get("jdbc.url"));
            outerAdaptersConfig.setPassword(pro.get("jdbc.password"));
            outerAdaptersConfig.setUsername(pro.get("jdbc.username"));
            outerAdaptersConfigList.add(outerAdaptersConfig);
        }
        return BaseModel.getInstance(outerAdaptersConfigList);
    }

    /**
     * 更新adapter配置文件
     * @param rdbConfig
     * @param canalAdapterConfig
     * @return
     */
    @ApiOperation("更新adapter配置文件")
    @PostMapping(value = "/update")
    public BaseModel<String> update(@RequestBody RdbConfig rdbConfig,CanalAdapterConfig canalAdapterConfig){
        try {
            String rdbConfigString=generateRdbConfigString(rdbConfig);
            canalAdapterConfig.setContent(rdbConfigString);
            canalAdapterConfig.setModifiedTime(new Date());
            canalAdapterConfig.setName(rdbConfig.getDbMappingDatabase()+".yml");
            canalDbsyncService.update(canalAdapterConfig);
            return BaseModel.getInstance("更新成功");
        }catch (Exception e){
            LOGGER.error(e.getMessage());
        }
        return BaseModel.getInstance("更新失败");

    }

    /**
     * 删除adapter配置文件
     * @param canalAdapterConfig
     * @return
     */
    @ApiOperation("删除adapter配置文件")
    @PostMapping(value = "/deleteById")
    public BaseModel<String> delete(CanalAdapterConfig canalAdapterConfig){
        canalDbsyncService.delete(canalAdapterConfig);
        return BaseModel.getInstance("删除成功");
    }

    @GetMapping("/insertTest")
    public BaseModel<String> test(){
        OuterAdaptersConfig outerAdaptersConfig=new OuterAdaptersConfig();

        //设置主库 mysql jdbc链接地址
        outerAdaptersConfig.setName("rdb");
        outerAdaptersConfig.setKey("1mysql");//默认适配kek为mysql
        outerAdaptersConfig.setUrl("11");
        outerAdaptersConfig.setUsername("mysgetUsername");
        outerAdaptersConfig.setPassword("mysqlGroupgetPassword");
        String configString=generateApplicationDataBaseConfigString(outerAdaptersConfig);
        //outerAdaptersConfig.setContent(configString);
        outerAdaptersConfig.setModifiedTime(new Date());


        canalDbsyncService.saveOuterAdaptersConfig(outerAdaptersConfig);
        return null;
    }

    /**
     * 追加更新application.yml文件
     * @param mysqlGroupId
     * @return
     */
    @ApiOperation("追加更新application.yml文件")
    @PostMapping(value = "/updateApplicationDateBaseConfig/{mysqlGroupId}")
    public BaseModel<String> updateApplicationDateBaseConfig(@PathVariable Long mysqlGroupId){
        try {

            MysqlGroup mysqlGroup=MysqlGroup.find.byId(mysqlGroupId);
            mysqlGroup.setIsUsed("1");

            OuterAdaptersConfig outerAdaptersConfig=new OuterAdaptersConfig();

            //设置主库 mysql jdbc链接地址
            outerAdaptersConfig.setUrl("jdbc:mysql://"+mysqlGroup.getMasterUrl()+"/mysql?useUnicode=true&useSSL=false");
            outerAdaptersConfig.setUsername(mysqlGroup.getUsername());
            outerAdaptersConfig.setPassword(mysqlGroup.getPassword());
            outerAdaptersConfig.setKey("mysql");//默认适配kek为mysql
            outerAdaptersConfig.setName("rdb");
            outerAdaptersConfig.setModifiedTime(new Date());

            String configString=generateApplicationDataBaseConfigString(outerAdaptersConfig);
            outerAdaptersConfig.setContent(configString);

           // canalDbsyncService.saveOuterAdaptersConfig(outerAdaptersConfig);

            CanalConfig canalConfig = CanalConfig.find.byId(2l);
            canalConfig.setContent(canalConfig.getContent()+FILE_CONTENT_SPLIT_MARK+configString);
            String contentMd5 = SecurityUtil.md5String(canalConfig.getContent());
            canalConfig.setContentMd5(contentMd5);
            canalConfig.setModifiedTime(new Date());
            canalConfig.update("modifiedTime", "content", "contentMd5");

            mysqlGroup.update();
            /*File file=new File(applicationYmlLocation+File.separator+"application.yml");
            FileOutputStream os = new FileOutputStream(file,true);
            os.write(configString.getBytes("UTF-8"));
            os.flush();
            os.close();*/
            return BaseModel.getInstance("更新application.yml文件成功");
        }catch (Exception e){
            LOGGER.error(e.getMessage());
            return  BaseModel.getInstance("更新application.yml文件失败");
        }
    }


    @ApiOperation("")
    @PostMapping("/updateGlobalConfig")
    public BaseModel<String>  updateGlobalConfig(){

        return null;
    }

    @ApiOperation("获取全局配置文件")
    @GetMapping(value = "/getApplicationYml")
    public BaseModel<String> getApplicationYml(){
        try {
            CanalConfig canalConfig = CanalConfig.find.byId(2l);
            return BaseModel.getInstance(canalConfig.getContent());
        }catch (Exception e){
            LOGGER.error(e.getMessage());
        }
        return  BaseModel.getInstance("获取全局配置文件出错");
    }

    private String generateApplicationDataBaseConfigString(OuterAdaptersConfig outerAdaptersConfig) {
        StringBuilder sb = new StringBuilder();
        sb.append(FILE_CONTENT_SPLIT_MARK);
        sb.append("      - name: ").append(outerAdaptersConfig.getName()+FILE_CONTENT_SPLIT_MARK);
        sb.append("        key: ").append(outerAdaptersConfig.getKey()+FILE_CONTENT_SPLIT_MARK);
        sb.append("        properties:").append(FILE_CONTENT_SPLIT_MARK);
        sb.append("          jdbc.driverClassName: ").append("com.mysql.jdbc.Driver"+FILE_CONTENT_SPLIT_MARK);
        //sb.append("          jdbc.driverClassName: ").append(outerAdaptersConfig.getDriverClassName()+FILE_CONTENT_SPLIT_MARK);
        sb.append("          jdbc.url: ").append(outerAdaptersConfig.getUrl()+FILE_CONTENT_SPLIT_MARK);
        sb.append("          jdbc.username: ").append(outerAdaptersConfig.getUsername()+FILE_CONTENT_SPLIT_MARK);
        sb.append("          jdbc.password: ").append(outerAdaptersConfig.getPassword()+FILE_CONTENT_SPLIT_MARK);
        return sb.toString();
    }
    private String generateRdbConfigString(RdbConfig rdbConfig) {
        StringBuilder sb = new StringBuilder();
        sb.append("dataSourceKey: ").append("defaultDS"+FILE_CONTENT_SPLIT_MARK);
        //sb.append("destination: ").append("example"+FILE_CONTENT_SPLIT_MARK);
        sb.append("destination: ").append(rdbConfig.getDestination()==null||"".equals(rdbConfig.getDestination())?"example":rdbConfig.getDestination()+FILE_CONTENT_SPLIT_MARK);
        sb.append("groupId: ").append(rdbConfig.getGroupId()+FILE_CONTENT_SPLIT_MARK);
        sb.append("outerAdapterKey: ").append(rdbConfig.getOuterAdapterKey()+FILE_CONTENT_SPLIT_MARK);
        sb.append("concurrent: ").append(Boolean.TRUE+FILE_CONTENT_SPLIT_MARK);
        sb.append("dbMapping: ").append(FILE_CONTENT_SPLIT_MARK);
        sb.append("  mirrorDb: ").append(Boolean.TRUE+FILE_CONTENT_SPLIT_MARK);
        sb.append("  database: ").append(rdbConfig.getDbMappingDatabase()+FILE_CONTENT_SPLIT_MARK);
        return sb.toString();
    }

    /**
     * 启动或者停止
     * @return
     */
    @ApiOperation("启动或者停止")
    @GetMapping(value = "/startOrStop/{status}")
    public BaseModel<String> start(@PathVariable String status){
        String result="";
        String message="";
        if ("off".equals(status)){
            result = restTemplate.postForObject(adapterServiceUrl+"/startOrStop/off", null, String.class);
            message="关闭成功";
        }else {
            result = restTemplate.postForObject(adapterServiceUrl+"/startOrStop/on", null, String.class);
            message="启动成功";
        }
        return BaseModel.getInstance(message);
    }

    @ApiOperation("删除三区mysql集群")
    @DeleteMapping("/deleteMysqlGroupById/{id}")
    public BaseModel<String> deleteGroupById(@PathVariable Long id){
        MysqlGroup mysqlGroup=MysqlGroup.find.byId(id);
        if (mysqlGroup!=null){
            CanalConfig canalConfig = CanalConfig.find.byId(2l);

            OuterAdaptersConfig outerAdaptersConfig=new OuterAdaptersConfig();
            //设置主库 mysql jdbc链接地址
            outerAdaptersConfig.setUrl("jdbc:mysql://"+mysqlGroup.getMasterUrl()+"/mysql?useUnicode=true&useSSL=false");
            outerAdaptersConfig.setUsername(mysqlGroup.getUsername());
            outerAdaptersConfig.setPassword(mysqlGroup.getPassword());
            outerAdaptersConfig.setKey("mysql");//默认适配kek为mysql
            outerAdaptersConfig.setName("rdb");
            outerAdaptersConfig.setModifiedTime(new Date());
            String configString=generateApplicationDataBaseConfigString(outerAdaptersConfig);
            canalConfig.setContent(canalConfig.getContent().replace(configString,""));
            canalConfig.update("content");

            mysqlGroup.delete();
        }



        return BaseModel.getInstance("删除集群成功");
    }

    @ApiOperation("根据id获取mysql集群详情")
    @GetMapping("/getMysqlGroupDetailById/{id}")
    public BaseModel<MysqlGroup> getMysqlGroupDetailById(@PathVariable  Long id){

        MysqlGroup mysqlGroup=MysqlGroup.find.byId(id);
        if (mysqlGroup!=null){
            return BaseModel.getInstance(mysqlGroup);
        }
        return BaseModel.getInstance(null);
    }

    @ApiOperation("添加mysql集群")
    @PostMapping(value = "/addMysqlGroup")
    public BaseModel<String> addMysqlGroup(@RequestBody MysqlGroup mysqlGroup) throws Exception{

        List<MysqlGroup> list=MysqlGroup.find.query().where().eq("name",mysqlGroup.getName()).findList();
        if (list!=null&list.size()>0){
            BaseModel<String> reslut=new BaseModel<>();
            reslut.setCode(500);
            reslut.setMessage("集群名称已存在，请修改！");
            return reslut;
        }
        mysqlGroup.setIsUsed("0");//未被全局配置关联
        //if ("1".equals(mysqlGroup.getIsUsed())){
            mysqlGroup.setIsUsed(mysqlGroup.getIsUsed());
            OuterAdaptersConfig outerAdaptersConfig=new OuterAdaptersConfig();
            //设置主库 mysql jdbc链接地址
            outerAdaptersConfig.setUrl("jdbc:mysql://"+mysqlGroup.getMasterUrl()+"/mysql?useUnicode=true&useSSL=false");
            outerAdaptersConfig.setUsername(mysqlGroup.getUsername());
            outerAdaptersConfig.setPassword(mysqlGroup.getPassword());
            outerAdaptersConfig.setKey("mysql");//默认适配kek为mysql
            outerAdaptersConfig.setName("rdb");
            outerAdaptersConfig.setModifiedTime(new Date());
            String configString=generateApplicationDataBaseConfigString(outerAdaptersConfig);
            outerAdaptersConfig.setContent(configString);
            // canalDbsyncService.saveOuterAdaptersConfig(outerAdaptersConfig);
            CanalConfig canalConfig = CanalConfig.find.byId(2l);
            canalConfig.setContent(canalConfig.getContent()+FILE_CONTENT_SPLIT_MARK+configString);
            String contentMd5 = SecurityUtil.md5String(canalConfig.getContent());
            canalConfig.setContentMd5(contentMd5);
            canalConfig.setModifiedTime(new Date());
            canalConfig.update("modifiedTime", "content", "contentMd5");
        //}

        mysqlGroup.setContent(configString);
        mysqlGroup.save();
        return BaseModel.getInstance("新增mysql集群成功");
    }

    @ApiOperation("更新mysql集群信息")
    @PostMapping("/updateMysqlGroup")
    public BaseModel<String> updateMysqlGroup(@RequestBody MysqlGroup mysqlGroup){
        List<MysqlGroup> list=MysqlGroup.find.query().where().eq("name",mysqlGroup.getName()).ne("id",mysqlGroup.getId()).findList();
        if (list!=null&list.size()>0){
            BaseModel<String> reslut=new BaseModel<>();
            reslut.setCode(500);
            reslut.setMessage("集群名称已存在，请修改！");
            return reslut;
        }
        mysqlGroup.setModifiedTime(new Date());
        if ("1".equals(mysqlGroup.getIsUsed())){//如果将未设置的集群设置为监控状态 则需要对全局配置进行修改 且将其他集群的监控状态设为未监控 确保同一时刻只监控一个集群
            List<MysqlGroup> mysqlGroupList = MysqlGroup.find.query().where().eq("isUsed","1").findList();//已启用
            mysqlGroupList.stream().forEach(item -> {
                item.setIsUsed("0");
                item.update("isUsed");
            });
            //修改全局配置
        }

        CanalConfig canalConfig = CanalConfig.find.byId(2l);

        OuterAdaptersConfig outerAdaptersConfig=new OuterAdaptersConfig();
        //设置主库 mysql jdbc链接地址
        outerAdaptersConfig.setUrl("jdbc:mysql://"+mysqlGroup.getMasterUrl()+"/mysql?useUnicode=true&useSSL=false");
        outerAdaptersConfig.setUsername(mysqlGroup.getUsername());
        outerAdaptersConfig.setPassword(mysqlGroup.getPassword());
        outerAdaptersConfig.setKey("mysql");//默认适配kek为mysql
        outerAdaptersConfig.setName("rdb");
        outerAdaptersConfig.setModifiedTime(new Date());
        String configString=generateApplicationDataBaseConfigString(outerAdaptersConfig);


        MysqlGroup detail =MysqlGroup.find.byId(mysqlGroup.getId());

        canalConfig.setContent(canalConfig.getContent().replace(detail.getContent(),""));

        canalConfig.setContent(canalConfig.getContent()+FILE_CONTENT_SPLIT_MARK+configString);


        canalConfig.update("content");


        mysqlGroup.setContent(configString);
        mysqlGroup.update();
        return BaseModel.getInstance("更新mysql集群信息成功");
    }

    /**
     * 查询三区mysql集群列表
     * @return
     */
    @ApiOperation("查询三区mysql集群分页列表")
    @GetMapping("/selectAllMysqlGroup")
    public BaseModel<Pager<MysqlGroup>> selectAllMysqlGroup(Pager<MysqlGroup> pager,String name){
        Query<MysqlGroup> query = MysqlGroup.find.query();
        if (StringUtils.isNotEmpty(name)) {
            query.where().like("name", "%" + name + "%");
        }
        List<MysqlGroup> nodeServers = query.order()
                .desc("id")
                .setFirstRow(pager.getOffset().intValue())
                .setMaxRows(pager.getSize())
                .findList();

        nodeServers.forEach(item->{
            Task task=Task.find.query().where().eq("mysqlGroupId",item.getId()).findOne();
            if (task!=null){
                item.setIsUsed("1");
            }
        });
        Query<MysqlGroup> queryCnt = query.copy();
        int count = queryCnt.findCount();
        pager.setCount((long) count);
        pager.setItems(nodeServers);
        return BaseModel.getInstance(pager);
    }

    @ApiOperation("获取mysql集群下拉列表")
    @GetMapping("/getMysqlGroupList")
    public BaseModel<List<MysqlGroup>> getMysqlGroupList(String id){//任务id
        List<Task> taskList = Task.find.query().findList();
        List<Long> mysqlGroupIdList = taskList.stream().map(Task::getMysqlGroupId).collect(Collectors.toList());
        List<MysqlGroup> list = MysqlGroup.find.query().where().notIn("id",mysqlGroupIdList).findList();
        Task task=null;
        if (StringUtils.isNotBlank(id)){
            task=Task.find.byId(Long.parseLong(id));
            if (task!=null){
                list.add(MysqlGroup.find.byId(task.getMysqlGroupId()));
            }
        }
        return BaseModel.getInstance(list,"获取mysql集群下拉列表成功");
    }

    /**
     * 加载三区配置文件模板
     * @return
     */
    /*@GetMapping(value = "/{flag}/template")
    public BaseModel<String> template(@PathVariable String flag) {
        BaseModel<String> result=new BaseModel<>();
        try {
            if ("rdb".equals(flag))
                result= BaseModel.getInstance(TemplateConfigLoader.loadRdbConfig());
            else
                result= BaseModel.getInstance(TemplateConfigLoader.loadApplicationYmlConfig());
        }catch (Exception e){
            result=BaseModel.getInstance("");
        }
        return result;
    }*/

    /**
     * 更新application.yml配置文件
     * @param request
     * @return
     */
   /* @RequestMapping(value = "/updateApplicationYmlFile")
    public BaseModel<String> updateApplicationYmlFile(MultipartHttpServletRequest request){
        try {
            Map<String, MultipartFile> fileMap = request.getFileMap();
            if (fileMap.size()<0)
            {
                return BaseModel.getInstance("请上传文件！");
            }
            Collection<MultipartFile> values = fileMap.values();
            MultipartFile multipartFile = values.stream().findFirst().get();
            if (!"application.yml".equals(multipartFile.getOriginalFilename())){
                return BaseModel.getInstance("application.yml配置文件名有误！");
            }
            InputStream inputStream = multipartFile.getInputStream();
            File file=new File(applicationYmlLocation+File.separator+multipartFile.getOriginalFilename());
            FileOutputStream os = new FileOutputStream(file);
            byte[] bytes=new byte[multipartFile.getBytes().length];
            int read = inputStream.read(bytes);
            os.write(bytes,0,bytes.length);
            inputStream.close();
            os.flush();
            os.close();
            return BaseModel.getInstance("更新application.yml文件成功");
        }catch (Exception e){
            return  BaseModel.getInstance("更新application.yml文件失败");
        }
    }*/

    /**
     * 添加rdb数据库配置文件
     * @param request
     * @return
     */
 /*   @RequestMapping(value = "/addRdbDataBaseConfig")
    public BaseModel<String> addRdbDataBaseConfig(MultipartHttpServletRequest request){
        try {
            Map<String, MultipartFile> fileMap = request.getFileMap();
            if (fileMap.size()<0)
            {
                return BaseModel.getInstance("请上传文件！");
            }
            Collection<MultipartFile> values = fileMap.values();
            MultipartFile multipartFile = values.stream().findFirst().get();
            InputStream inputStream = multipartFile.getInputStream();
            File file=new File(rdbLocation+File.separator+multipartFile.getOriginalFilename());
            FileOutputStream os = new FileOutputStream(file);
            byte[] bytes=new byte[multipartFile.getBytes().length];
            int read = inputStream.read(bytes);
            os.write(bytes,0,bytes.length);
            inputStream.close();
            os.flush();
            os.close();
            return BaseModel.getInstance("添加rdb配置文件成功！");
        }catch (Exception e){
            return  BaseModel.getInstance("添加rdb配置文件失败！");
        }
    }*/

   /* @PostMapping(consumes = {"application/json;charset=UTF-8"} ,produces = {"application/json;charset=UTF-8"},value = "/getJson")
    public String test(@RequestBody CanalDbsync dbsync, String name,
                       String code){
        System.out.println(name+","+code);
        return name+","+code;
    }*/
    /*@RequestMapping(value = "/createCommonQRCode")
    public void createCommonQRCode(HttpServletResponse response, String url) throws Exception {
        ServletOutputStream stream = null;
        try {
            stream = response.getOutputStream();
            //使用工具类生成二维码
            QRCodeUtil.encode(url, stream);
        } catch (Exception e) {
            e.getStackTrace();
            e.printStackTrace();
        } finally {
            if (stream != null) {
                stream.flush();
                stream.close();
            }
        }
    }*/
    /*@RequestMapping(value = "/createLogoQRCode")
    public void createLogoQRCode(HttpServletResponse response, String url) throws Exception {
        ServletOutputStream stream = null;
        try {
            stream = response.getOutputStream();
            String logoPath = Thread.currentThread().getContextClassLoader().getResource("").getPath()
                    + "public" + File.separator + "logo1.jpg";

            QRCodeUtil.encode(url, logoPath, stream, true);
        } catch (Exception e) {
            e.getStackTrace();
            e.printStackTrace();
        } finally {
            if (stream != null) {
                stream.flush();
                stream.close();
            }
        }
    }*/

}
