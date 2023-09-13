package com.xbaimiao.easypay

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitArray
import com.google.zxing.common.BitMatrix
import java.awt.image.BufferedImage
import java.util.*

object ZxingUtil {

    fun generate(content: String, width: Int = 128, height: Int = 128): BufferedImage {
        val encodeHints: MutableMap<EncodeHintType, Any> = EnumMap(EncodeHintType::class.java)
        encodeHints[EncodeHintType.CHARACTER_SET] = "UTF-8"
        encodeHints[EncodeHintType.MARGIN] = 1
        val matrix: BitMatrix =
            MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, encodeHints)

        val image = BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY)
        val rowPixels = IntArray(width)
        var row = BitArray(width)
        for (y in 0 until height) {
            row = matrix.getRow(y, row)
            for (x in 0 until width) {
                rowPixels[x] = if (row[x]) 1 else -1
            }
            image.setRGB(0, y, width, 1, rowPixels, 0, width)
        }
        return image
    }

}