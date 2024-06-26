package org.example.http.helloworld


import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.*


class HttpHelloWorldServerHandler : SimpleChannelInboundHandler<FullHttpRequest>() {

    override fun channelRead0(ctx: ChannelHandlerContext?, msg: FullHttpRequest?) {

        val req: HttpRequest = msg as HttpRequest

        val keepAlive = HttpUtil.isKeepAlive(req)
        val response: FullHttpResponse = DefaultFullHttpResponse(
            req.protocolVersion(), HttpResponseStatus.OK,
            Unpooled.wrappedBuffer("hello, world".toByteArray())
        )
        response.headers()
            .set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN)
            .setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes())

        if (keepAlive) {
            if (!req.protocolVersion().isKeepAliveDefault()) {
                response.headers()[HttpHeaderNames.CONNECTION] = HttpHeaderValues.KEEP_ALIVE
            }
        } else {
            // Tell the client we're going to close the connection.
            response.headers()[HttpHeaderNames.CONNECTION] = HttpHeaderValues.CLOSE
        }

        val f = ctx!!.writeAndFlush(response)
        if (!keepAlive) {
            f.addListener(ChannelFutureListener.CLOSE)
        }
    }

}