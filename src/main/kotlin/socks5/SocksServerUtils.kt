package org.example.socks5

import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener


class SocksServerUtils {


    private fun SocksServerUtils() {}

    companion object {
        fun closeOnFlush(ch: Channel) {
            if (ch.isActive()) {
                ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE)
            }
        }
    }
}