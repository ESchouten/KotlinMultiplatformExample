package com.erikschouten.kotlinmultiplatformexample

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@ExperimentalJsExport
@JsExport
class Child(
    override val a: Byte?,
    override val b: Short?,
    override val c: Int,
    override val d: Long?,
    val e: Boolean?,
    val f: Char?,
    val g: Float?,
    val h: Double?,
    val i: String = ""
) : Parent {

    override fun isValid() = c > 0 && i.isNotBlank()
}
