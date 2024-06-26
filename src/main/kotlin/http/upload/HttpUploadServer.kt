package org.example.http.upload

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import org.example.ServerUtil


class HttpUploadServer(val ssl:Boolean,val port:Int) {


    fun start(){
        val sslCtx = ServerUtil.buildSslContext(ssl)
        val bossGroup = NioEventLoopGroup(1)
        val workerGroup = NioEventLoopGroup()
        try {
            val b = ServerBootstrap()
            b.group(bossGroup, workerGroup)
            b.channel(NioServerSocketChannel::class.java)
            b.handler(LoggingHandler(LogLevel.INFO))
            b.childHandler(HttpUploadServerInitializer(sslCtx))

            val ch = b.bind(port).sync().channel()

            System.err.println(
                "Open your web browser and navigate to " +
                        (if (ssl) "https" else "http") + "://127.0.0.1:" + port + '/'
            )

            ch.closeFuture().sync()
        } finally {
            bossGroup.shutdownGracefully()
            workerGroup.shutdownGracefully()
        }
    }
}