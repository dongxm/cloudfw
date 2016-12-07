package com.dxmcloudfw.httpclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HttpClientApache {
    
    private static final Logger LOG = LogManager.getLogger(HttpClientApache.class);
    
    private final static int BUFFER = 10240;
    
    private static HttpClientApache instance;
    
    private HttpClientApache() {
    }
    
    private static class HttpClientLoader {
        
        private static final HttpClientApache instance = new HttpClientApache();
    }
    
    public static HttpClientApache getInstance() {
        instance = HttpClientLoader.instance;
        return instance;
        
    }
    
    public static StringBuffer sendPost(Map parameters, String url, Charset charset) {
        StringBuffer ret = null;

        //创建HttpClient对象  
        CloseableHttpClient closeHttpClient = HttpClients.createDefault();
        CloseableHttpResponse httpResponse = null;
        //发送Post请求  
        HttpPost httpPost = new HttpPost(url);
        //设置Post参数  
        List<NameValuePair> params = new ArrayList();
        
        Iterator its = parameters.keySet().iterator();
        if (parameters != null) {
            for (Object key : parameters.keySet()) {
                params.add(new BasicNameValuePair(key.toString(), (String) parameters.get(key)));
            }
        }
        
        try {
            //转换参数并设置编码格式  
            httpPost.setEntity(new UrlEncodedFormEntity(params, charset));
            //执行Post请求 得到Response对象  
            httpResponse = closeHttpClient.execute(httpPost);

            //httpResponse.getStatusLine() 响应头信息  
            LOG.info(" - - - - - " + httpResponse.getStatusLine().getStatusCode());
            
            if (httpResponse.getStatusLine().getStatusCode() == 302) {
                System.out.println("  - - - - -  rediret  - - - -");
                LOG.info("  ---------   rediret  ---------  ");
                //处理请求头
                String urlhead = url.substring(url.indexOf("//") + 2);
                urlhead = "http://" + urlhead.substring(0, urlhead.indexOf("/"));
                
                Header[] hs = httpResponse.getHeaders("Location");
                
                for (Header h : hs) {
                    System.out.println("  hs : " + h.getValue());
                }
                LOG.info("  url : " + urlhead + hs[0].getValue());
                HttpGet httpGet = new HttpGet(urlhead + hs[0].getValue());
                
                httpResponse = closeHttpClient.execute(httpGet);
                
            }

            LOG.debug(" - - - - - " + httpResponse.getStatusLine());
            //返回对象 向上造型  
            HttpEntity httpEntity = httpResponse.getEntity();
            if (httpEntity != null) {
                //响应输入流  
                InputStream is = httpEntity.getContent();
                
                BufferedReader br = new BufferedReader(new InputStreamReader(is, Consts.UTF_8));
                ret = new StringBuffer("");
                String line;
                
                while ((line = br.readLine()) != null) {
                    ret.append(line);
                }
                
                is.close();
                return ret;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (httpResponse != null) {
                try {
                    httpResponse.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (closeHttpClient != null) {
                try {
                    closeHttpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return ret;
    }
    
    public static void main(String[] ddd) {
        System.out.println(Calendar.getInstance().getTimeInMillis());
        Map parameters = new HashMap();
        parameters.put("username", "111111");
        parameters.put("password", "111111");
        parameters.put("type", "cpu");

//        StringBuffer result = HttpClientApache.sendPost(parameters, "http://localhost:8083/br/hardware", Consts.UTF_8);
        StringBuffer result = HttpClientApache.sendPost(parameters, "http://localhost:8080/open/do_login", Consts.UTF_8);
        
        System.out.println(result.toString());
        
        System.out.println(Calendar.getInstance().getTimeInMillis());
        
    }
}
