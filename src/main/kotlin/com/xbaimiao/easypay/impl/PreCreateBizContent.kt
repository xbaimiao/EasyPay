package com.xbaimiao.easypay.impl

import com.google.gson.annotations.SerializedName

data class PreCreateBizContent(
    @SerializedName("out_trade_no")
    val outTradeNo: String,
    @SerializedName("total_amount")
    val totalAmount: String,
    @SerializedName("subject")
    val subject: String,
    @SerializedName("store_id")
    val storeId: String,
    @SerializedName("qr_code_timeout_express")
    val timeout: String
)
