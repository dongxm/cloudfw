package com.dxmcloudfw.util;

import java.io.FileInputStream;
import java.util.Properties;
import org.apache.ibatis.io.Resources;

/**
 *
 * @author dongxm
 */
public class PropertiesUtils {

    private PropertiesUtils() {
    }

//    private static PropertiesUtils putils;
//    static {
//        if(putils==null)
//        {
//            putils = new PropertiesUtils(); 
//        }
//    }
//    public static PropertiesUtils getInstence(){
//        return putils;
//    }
    private static Properties pro;

    static {
        if (pro == null) {
//            putils = new PropertiesUtils();
            pro = new Properties();
            FileInputStream in;
            try {

//                in = new FileInputStream(System.getProperty("user.dir") + "/sf.properties");
                pro.load(Resources.getResourceAsStream("sf.properties"));
//                in.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
    }

    public static String getPropert(String key) {
        return pro.getProperty(key);

    }

}
