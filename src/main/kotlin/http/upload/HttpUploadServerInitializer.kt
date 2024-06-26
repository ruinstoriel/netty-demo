package org.example.http.upload

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.*
import io.netty.handler.ssl.SslContext


class HttpUploadServerInitializer(val sslCtx:SslContext?): ChannelInitializer<SocketChannel>() {
     override fun initChannel(ch: SocketChannel) {
         val pipeline = ch.pipeline()
         sslCtx?.apply {
             pipeline.addLast(newHandler(ch.alloc()))
         }
         pipeline.addLast(HttpServerCodec())
         // pipeline.addLast(HttpObjectAggregator(1024 * 1024))
         //pipeline.addLast(HttpServerExpectContinueHandler())
         pipeline.addLast(HttpUploadServerHandler())

     }
 }