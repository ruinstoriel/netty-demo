package org.example.http.upload

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.*
import io.netty.handler.codec.http.multipart.*
import org.example.socks5.SocksServerUtils
import java.io.FileOutputStream


class HttpUploadServerHandler : SimpleChannelInboundHandler<HttpObject>() {
    val factory: HttpDataFactory = DefaultHttpDataFactory(false)
    lateinit var decoder: InterfaceHttpPostRequestDecoder
    val responseContent = StringBuilder()
    val fileStream = FileOutputStream("C:\\Users\\weiliang\\Desktop\\netty_pic.png")
    override fun channelRead0(ctx: ChannelHandlerContext?, msg: HttpObject?) {

        if (msg is HttpRequest) {

            responseContent.setLength(0);
            responseContent.append("WELCOME TO THE WILD WILD WEB SERVER\r\n");
            responseContent.append("===================================\r\n");

            responseContent.append("VERSION: " + msg.protocolVersion().text() + "\r\n");

            responseContent.append("REQUEST_URI: " + msg.uri() + "\r\n\r\n");
            responseContent.append("\r\n\r\n");
            decoder = HttpPostRequestDecoder(factory, msg)
            decoder.discardThreshold = 0
            val readingChunks = HttpUtil.isTransferEncodingChunked(msg)
            responseContent.append("Is Chunked: $readingChunks\r\n");
            responseContent.append("IsMultipart: " + decoder.isMultipart + "\r\n");
            if (readingChunks) {
                // Chunk version
                responseContent.append("Chunks: ");
            }


        }
        if (msg is HttpContent) {
            decoder.offer(msg)
            while (decoder.hasNext()) {
                val data = decoder.next()

                    if (data != null) {
                        if (data.httpDataType == InterfaceHttpData.HttpDataType.FileUpload) {
                            val fileUpload = data as FileUpload

                            fileStream.write(fileUpload.get())

                        }
                        //如果数据类型为参数类型，则保存到body对象中
                        if (data.httpDataType == InterfaceHttpData.HttpDataType.Attribute) {
                            val attribute: Attribute = data as Attribute
                            println(attribute.getName() + ":" + attribute.getValue())
                        }
                    }

            }


        }


        if (msg is LastHttpContent) {


            val response: FullHttpResponse = DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(responseContent.toString().toByteArray())
            )

            response.headers()[HttpHeaderNames.CONNECTION] = HttpHeaderValues.CLOSE


            response.headers()[HttpHeaderNames.CONTENT_TYPE] = "text/plain"
            response.headers()[HttpHeaderNames.CONTENT_LENGTH] = response.content().readableBytes()

            ctx!!.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE)
        }


    }
    @Deprecated("Deprecated in Java")
    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
        cause!!.printStackTrace()
        SocksServerUtils.closeOnFlush(ctx!!.channel())
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext?) {
        fileStream.close()
        ctx!!.flush()
    }
}
