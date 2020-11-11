package com.alibaba.otter.canal.client.file;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 文件模式canal链接器
 * @Author: Rivan
 * @Date: 2020/9/29 9:17
 */
public class FileCanalConnector  {

    private static final Logger LOGGER= LoggerFactory.getLogger(FileCanalConnector.class);

    private DruidDataSource dataSource;

    private Connection connection;

    private ReentrantLock lock=new ReentrantLock();
    private static String FILE_CONTENT_SPLIT_MARK="\r\n";
    /**
     * 初始化canal-admin数据源
     * @param driverName
     * @param jdbcUrl
     * @param jdbcUsername
     * @param jdbcPassword
     */
    public FileCanalConnector(String driverName, String jdbcUrl, String jdbcUsername, String jdbcPassword){
        dataSource = new DruidDataSource();
        if (StringUtils.isEmpty(driverName)) {
            driverName = "com.mysql.jdbc.Driver";
        }
        dataSource.setDriverClassName(driverName);
        dataSource.setUrl(jdbcUrl);
        dataSource.setUsername(jdbcUsername);
        dataSource.setPassword(jdbcPassword);
        dataSource.setInitialSize(1);
        dataSource.setMinIdle(1);
        dataSource.setMaxActive(1);
        dataSource.setMaxWait(60000);
        dataSource.setTimeBetweenEvictionRunsMillis(60000);
        dataSource.setMinEvictableIdleTimeMillis(300000);
        try {
            dataSource.init();
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void init(){
    }

    /**
     * 获取链接
     * @return
     * @throws SQLException
     */
    public Connection getConnection() throws SQLException {
        lock.lock();
        try {
            if (connection==null||connection.isClosed())
                connection=dataSource.getConnection();
            return connection;
        }finally {
            lock.unlock();
        }
    }

    /**
     * 插入message读取位置
     * @param canalFileAdapterPostionDto
     */
    public void insertAck(CanalFileAdapterPostionDto canalFileAdapterPostionDto)  {

        String sql = " insert into canal_file_adapter_postion(instance,group_id,file_name, message_num)" +
                " values (?,?,?,?) ";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, canalFileAdapterPostionDto.getInstance());
            ps.setString(2, canalFileAdapterPostionDto.getGroupId());
            ps.setString(3, canalFileAdapterPostionDto.getFileName());
            ps.setInt(4, canalFileAdapterPostionDto.getMessageNum());
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }


    }

    /**
     * 获取message读取位置
     * @param fileName
     * @return
     */
    public int selectAck(String fileName) {
        String sql = "SELECT IFNULL(SUM(message_num),0) num FROM canal_file_adapter_postion WHERE file_Name= ? ";
        int num=0;
        try {
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1,fileName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                num= rs.getInt("num");
            }
            stmt.close();
            rs.close();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return num;
    }
    public void destroy(){
        if (dataSource!=null){
            dataSource.close();
        }
    }

