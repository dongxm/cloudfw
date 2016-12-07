package com.dxmcloudfw.util;

import java.io.UnsupportedEncodingException;
import java.security.SignatureException;
import java.util.Random;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MD5 {

    private static final Logger LOG = LogManager.getLogger(MD5.class);

    private static final String BaseChars = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ~!@#$%&*";

    public static void main(String[] a) {
        
        String key = getRandomString(24);
        System.out.println(key);
        
        String src = "测试测试";
        String sign = sign(src, key, "gbk");
        System.out.println("sign : " + sign);
        System.out.println("verify : " + verify("测试测试", sign, key, "gbk"));

        
    }

    public static String getRandomString(int length) {
        StringBuilder ret = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            int number = random.nextInt(70);
            ret.append(BaseChars.charAt(number));
        }

        return ret.toString();
    }


    /**
     * 签名字符串
     *
     * @param text 需要签名的字符串
     * @param key 密钥
     * @param input_charset 编码格式
     * @return 签名结果
     */
    public static String sign(String text, String key, String input_charset) {
        text = text + key;
        return DigestUtils.md5Hex(getContentBytes(text, input_charset));
    }

    /**
     * 签名字符串
     *
     * @param text 需要签名的字符串
     * @param sign 签名结果
     * @param key 密钥
     * @param input_charset 编码格式
     * @return 签名结果
     */
    public static boolean verify(String text, String sign, String key, String input_charset) {
        text = text + key;
        String mysign = DigestUtils.md5Hex(getContentBytes(text, input_charset));
        if (mysign.equals(sign)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param content
     * @param charset
     * @return
     * @throws SignatureException
     * @throws UnsupportedEncodingException
     */
    private static byte[] getContentBytes(String content, String charset) {
        if (charset == null || "".equals(charset)) {
            return content.getBytes();
        }
        try {
            return content.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            LOG.info("MD5签名过程中出现错误,指定的编码集不对,您目前指定的编码集是:" + charset);
            throw new RuntimeException("MD5签名过程中出现错误,指定的编码集不对,您目前指定的编码集是:" + charset);
        }
    }

}
