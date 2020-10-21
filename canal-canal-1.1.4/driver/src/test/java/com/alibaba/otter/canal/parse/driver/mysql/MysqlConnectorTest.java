package com.alibaba.otter.canal.parse.driver.mysql;

import com.alibaba.otter.canal.parse.driver.mysql.packets.server.ResultSetPacket;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;

@Ignore
public class MysqlConnectorTest {

    @Test
    public void testQuery() {

        MysqlConnector connector = new MysqlConnector(new InetSocketAddress("127.0.0.1", 3306), "xxxxx", "xxxxx");
        try {
            connector.connect();
            MysqlQueryExecutor executor = new MysqlQueryExecutor(connector);
            ResultSetPacket result = executor.query("show variables like '%char%';");
            System.out.println(result);
            result = executor.query("select * from test.test1");
            System.out.println(result);
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        } finally {
            try {
                connector.disconnect();
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        }
    }

    @Test
    public void testUpdate() {

        MysqlConnector connector = new MysqlConnector(new InetSocketAddress("111.231.110.80", 3306), "root", "doXbwOrq");
        try {
            connector.connect();
            MysqlUpdateExecutor executor = new MysqlUpdateExecutor(connector);
            executor.update("CREATE DATABASE `testDataBase` CHARACTER SET 'utf8' COLLATE 'utf8_general_ci'");
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        } finally {
            try {
                connector.disconnect();
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        }
    }
}
