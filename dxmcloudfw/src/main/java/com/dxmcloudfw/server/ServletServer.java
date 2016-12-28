package com.dxmcloudfw.server;

import com.dxmcloudfw.annotation.iServlet;
import static com.dxmcloudfw.util.ClassUtil.getClassList;
import com.dxmcloudfw.util.EhcacheUtil;
import com.dxmcloudfw.util.JedisUtil;
import com.dxmcloudfw.util.PropertiesUtils;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.jsp.HackInstanceManager;
import io.undertow.jsp.JspServletBuilder;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import java.util.HashMap;
import org.apache.logging.log4j.Logger;
import java.util.List;
import org.apache.jasper.deploy.TagLibraryInfo;
import org.apache.logging.log4j.LogManager;

public class ServletServer {

    private static final String MYAPP = "/sfflow";
    private static int port = 8080;
    private static final String deployname = "sfflow.war";
    private static String pkg = "";

    private static String ehcache_conf = "";

    private static final Logger LOG = LogManager.getLogger(ServletServer.class);

    private static Undertow server;

//    private static final String CONFIGURATION_PATH = "sf.properties";
    public static void main(String[] args) {

        try {

            LOG.info("  ------- 2016 12 26----" + PropertiesUtils.getPropert("sys_name") + "--- start server ---------------------");
            init();

            List<Class<?>> servletList = getServletList(pkg);

            DeploymentInfo servletBuilder = Servlets.deployment().setClassLoader(ServletServer.class.getClassLoader())
                    .setContextPath(MYAPP)
                    .setDeploymentName(deployname)
                    .setResourceManager(new DefaultResourceLoader(ServletServer.class))
                    .addServlet(JspServletBuilder.createServlet("Default Jsp Servlet", "*.jsp"));

            if (servletList != null && servletList.size() > 0) {
                iServlet s;

                for (int i = 0; i < servletList.size(); i++) {
                    try {
                        Class c = servletList.get(i).newInstance().getClass();
                        s = (iServlet) c.getAnnotation(iServlet.class);
//                        LOG.debug(" ------ iServlet ------ : " + s + " servletname: " + s.servletName() + " mapping :" + s.mapping());

                        servletBuilder.addServlets(Servlets.servlet(s.servletName(), c).addMappings(s.mapping()));

                    } catch (Exception ex) {
//                        LOG.info("  servlet mapping error : ex : " + ex.getMessage());
                    }
                }
            }
            JspServletBuilder.setupDeployment(servletBuilder, new HashMap<>(), new HashMap<String, TagLibraryInfo>(), new HackInstanceManager());

            DeploymentManager manager = Servlets.defaultContainer().addDeployment(servletBuilder);
            manager.deploy();

            HttpHandler servletHandler = manager.start();
            PathHandler path = Handlers.path(Handlers.redirect(MYAPP)).addPrefixPath(MYAPP, servletHandler);

            server = Undertow.builder().addHttpListener(port, "localhost").setHandler(path).build();
            server.start();

            LOG.info("----------  *  SANFE  server init end   * ---------" + servletBuilder.getContextPath());

            List classlist = getClassList(pkg, true, null);
            for (int i = 0; i < classlist.size(); i++) {
                Class imp = (Class) classlist.get(i);
                Object o = imp.newInstance();

                if (o instanceof MainInterface) {
                    LOG.info("  - - -- - - - interface :" + o);
                    MainInterface mi = (MainInterface) o;
                    mi.startMain();
                }

            }
            LOG.info("----------  *  SANFE  server main start * ---------" + servletBuilder.getContextPath());

            LOG.info("----------  *  start thread ... * ---------  ");
//            ThreadManager tm = new ThreadManager(PropertiesUtils.getPropert("thread_path"));
//            Thread t = new Thread(tm);
//            t.setDaemon(true);
//            t.start();
//            Thread.currentThread().setName("sffmain");

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        } finally {
            LOG.info("  - - - -      finally      -- - - - - - - -");
        }

    }

    public static void stopServer() {

        server.stop();
    }

    private static void init() {

        pkg = PropertiesUtils.getPropert("pkg");
        port = Integer.parseInt(PropertiesUtils.getPropert("port"));
        ehcache_conf = PropertiesUtils.getPropert("ehcache_conf");

        LOG.info("------   server init  * pkg: " + pkg);
        LOG.info("------   server init  * prot: " + port);
        LOG.info("------   server init  * ehcache_conf: " + ehcache_conf);

        //初始本地缓存服务,缓冲属性
        LOG.info("------   server init  * ehcache  ");
        EhcacheUtil.getInstance().ehcacheStart(ehcache_conf);

        //初始化redis客户端
        LOG.info("------   server init  * redis  ");
        if ("true".equals(PropertiesUtils.getPropert("use_redis"))) {
            JedisUtil.getInstance();
        }

    }

    private static List<Class<?>> getServletList(String pkg) {

        // 标识是否要遍历该包路径下子包的类名
        boolean recursive = false;
        // 指定的包名

        List<Class<?>> list = null;
        // 增加 author.class的过滤项
        list = getClassList(pkg, recursive, iServlet.class);

        return list;

    }

    public static class DefaultResourceLoader extends ClassPathResourceManager {

        public DefaultResourceLoader(final Class<?> clazz) {
            super(clazz.getClassLoader(), "");
        }
    }

}
