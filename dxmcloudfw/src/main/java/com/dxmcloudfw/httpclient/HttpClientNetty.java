package com.dxmcloudfw.httpclient;

/**
 *
 * @author dxm
 */
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestEncoder;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class HttpClientNetty {

    private static HttpClientNetty instance;

    private static Map responseValues = new HashMap();

//    private static ExecutorService pool = Executors.newFixedThreadPool();
    static NioEventLoopGroup workerGroup;

    static Bootstrap b; // (1) 

    static {
        workerGroup = new NioEventLoopGroup();
        b = new Bootstrap();
        b.group(workerGroup);
        b.channel(NioSocketChannel.class); // (3) 
        b.option(ChannelOption.SO_KEEPALIVE, true); // (4)  
        b.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
//                httpclientInHandler = new HttpClientInboundHandler();
//                httpclientInHandler.setHC(instance);
                //注册编解码处理
                ch.pipeline().addLast(new HttpResponseDecoder());
                ch.pipeline().addLast(new HttpRequestEncoder());
                ch.pipeline().addLast(new HttpClientInboundHandler());

            }
        });
    }

    private HttpClientNetty() {
    }

    private static class HttpClientLoader {

        private static final HttpClientNetty instance = new HttpClientNetty();
    }

    public static HttpClientNetty getInstance() {
        instance = HttpClientLoader.instance;
        return instance;

    }

    /**
     * 收消息后回调
     *
     * @param objects
     * @return
     */
    public static Object executeSetValue(Object... objects) {
        return responseValues.get(objects[0]);
    }

//    synchronized ChannelFuture connect(String host, int port) {
//        return b.connect(host, port).syncUninterruptibly();
//    }
    public static NettyResponse sendRequest(NettyRequest request) {
        NettyResponse re = null;
        try {

            if (request.getMethod().equals(HttpMethod.POST)) {
                re = sendPost(request);
            } else if (request.getMethod().equals(HttpMethod.GET)) {
                re = sendGet(request);
            }

//            System.out.println(" ######################  response : "+re.getStatus());
            //重定向处理
            if (re != null && re.getStatus() == HttpResponseStatus.FOUND.code()) {
                NettyRequest request_rect = new NettyRequest();
                request_rect.setMethod(request.getMethod());

//                System.out.println("  - - - - -  rediret  - - - -");
                //处理请求头
                String urlhead = request.getUri().toString().substring(request.getUri().toString().indexOf("//") + 2);
                urlhead = "http://" + urlhead.substring(0, urlhead.indexOf("/"));
                request_rect.setUri(urlhead + re.getRedirect());

                //初始化，前面构造填值后
                request_rect.init();
                re = instance.sendRequest(request_rect);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return re;
    }

    /**
     *
     * @param request
     * @return
     */
    private synchronized static NettyResponse sendPost(NettyRequest request) {

//        System.out.println(" - - - - - ---------- 1 --------");
        try {
            //初始返回      
            ChannelFuture channelF = request.getChannelFuture();
//            System.out.println("  new  channel id : " + channelF.channel().id());
            responseValues.put(channelF.channel().id(), new NettyResponse());

            Object param = request.getParameters();

            if (param == null) {
                channelF.channel().write(request.getRequest());
            } else if (param instanceof Map) {
//                System.out.println(" map param ");

                HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MAXSIZE);
                // This encoder will help to encode Request for a FORM as POST.
                HttpPostRequestEncoder bodyRequestEncoder = new HttpPostRequestEncoder(factory, request.getRequest(), false);
                // add Form attribute
                Map<String, String> m = (Map) param;
                for (Map.Entry<String, String> entry : m.entrySet()) {
                    bodyRequestEncoder.addBodyAttribute(entry.getKey(), entry.getValue());
                }

                // 发送http请求  
                channelF.channel().write(bodyRequestEncoder.finalizeRequest());

            } else if (param.getClass() == String.class) {
                // apply to json or private type
//                System.out.println(" string  param ");

                ByteBuf buffer = request.getRequest().content().clear();
                int p0 = buffer.writerIndex();
                buffer.writeBytes(((String) request.getParameters()).getBytes());
                int p1 = buffer.writerIndex();
                request.getRequest().headers().set(HttpHeaderNames.CONTENT_LENGTH, Integer.toString(p1 - p0));
                // 发送http请求  
                channelF.channel().write(request.getRequest());
            }

            channelF.channel().flush();
            channelF.channel().closeFuture().syncUninterruptibly();
            return (NettyResponse) responseValues.get(channelF.channel().id());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     *
     * @param request
     * @return
     */
    private synchronized static NettyResponse sendGet(NettyRequest request) {

//        System.out.println(" - - - - - ---------- 1 --------");
        try {
            //初始返回      
            ChannelFuture channelF = request.getChannelFuture();
//            System.out.println("  new  channel id : " + channelF.channel().id());

            responseValues.put(channelF.channel().id(), new NettyResponse());

            channelF.channel().write(request.getRequest());

            channelF.channel().flush();
            channelF.channel().closeFuture().syncUninterruptibly();
            return (NettyResponse) responseValues.get(channelF.channel().id());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

//    private class ExecSendPost implements Callable<NettyRequest> {
//
//        private NettyRequest request;
//
//        ExecSendPost(NettyRequest req) {
//            request = req;
//        }
//
//        @Override
//        public NettyRequest call() throws Exception {
//            try {
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            return request;
//        }
//
//    }
    public static void main(String[] d) {

        byte[] ret;
        try {
            System.out.println(Calendar.getInstance().getTimeInMillis());

            //构造 发送对象 request
            NettyRequest request = new NettyRequest();
//            request.setAccept("application/json");
//            request.setContent_type("application/json;charset=utf-8");
            request.setMethod(HttpMethod.POST);

            //参数
//            System.out.println("   - - - - -  : " + Calendar.getInstance().getTimeInMillis());
//            ObjectMapper objectMapper = new ObjectMapper();
//            String json = "{'userType':'facebook','userEmail':'demo2@sinobel.com','password':'demo2'}";
            Map params = new HashMap();
            params.put("username", "111111");
            params.put("password", "111111");
            params.put("type", "cpu");

            request.setParameters(params);

//            request.setPort(8080);
            request.setUri("http://localhost:8080/open/do_login");

//            request.setPort(8083);
//            request.setUri("http://localhost:8083/br/hardware");
            //初始化，前面构造填值后
            request.init();

            System.out.println(Calendar.getInstance().getTimeInMillis());
            //发送
            NettyResponse re = HttpClientNetty.sendRequest(request);

            ret = re.getValue();
            System.out.println(" ret status : " + re.getStatus());
            System.out.println(new String(ret, "utf-8"));

//
            System.out.println(Calendar.getInstance().getTimeInMillis());

        } catch (Exception e) {

            e.printStackTrace();
        }

    }

}
