package com.dxmcloudfw.pojo;

import com.dxmcloudfw.util.EhcacheUtil;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author dongxm
 */
public class BasePojo {

    private static final Logger LOG = LogManager.getLogger(BasePojo.class);

    public  Map toDataMap(BasePojo bp) {
        LOG.debug("-------- base pojo -- get data map --------" + bp);
        Field[] fs = (Field[]) EhcacheUtil.readData(bp.getClass().getName()).getObjectValue();
        Map ret = new HashMap();
        for (Field fd : fs) {
            try {
                ret.put(fd.getName(), fd.get(bp));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return ret;
    }
}
