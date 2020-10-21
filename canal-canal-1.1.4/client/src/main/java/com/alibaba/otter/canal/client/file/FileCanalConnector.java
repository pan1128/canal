package com.alibaba.otter.canal.client.file;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.Date;
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
