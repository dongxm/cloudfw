package com.dxmcloudfw.httpclient;

/**
 *
 * @author dxm
 */
public class NettyResponse {
    private String cookie ;
    private byte[] value;
    private int status;
    private String redirect;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getRedirect() {
        return redirect;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }
    
    

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }
    
    
    
    
}
