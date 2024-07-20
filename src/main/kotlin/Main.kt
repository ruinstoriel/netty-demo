package org.example

import io.netty.util.concurrent.Future
import io.netty.util.concurrent.GenericFutureListener
import org.example.http.upload.HttpUploadServer


fun main() {
   HttpUploadServer(false,8080).start()

}

fun interface IntPredicate {
    fun accept(i: Int): Boolean
}

fun interface Me{
     fun demo():String
}




fun interface Demo<V>: GenericFutureListener<Future<V>> {


}
