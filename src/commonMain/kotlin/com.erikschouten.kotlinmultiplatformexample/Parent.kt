package com.erikschouten.kotlinmultiplatformexample

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@ExperimentalJsExport
@JsExport
interface Parent {
    val a: Byte?
    val b: Short?
    val c: Int?
    val d: Long?

    fun isValid(): Boolean
}
