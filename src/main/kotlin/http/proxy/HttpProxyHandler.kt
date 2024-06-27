package org.example.http.proxy

import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelOption
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http.*
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandResponse
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus
import io.netty.util.concurrent.FutureListener
import io.netty.util.concurrent.Promise
import org.example.socks5.DirectClientHandler
import org.example.socks5.RelayHandler
import org.example.socks5.SocksServerUtils

class HttpProxyHandler:SimpleChannelInboundHandler<DefaultHttpRequest>() {
    private val b: Bootstrap = Bootstrap()
    override fun channelRead0(ctx: ChannelHandlerContext?, msg: DefaultHttpRequest?) {
        if(msg is HttpRequest && msg.method() == HttpMethod.CONNECT){
            val request = msg

            val promise: Promise<Channel> = ctx!!.executor().newPromise()
            promise.addListener(
                FutureListener { future ->
                    val outboundChannel: Channel = future.now
                    if (future.isSuccess) {
                        val responseFuture = ctx.channel().writeAndFlush(
                                DefaultHttpResponse(msg.protocolVersion(),HttpResponseStatus.OK)
                            )

                        responseFuture.addListener(ChannelFutureListener {
                            ctx.pipeline().remove(this@HttpProxyHandler)
                            ctx.pipeline().remove(HttpServerCodec::class.java)
                            outboundChannel.pipeline().addLast(RelayHandler(ctx.channel()))
                            ctx.pipeline().addLast(RelayHandler(outboundChannel))
                        })
                    } else {
                        ctx.channel().writeAndFlush(
                            DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.SERVICE_UNAVAILABLE)
                        )
                        SocksServerUtils.closeOnFlush(ctx.channel())
                    }
                })

            val inboundChannel: Channel = ctx.channel()
            b.group(inboundChannel.eventLoop())
                .channel(NioSocketChannel::class.java)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(DirectClientHandler(promise))
            println("----------" + msg.headers()["Host"] + "------" + msg.headers()["Proxy-Authorization"])
            b.connect(msg.headers()["Host"].split(":")[0], 443).addListener(ChannelFutureListener { future ->
                if (future.isSuccess) {
                    // Connection established use handler provided results
                } else {
                    // Close the connection if the connection attempt has failed.
                    ctx.channel().writeAndFlush(
                        DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.SERVICE_UNAVAILABLE)
                    )
                    SocksServerUtils.closeOnFlush(ctx.channel())
                }
            })
        }
    }
    @Deprecated("Deprecated in Java")
    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
        cause!!.printStackTrace()
        SocksServerUtils.closeOnFlush(ctx!!.channel())
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext?) {
        ctx!!.flush()
    }
}