    public void insertHeart(int tatal, int runndeTatal) {
        BigDecimal ta = new BigDecimal(runndeTatal);
        BigDecimal run = new BigDecimal(tatal);
        int precent =0;
        if (tatal!=0){
            precent = ta.divide(run, 2, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).intValue();
        }
        boolean flag =false;
        String sql="";
        if (flag){
            sql = " insert into canal_heart(precent,last_run_time)" +
                    " values (?,?) ";
        }else {
            sql=" update canal_heart set precent=?,last_run_time=? LIMIT 1";
        }
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, precent);
            ps.setLong(2, System.currentTimeMillis());
            ps.close();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

    }

    /*private boolean isExist(){
        String sql = "SELECT IFNULL(SUM(message_num),0) num FROM canal_file_adapter_postion WHERE file_Name= ? ";//'"+fileName+"'";
        int num=0;
        try {
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1,fileName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                num= rs.getInt("num");
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return num>0?true:false;
    }*/

    public static void main(String[] args) {
       BigDecimal ta = new BigDecimal(1);
       BigDecimal run = new BigDecimal(3);
        System.out.println(ta.divide(run,2, RoundingMode.HALF_UP).multiply(new BigDecimal(100)).intValue());
        System.out.println(System.currentTimeMillis());
        System.out.println(new Date(System.currentTimeMillis()));
    }

    /**
     * 查询三区任务
     * @Param: []
     * @Return: java.lang.String
     * @Author: Rivan
     * @Date: 2020/11/11 10:33
     */
    public String getTask() {
        String sql = "SELECT file_url FROM task where status ='1' limit 1 ";
        String url=null;
        try {
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                url= rs.getString("file_url");
            }
            stmt.close();
            rs.close();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return url;

    }

    /**
     * binlog文件回放完，插入一条记录
     * @Param: [fileName]
     * @Return: void
     * @Author: Rivan
     * @Date: 2020/11/11 10:33
     */
    public void insertHeartFile(String fileName) {
        String sql = " insert into canal_heart(file_name,last_run_time)" +
                " values (?,?) ";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fileName);
            ps.setTimestamp(2,new Timestamp(new Date().getTime()));
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }


    /**
     * 构造rdb配置文件内容
     * @Param: [dataBase]
     * @Return: java.lang.String
     * @Author: Rivan
     * @Date: 2020/11/11 10:34
     */
    private String generateRdbConfigString(String dataBase) {
        StringBuilder sb = new StringBuilder();
        sb.append("dataSourceKey: ").append("defaultDS"+FILE_CONTENT_SPLIT_MARK);
        sb.append("destination: ").append("example"+FILE_CONTENT_SPLIT_MARK);
        sb.append("groupId: ").append("g1"+FILE_CONTENT_SPLIT_MARK);
        sb.append("outerAdapterKey: ").append("mysql"+FILE_CONTENT_SPLIT_MARK);
        sb.append("concurrent: ").append(Boolean.TRUE+FILE_CONTENT_SPLIT_MARK);
        sb.append("dbMapping: ").append(FILE_CONTENT_SPLIT_MARK);
        sb.append("  mirrorDb: ").append(Boolean.TRUE+FILE_CONTENT_SPLIT_MARK);
        sb.append("  database: ").append(dataBase+FILE_CONTENT_SPLIT_MARK);
        return sb.toString();
    }

    public void deleteConfig(){
        String sql = " delete from canal_adapter_config";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * 根据数据库名称查询是否已经存在配置文件
     * @Param: [name]
     * @Return: int
     * @Author: Rivan
     * @Date: 2020/11/11 10:34
     */
    public int getConfig(String name) {
        String sql = "SELECT IFNULL(count(*),0) num FROM canal_adapter_config WHERE name= ? ";
        int num=0;
        try {
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1,name+".yml");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                num= rs.getInt("num");
            }
            stmt.close();
            rs.close();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return num;
    }

    /**
     * 插入rdb数据库配置文件
     * @Param: [dataBase]
     * @Return: void
     * @Author: Rivan
     * @Date: 2020/11/11 10:35
     */
    public void insertConfig(String dataBase) {
        String sql = " insert into canal_adapter_config(category,name,status,content)" +
                " values (?,?,?,?) ";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "rdb");
            ps.setString(2,dataBase+".yml");
            ps.setString(3,"0");
            ps.setString(4,generateRdbConfigString(dataBase));
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * 查询message是否已经回放
     * @param fileName
     * @param messageUniqeName
     * @return
     */
    public int selectSqlPostion(String fileName, String messageUniqeName) {
        //String sql = "SELECT IFNULL(SUM(message_num),0) num FROM canal_file_adapter_postion WHERE file_Name= ? and group_id= ?";
        String sql = "SELECT IFNULL(count(*),0) num FROM canal_file_adapter_postion WHERE file_Name= ? and group_id= ?";
        int num=0;
        try {
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1,fileName);
            stmt.setString(2,messageUniqeName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                num= rs.getInt("num");
            }
            stmt.close();
            rs.close();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return num;
    }

    /**
     * 查询message是否已经回放
     * @param fileName
     * @param id
     * @return
     */
    public int  isExist(String fileName, String id){
        //String sql = "SELECT IFNULL(SUM(message_num),0) num FROM canal_file_adapter_postion WHERE file_Name= ? and group_id= ?";
        String sql = "SELECT IFNULL(count(*),0) num FROM canal_file_adapter_postion WHERE file_Name= ? and group_id= ?";
        int num=0;
        try {
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1,fileName);
            stmt.setString(2,id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                num= rs.getInt("num");
            }
            stmt.close();
            rs.close();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return num;

    }

    /**
     * 回放完一条message，记录
     * @Param: [fileName, id, p]
     * @Return: void
     * @Author: Rivan
     * @Date: 2020/11/11 10:39
     */
    public void insertSqlPosition(String fileName, String id, int p) {
        int num=isExist(fileName,id);
        if (num>0){
            return;
        }
        String sql = " insert into canal_file_adapter_postion(instance,group_id,file_name, message_num)" +
                " values (?,?,?,?) ";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "example");
            ps.setString(2, id);//利用group id作为message的唯一标识
            ps.setString(3, fileName);
            ps.setInt(4, p);
            ps.executeUpdate();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * 查询已经回放结束的binlog文件名称list
     * @Param: []
     * @Return: java.util.List<java.lang.String>
     * @Author: Rivan
     * @Date: 2020/11/11 10:38
     */
    public List<String> getHeartFileName() {
        String sql = "SELECT DISTINCT file_name  FROM canal_heart ";
        List<String> fileNameList = new ArrayList<>();
        //String fileName = fileNameList.stream().collect(Collectors.joining(", "));
        String fileName ="";
        try {
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                fileName= rs.getString("file_name");
                fileNameList.add(fileName);
            }
            stmt.close();
            rs.close();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return fileNameList;
    }

    /**
     * 更新三区任务状态
     * @Param: []
     * @Return: int
     * @Author: Rivan
     * @Date: 2020/11/11 10:37
     */
    public int updateTaskStatus() {
        String sql = " UPDATE task SET status=0  ";
        int result =0;
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            result = ps.executeUpdate();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * 删除记录的message回放信息
     * @Param: [fileName]
     * @Return: void
     * @Author: Rivan
     * @Date: 2020/11/11 10:37
     */
    public void deleteCanalFileAdapterPostion(String fileName) {

        String sql="delete from canal_file_adapter_postion where file_name= ? ";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1,fileName);
            ps.executeUpdate();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }




    /*public void insertHeart() {
        String sql = " insert into canal_file_adapter_postion(instance,group_id,file_name, message_num)" +
                " values (?,?,?,?) ";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, canalFileAdapterPostionDto.getInstance());
            ps.setString(2, canalFileAdapterPostionDto.getGroupId());
            ps.setString(3, canalFileAdapterPostionDto.getFileName());
            ps.setInt(4, canalFileAdapterPostionDto.getMessageNum());
            ps.executeUpdate();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }*/
}
