package com.xbaimiao.easypay.results

import dev.rgbmc.expression.functions.FunctionResult

class CancellableResult(val status: Boolean) : FunctionResult() {
    var isCancelled = false
    var shouldCancel = false
}
