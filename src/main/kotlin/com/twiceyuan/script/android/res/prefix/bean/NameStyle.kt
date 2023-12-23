package com.twiceyuan.script.android.res.prefix.bean

import com.twiceyuan.script.android.res.prefix.newPrefix

enum class NameStyle {
    /**
     * 大驼峰命名
     */
    UpperCamelStyle,

    /**
     * 下划线分割命名（默认）
     */
    UnderScoreStyle
}

fun NameStyle.getNameStylePrefix(prefix: String) = when (this) {
    NameStyle.UnderScoreStyle -> {
        prefix
    }
    NameStyle.UpperCamelStyle -> {
        prefix.toUpperCamelStyle()
    }
}

val upperCamelStylePrefix by lazy {
    newPrefix.toUpperCamelStyle()
}

fun String.toUpperCamelStyle(): String {
    val words = split("_").filter { it.isNotBlank() }
    return words.joinToString("") { it.capitalize() }
}
