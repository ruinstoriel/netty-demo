package org.example

import io.netty.util.concurrent.Future
import io.netty.util.concurrent.GenericFutureListener
import org.example.socks5.Socks5Server


fun main() {
    Socks5Server().start(2080)
}

fun interface IntPredicate {
    fun accept(i: Int): Boolean
}

fun interface Me{
     fun demo():String
}




fun interface Demo<V>: GenericFutureListener<Future<V>> {


}
