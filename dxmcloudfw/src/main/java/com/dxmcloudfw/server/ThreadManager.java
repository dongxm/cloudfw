package com.dxmcloudfw.server;

import com.dxmcloudfw.annotation.iThread;
import static com.dxmcloudfw.util.ClassUtil.getClassList;
import com.dxmcloudfw.util.EhcacheUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 管理定时任务. 暂时只做启动处理
 *
 * @author dongxm
 */
public class ThreadManager implements Runnable {

    private static List taskList = new ArrayList();

    private static final Logger LOG = LogManager.getLogger(ThreadManager.class);

    private static ScheduledExecutorService exeservice;

    public ThreadManager(String class_path) {

        List<Class<?>> threadList = getClassList(class_path, false, iThread.class);

        if (threadList != null && threadList.size() > 0) {
            exeservice = Executors.newScheduledThreadPool(threadList.size());

            iThread s;

            for (int i = 0; i < threadList.size(); i++) {
                try {
                    Object c = threadList.get(i).newInstance();
                    taskList.add(c);
                    LOG.info(" objcet : " + c.getClass().getName());
                    s = (iThread) c.getClass().getAnnotation(iThread.class);
                    LOG.info(" task clas : " + s.getClass().getName() + " " + s.loopNumber() + " , " + s.delay() + " , " + s.startTime());

                    EhcacheUtil.writeData(c.getClass().getName() + "_delay", s.delay());
                    EhcacheUtil.writeData(c.getClass().getName() + "_loop", s.loopNumber());
                    EhcacheUtil.writeData(c.getClass().getName() + "_initdelay", s.initialDelay());
                    EhcacheUtil.writeData(c.getClass().getName() + "_mode", s.mode());

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

    }

    @Override
    public void run() {

        try {
            for (int i = 0; i < taskList.size(); i++) {
                String classname = taskList.get(i).getClass().getName();
                LOG.info("  runnable : class name : " + classname);
                Runnable r = (Runnable) taskList.get(i);

                if ((int) EhcacheUtil.readData(classname + "_mode").getObjectValue() == 1) {

                    exeservice.scheduleWithFixedDelay(r, (long) EhcacheUtil.readData(classname + "_initdelay").getObjectValue(), (long) EhcacheUtil.readData(classname + "_delay").getObjectValue(), TimeUnit.MILLISECONDS);
                } else {
                    exeservice.scheduleAtFixedRate(r, (long) EhcacheUtil.readData(classname + "_initdelay").getObjectValue(), (long) EhcacheUtil.readData(classname + "_delay").getObjectValue(), TimeUnit.MILLISECONDS);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
