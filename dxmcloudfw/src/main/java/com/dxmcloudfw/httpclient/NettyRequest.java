package com.dxmcloudfw.httpclient;

import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author dxm
 */
public class NettyRequest {

    private static final Logger LOG = LogManager.getLogger(NettyRequest.class);

    private String content_type;
    private ChannelFuture channelf;
    private DefaultFullHttpRequest request;
    private URI uri;
    private int port;
    private Cookie cookie;
    private HttpMethod method;
    private String accept;
    private Object parameters;// 只能接受 map , String 两种

    public ChannelFuture getChannelFuture() {
        return channelf;
    }

    public DefaultFullHttpRequest getRequest() {
        return request;
    }

    public void setRequest(DefaultFullHttpRequest request) {
        this.request = request;
    }

    public Object getParameters() {
        return parameters;
    }

    public void setParameters(Object parameters) {
        this.parameters = parameters;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public String getAccept() {
        return accept;
    }

    public void setAccept(String accept) {
        this.accept = accept;
    }

    public Cookie getCookie() {
        return cookie;
    }

    public void setCookie(Cookie cookie) {
        this.cookie = cookie;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getContent_type() {
        return content_type;
    }

    public void setContent_type(String content_type) {
        this.content_type = content_type;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(String uri) {
        try {
//            System.out.println(" set uri  : " + uri);
            this.uri = new URI(uri);
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        }
    }

    public void init() {

        channelf = HttpClientNetty.b.connect(uri.getHost(), port).syncUninterruptibly();
        request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method,
                this.uri.toASCIIString());
        request.headers().set(HttpHeaderNames.HOST, uri.getHost());
        request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);

        if (content_type != null) {
            request.headers().set(HttpHeaderNames.CONTENT_TYPE, content_type);
        }

        if (accept != null) {
            request.headers().set(HttpHeaderNames.ACCEPT, accept);
        }

        request.headers().set(HttpHeaderNames.CONTENT_LENGTH, "" + request.content().readableBytes());
        if (this.cookie != null) {
            request.headers().set(HttpHeaderNames.COOKIE, cookie.value());
        }

    }

}
