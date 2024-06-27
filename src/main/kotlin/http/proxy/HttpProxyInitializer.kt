package org.example.http.proxy

import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelPipeline
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.HttpServerCodec
import org.example.http.helloworld.HttpHelloWorldServerHandler


class HttpProxyInitializer:ChannelInitializer<SocketChannel>() {
    override fun initChannel(ch: SocketChannel) {
        val p: ChannelPipeline = ch.pipeline()
        p.addLast(HttpServerCodec())
        p.addLast(HttpProxyHandler())
    }
}