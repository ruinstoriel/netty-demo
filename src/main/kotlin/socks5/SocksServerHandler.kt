package org.example.socks5

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.socksx.v5.*


class SocksServerHandler:SimpleChannelInboundHandler<Socks5Message>(){
    override fun channelRead0(ctx: ChannelHandlerContext?, msg: Socks5Message?) {
        if (msg is Socks5InitialRequest) {
            // auth support example
            //ctx.pipeline().addFirst(new Socks5PasswordAuthRequestDecoder());
            //ctx.write(new DefaultSocks5AuthMethodResponse(Socks5AuthMethod.PASSWORD));
            ctx!!.pipeline().addFirst(Socks5CommandRequestDecoder())
            ctx.write(DefaultSocks5InitialResponse(Socks5AuthMethod.NO_AUTH))
        } else if (msg is Socks5PasswordAuthRequest) {
            ctx!!.pipeline().addFirst(Socks5CommandRequestDecoder())
            ctx.write(DefaultSocks5PasswordAuthResponse(Socks5PasswordAuthStatus.SUCCESS))
        } else if (msg is Socks5CommandRequest) {
            if (msg.type() === Socks5CommandType.CONNECT) {
                ctx!!.pipeline().addLast(SocksServerConnectHandler())
                ctx.pipeline().remove(this)
                ctx.fireChannelRead(msg)
            } else {
                ctx!!.close()
            }
        } else {
            ctx!!.close()
        }

    }

    override fun channelReadComplete(ctx: ChannelHandlerContext?) {
        ctx!!.flush()
    }

    @Deprecated("Deprecated in Java")
    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable?) {
        cause!!.printStackTrace()
        SocksServerUtils.closeOnFlush(ctx!!.channel())
    }
}