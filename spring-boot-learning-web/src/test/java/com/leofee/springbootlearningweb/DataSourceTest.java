package com.leofee.springbootlearningweb;

import com.alibaba.druid.pool.DruidDataSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;

/**
 * @author leofee
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class DataSourceTest {

    @Autowired
    private DataSource dataSource;

    @Test
    public void getDataSource() {
        System.out.println("当前数据源对象为" + dataSource.getClass());
        System.out.println("当前数据源对象maxActive:" + ((DruidDataSource)dataSource).getMaxActive());
        System.out.println("当前数据源对象maxActive:" + ((DruidDataSource)dataSource).getInitialSize());
    }
}
