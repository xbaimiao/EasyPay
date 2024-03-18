package com.xbaimiao.easypay.book

import com.xbaimiao.easylib.util.info

object BookUtilProvider {

    private var bookUtil: BookUtil? = null

    fun setBookUtil(bookUtil: BookUtil) {
        this.bookUtil = bookUtil
        info("使用 ${bookUtil.javaClass.simpleName} 做为地图工具")
    }

    fun getBookUtil(): BookUtil {
        return bookUtil!!
    }

}