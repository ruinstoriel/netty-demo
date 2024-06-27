package org.example.socks5

import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandResponse
import io.netty.handler.codec.socksx.v5.Socks5CommandRequest
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus
import io.netty.util.concurrent.FutureListener
import io.netty.util.concurrent.Promise


class SocksServerConnectHandler:SimpleChannelInboundHandler<Socks5CommandRequest>() {
    private val b: Bootstrap = Bootstrap()
    override fun channelRead0(ctx: ChannelHandlerContext?, msg: Socks5CommandRequest?) {
        val request = msg!!
        val promise: Promise<Channel> = ctx!!.executor().newPromise()
        promise.addListener(
            FutureListener { future ->
                val outboundChannel: Channel = future.now
                if (future.isSuccess) {
                    val responseFuture =
                        ctx.channel().writeAndFlush(
                            DefaultSocks5CommandResponse(
                                Socks5CommandStatus.SUCCESS,
                                request.dstAddrType(),
                                request.dstAddr(),
                                request.dstPort()
                            )
                        )

                    responseFuture.addListener(ChannelFutureListener {
                        ctx.pipeline().remove(this@SocksServerConnectHandler)
                        outboundChannel.pipeline().addLast(RelayHandler(ctx.channel()))
                        ctx.pipeline().addLast(RelayHandler(outboundChannel))
                    })
                } else {
                    ctx.channel().writeAndFlush(
                        DefaultSocks5CommandResponse(
                            Socks5CommandStatus.FAILURE, request.dstAddrType()
                        )
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

        b.connect(request.dstAddr(), request.dstPort()).addListener(ChannelFutureListener { future ->
            if (future.isSuccess) {
                // Connection established use handler provided results
            } else {
                // Close the connection if the connection attempt has failed.
                ctx.channel().writeAndFlush(
                    DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, request.dstAddrType())
                )
                SocksServerUtils.closeOnFlush(ctx.channel())
            }
        })
    }
    @Deprecated("Deprecated in Java")
    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
        cause!!.printStackTrace()
        SocksServerUtils.closeOnFlush(ctx!!.channel())
    }
}