package com.dxmcloudfw.db;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

/**
 *
 * @author dongxm
 */
public final class DataSourceSqlSessionFactory {

    private static final String CONFIGURATION_PATH = "mybatis-config.xml";

    private static final Map<DataSourceEnvironment, SqlSessionFactory> SQLSESSIONFACTORYS
            = new HashMap<DataSourceEnvironment, SqlSessionFactory>();

    /**
     * 根据指定的DataSourceEnvironment获取对应的SqlSessionFactory
     *
     * @param environment 数据源environment
     * @return SqlSessionFactory
     */
    public static SqlSessionFactory getSqlSessionFactory(DataSourceEnvironment environment) {

        SqlSessionFactory sqlSessionFactory = SQLSESSIONFACTORYS.get(environment);
        if (sqlSessionFactory != null) {
            return sqlSessionFactory;
        } else {
            InputStream ins = null;
            try {
                ins = Resources.getResourceAsStream(CONFIGURATION_PATH);
                sqlSessionFactory = new SqlSessionFactoryBuilder().build(ins, environment.name());

            } catch (Exception e) {
                e.printStackTrace();
            } 

            SQLSESSIONFACTORYS.put(environment, sqlSessionFactory);
            return sqlSessionFactory;
        }
    }

    public static enum DataSourceEnvironment {
        HR,
        HW;
    }
}
