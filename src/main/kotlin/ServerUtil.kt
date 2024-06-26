package org.example

import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.SelfSignedCertificate

class ServerUtil {
    companion object{
        fun buildSslContext(ssl:Boolean):SslContext?{
            if(!ssl){
                return null
            }
            val ssc = SelfSignedCertificate()
            return SslContextBuilder.forServer(ssc.certificate(),ssc.privateKey()).build()
        }
    }
}