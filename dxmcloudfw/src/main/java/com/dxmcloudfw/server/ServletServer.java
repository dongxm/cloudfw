package com.dxmcloudfw.server;

import com.dxmcloudfw.annotation.iServlet;
import com.dxmcloudfw.annotation.iTempletClass;
import com.dxmcloudfw.pojo.BasePojo;
import com.dxmcloudfw.sysenum.TempletEnum;
import static com.dxmcloudfw.util.ClassUtil.getClassList;
import com.dxmcloudfw.util.EhcacheUtil;
import com.dxmcloudfw.util.JedisUtil;
import com.dxmcloudfw.util.PropertiesUtils;
import javax.servlet.ServletException;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.HashMap;
import org.apache.logging.log4j.Logger;
import java.util.List;
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
    public static void main(String[] args) throws ServletException {

        try {
            LOG.info("  -----------" + PropertiesUtils.getPropert("sys_name") + "--- start server ---------------------");
            init();

            List<Class<?>> servletList = getServletList(pkg);

            DeploymentInfo servletBuilder = Servlets.deployment().setClassLoader(ServletServer.class.getClassLoader())
                    .setContextPath(MYAPP)
                    .setDeploymentName(deployname);

            if (servletList != null && servletList.size() > 0) {
                iServlet s;

                for (int i = 0; i < servletList.size(); i++) {
                    try {
                        Class c = servletList.get(i).newInstance().getClass();
                        s = (iServlet) c.getAnnotation(iServlet.class);
                        LOG.debug(" ------ iServlet ------ : " + s + " servletname: " + s.servletName() + " mapping :" + s.mapping());

                        servletBuilder.addServlets(Servlets.servlet(s.servletName(), c).addMappings(s.mapping()));

                    } catch (Exception ex) {
                        LOG.info("  servlet mapping error : ex : " + ex.getMessage());
                    }
                }
            }
            DeploymentManager manager = Servlets.defaultContainer().addDeployment(servletBuilder);
            manager.deploy();

            HttpHandler servletHandler = manager.start();
            PathHandler path = Handlers.path(Handlers.redirect(MYAPP)).addPrefixPath(MYAPP, servletHandler);

            server = Undertow.builder().addHttpListener(port, "localhost").setHandler(path).build();
            server.start();

            LOG.info("----------  *  SANFE  server star  * ---------" + servletBuilder.getContextPath());

        } catch (Exception e) {
            e.printStackTrace();

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
        initTemplet();

        //初始化redis客户端
        LOG.info("------   server init  * redis  ");
        if ("true".equals(PropertiesUtils.getPropert("use_redis"))) {
            JedisUtil.getInstance();
        }

    }

    private static void initTemplet() {

        // 标识是否要遍历该包路径下子包的类名
        boolean recursive = false;
        // 指定的包名

        LOG.debug("------ init pojo pkg : "+PropertiesUtils.getPropert("pojo_pkg"));
        List<Class<?>> list = null;
        list = getClassList(PropertiesUtils.getPropert("pojo_pkg"), recursive, iTempletClass.class);

        LOG.debug("------   init templet ----- list size : "+list.size());
        
        if (list != null && list.size() > 0) {

            for (int i = 0; i < list.size(); i++) {
                try {

                    Class c = list.get(i);
                    iTempletClass s = (iTempletClass) c.getAnnotation(iTempletClass.class);
                    
                    System.out.println("  ehcache write data : key: "+c.getName() );

                    if (s.pojoType().equals(TempletEnum.req) && s.templetType().equals(TempletEnum.form)) {
                        EhcacheUtil.writeData(c.getName(), c.getDeclaredFields());
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
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

}
