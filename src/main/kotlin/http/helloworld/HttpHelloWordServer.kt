package org.example.http.helloworld


import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.*
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import io.netty.handler.ssl.ClientAuth
import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.SelfSignedCertificate
import org.slf4j.LoggerFactory


class HttpHelloWordServer {
    private val log = LoggerFactory.getLogger(this.javaClass)

    var SSL: Boolean = System.getProperty("ssl") != null


    @Throws(Exception::class)
    fun start(port: Int) {
        // Configure SSL.
        val sslCtx: SslContext?
        if (SSL) {
            val ssc = SelfSignedCertificate()
            sslCtx = SslContextBuilder
                .forServer(ssc.certificate(), ssc.privateKey())
                .clientAuth(ClientAuth.NONE)
                .build()
        } else {
            sslCtx = null
        }

        // Configure the server.
        val bossGroup: EventLoopGroup = NioEventLoopGroup(1)
        val workerGroup: EventLoopGroup = NioEventLoopGroup()

        try {
            val b = ServerBootstrap()
            b.option(ChannelOption.SO_BACKLOG, 1024)
            b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel::class.java)
                .handler(LoggingHandler(LogLevel.INFO))
                .childHandler(object : ChannelInitializer<SocketChannel>() {
                    @Throws(Exception::class)
                    override fun initChannel(ch: SocketChannel) {
                        val p: ChannelPipeline = ch.pipeline()
                        if (sslCtx != null) {
                            p.addLast(sslCtx.newHandler(ch.alloc()))
                        }
                        p.addLast(HttpServerCodec())
                        p.addLast(HttpHelloWorldServerHandler())


                    }
                })

            val ch: Channel = b.bind(port).sync().channel()

            log.info("HTTP服务器启动啦, port={}", port)

            ch.closeFuture().sync()
        } finally {
            bossGroup.shutdownGracefully()
            workerGroup.shutdownGracefully()
        }
    }
}