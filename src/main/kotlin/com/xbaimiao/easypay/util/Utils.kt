package com.xbaimiao.easypay.util

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun Long.formatTime(): String {
    val dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(this), ZoneId.systemDefault())

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val formattedDateTime = dateTime.format(formatter)

    return formattedDateTime
}

fun List<String>.listToString(): String {
    val list = this
    val string = buildString {
        list.forEach {
            append(it)
        }
    }
    return string
}