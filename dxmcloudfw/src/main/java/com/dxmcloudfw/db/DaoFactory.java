package com.dxmcloudfw.db;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

/**
 *
 * @author dongxm
 */
public enum DaoFactory {

    HR {
        private SqlSessionFactory sqlSessionFactory;

        @Override
        public <T> T createMapper(Class<? extends BaseDao> clazz) {
            return createMapper(clazz, this);
        }

        @Override
        protected void createSqlSessionFactory() {
            sqlSessionFactory = DataSourceSqlSessionFactory.getSqlSessionFactory(DataSourceSqlSessionFactory.DataSourceEnvironment.HR);
        }

        @Override
        public SqlSessionFactory getSqlSessionFactory() {
            return sqlSessionFactory;
        }

    },
    HW {
        private SqlSessionFactory sqlSessionFactory;

        @Override
        public <T> T createMapper(Class<? extends BaseDao> clazz) {
            return createMapper(clazz, this);
        }

        @Override
        protected void createSqlSessionFactory() {
            sqlSessionFactory = DataSourceSqlSessionFactory.getSqlSessionFactory(DataSourceSqlSessionFactory.DataSourceEnvironment.HW);
        }

        @Override
        public SqlSessionFactory getSqlSessionFactory() {
            return sqlSessionFactory;
        }

    };

    public abstract <T> T createMapper(Class<? extends BaseDao> clazz);

    public static SqlSession getSqlSession(Object obj) {

        MapperProxy mp = (MapperProxy) Proxy.getInvocationHandler(obj);
        
//        System.out.println("----------00000: " + mp.sqlSession.toString());
        return mp.sqlSession;

    }

    public static void setIsTransaction(Object obj, boolean b) {
        MapperProxy mp = (MapperProxy) Proxy.getInvocationHandler(obj);
        mp.isTransaction = b;
    }

    /**
     * Create SqlSessionFactory of environment
     */
    protected abstract void createSqlSessionFactory();

    /**
     * get SqlSessionFactory
     */
    public abstract SqlSessionFactory getSqlSessionFactory();

    static {
        try {
            HR.createSqlSessionFactory();
            HW.createSqlSessionFactory();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static <T> T createMapper(Class<? extends BaseDao> clazz, DaoFactory daoFactory) {
        SqlSession sqlSession = daoFactory.getSqlSessionFactory().openSession();
        BaseDao basedao = sqlSession.getMapper(clazz);
//        System.out.println(" --------------1111111: " + sqlSession.toString());
        return (T) MapperProxy.bind(basedao, sqlSession);
    }

    private static class MapperProxy implements InvocationHandler {

        private BaseDao basedao;
        private SqlSession sqlSession;
        private boolean isTransaction = false;

        private MapperProxy(BaseDao basedao, SqlSession sqlSession) {
            this.basedao = basedao;
            this.sqlSession = sqlSession;
        }

        private static BaseDao bind(BaseDao basedao, SqlSession sqlSession) {
            BaseDao ret = (BaseDao) Proxy.newProxyInstance(basedao.getClass().getClassLoader(),
                    basedao.getClass().getInterfaces(), new MapperProxy(basedao, sqlSession));

            return ret;
        }

        /**
         * execute mapper method and finally close sqlSession
         */
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object object = null;
            try {
//                System.out.println(" - - - - -  invoke -——--- " + method.getName());
                object = method.invoke(basedao, args);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (!isTransaction) {
                    sqlSession.commit();
                    sqlSession.close();
                }
            }
            return object;
        }

    }

}
