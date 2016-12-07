package com.dxmcloudfw.util;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apache.ibatis.io.Resources;

/**
 *
 * @author dxm
 */
public class EhcacheUtil {

    private static EhcacheUtil instance;
    private static CacheManager cacheManager;
    private static Cache c;

    public static void main(String[] args) {
//        ehcacheSetUp();
//
//        ehcacheUse();
    }
    
    private EhcacheUtil() {
    }

    private static class EhcacheUtilLoader {

        private static final EhcacheUtil instance = new EhcacheUtil();
    }

    public static EhcacheUtil getInstance() {
        instance = EhcacheUtilLoader.instance;
        return instance;

    }

    public static void ehcacheStart(String conf) {

        try {
            cacheManager = CacheManager.create(Resources.getResourceAsStream(conf));
            c = cacheManager.getCache(cacheManager.getCacheNames()[0]);
        } catch (IOException ex) {
            Logger.getLogger(EhcacheUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public static void ehcacheStop(){
        cacheManager.shutdown();
    }

    public static void writeData(Object key, Object value) {
        c.put(new Element(key, value));
    }

    public static Element readData(Object key) {
        return c.get(key);
    }
}
