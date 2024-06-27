package org.example.socks5

import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.util.ReferenceCountUtil


class RelayHandler(private val relayChannel: Channel?):ChannelInboundHandlerAdapter() {


    override fun channelActive(ctx: ChannelHandlerContext) {

    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (relayChannel!!.isActive) {
            relayChannel.writeAndFlush(msg)
        } else {
            ReferenceCountUtil.release(msg)
        }
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        if (relayChannel!!.isActive) {
            SocksServerUtils.closeOnFlush(relayChannel)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
        cause!!.printStackTrace()
        SocksServerUtils.closeOnFlush(ctx!!.channel())
    }
}