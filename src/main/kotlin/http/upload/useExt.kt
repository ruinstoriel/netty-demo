package org.example.http.upload

import java.io.Closeable


inline fun <T : Closeable, R> T.useDirect(block: T.() -> R): R {
    return use { block() }
}