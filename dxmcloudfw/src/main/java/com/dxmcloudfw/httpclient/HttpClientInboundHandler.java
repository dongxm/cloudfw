package com.dxmcloudfw.httpclient;

/**
 *
 * @author dxm
 */
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderUtil;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.util.ArrayList;

public class HttpClientInboundHandler extends ChannelHandlerAdapter {

    private ByteBufToBytes reader;

    private int rsLength = 0;

    private ArrayList<byte[]> allContent = new ArrayList();

    private Object head_content_length = null;

    public ByteBufToBytes getReader() {
        return reader;
    }

    private HttpClientNetty instance;

    public void setReader(ByteBufToBytes reader) {
        this.reader = reader;
    }

    public void setHC(HttpClientNetty instance) {
        this.instance = instance;
    }

    void docomplete(ChannelHandlerContext ctx) {
        if (allContent != null) {
            int size = allContent.size();
            if (size > 0) {

                byte[] ret = new byte[rsLength];
                byte[] r1 = allContent.get(0);
                System.arraycopy(r1, 0, ret, 0, r1.length);
                int aint = r1.length;
                for (int i = 1; i < size; i++) {
                    System.arraycopy(allContent.get(i), 0, ret, aint, allContent.get(i).length);
                    aint += allContent.get(i).length;
                }

                ((NettyResponse) instance.executeSetValue(ctx.channel().id())).setValue(ret);
            }
        }
        ctx.close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {

//        System.out.println("  - - - - --  is complete ------------------------------------------------------------" + allContent.size() + " content-length:" + head_content_length);
        if (head_content_length != null) {
            docomplete(ctx);
            //ctx.close();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        try {

//            System.out.println(" - - - - - - - - -  in ------------------------------------------" + msg);
//            System.out.println(msg);
            if (msg instanceof DefaultHttpResponse) {
                HttpResponse response = (HttpResponse) msg;
                head_content_length = response.headers().get(HttpHeaderNames.CONTENT_LENGTH);
//                System.out.println(" response content-length : " + head_content_length);
                ((NettyResponse) instance.executeSetValue(ctx.channel().id())).setStatus(response.status().code());

                ((NettyResponse) instance.executeSetValue(ctx.channel().id())).setCookie((String) response.headers().get(HttpHeaderNames.SET_COOKIE));

//                System.out.println(" response : " + response.headers().get(HttpHeaderNames.SET_COOKIE));
//                System.out.println(" - - - - status : " + response.status() + "  code :" + HttpResponseStatus.FOUND.code());
                if (response.status().code() == HttpResponseStatus.FOUND.code()) {

                    if (response.headers().names().contains("Location")) {
                        ((NettyResponse) instance.executeSetValue(ctx.channel().id())).setStatus(HttpResponseStatus.FOUND.code());
                        ((NettyResponse) instance.executeSetValue(ctx.channel().id())).setRedirect(response.headers().get("Location").toString());
//                        System.out.println(response.headers().get("Location"));
                    }
                    ctx.close();
                }

                if (HttpHeaderUtil.isContentLengthSet(response)) {
//                    System.out.println(" - - - - - - - - is content set ---------");
                    reader = new ByteBufToBytes((int) HttpHeaderUtil.getContentLength(response));
                }

//                System.out.println(" - - - -   end --------  1-----------");
            }

            if (msg instanceof HttpContent) {

//                System.out.println(" - - - -HttpContent --------  1 -----------");
                HttpContent httpContent = (HttpContent) msg;
//                System.out.println("  *******************  content : " + httpContent.content().toString(CharsetUtil.UTF_8));
                ByteBuf content = httpContent.content();
//                System.out.println(" - - - -HttpContent --------  2 -----------" + content.capacity());

                if (content.toString().equals("EmptyByteBufBE")) {
//                    System.out.println(" -  - - - - - -  emptyByteBuffBE -------------------------------");
                    docomplete(ctx);
//                    ctx.close();
                    return;
                }

                if (content.capacity() > 0) {
//                    System.out.println("  ---------------- new reader --------------------");
                    rsLength += content.capacity();
                    reader = new ByteBufToBytes(content.capacity());

                }

//                content.retain();
//                content.duplicate();
                reader.reading(content);
                content.release();

//                System.out.println("   reader end " + reader.isEnd());
                if (reader.isEnd()) {
                    allContent.add(reader.readFull());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
