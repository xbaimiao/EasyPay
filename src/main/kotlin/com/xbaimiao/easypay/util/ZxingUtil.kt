package com.xbaimiao.easypay.util

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.common.BitArray
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.xbaimiao.easylib.util.plugin
import java.awt.AlphaComposite
import java.awt.Color
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import javax.imageio.ImageIO


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

    fun generate(text: String, logoFile: File, width: Int = 128, height: Int = 128): BufferedImage {
        val encodeHints: MutableMap<EncodeHintType, Any> = EnumMap(EncodeHintType::class.java)
        encodeHints[EncodeHintType.CHARACTER_SET] = "UTF-8"
        encodeHints[EncodeHintType.MARGIN] = 1
        encodeHints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H

        val writer = QRCodeWriter()
        val matrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height, encodeHints)
        val qrImage: BufferedImage = MatrixToImageWriter.toBufferedImage(matrix)
        val ogImage = ImageIO.read(logoFile)

        val logoImage = BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB).also {
            // full white
            it.graphics.color = Color.WHITE
            it.graphics.fillRect(0, 0, 32, 32)
            it.graphics.drawImage(ogImage, 0, 0, 32, 32, null)
        }

        val deltaHeight = qrImage.height - logoImage.height
        val deltaWidth = qrImage.width - logoImage.width

        val combined = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g = combined.graphics as Graphics2D

        g.drawImage(qrImage, 0, 0, null)
        g.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f)

        g.drawImage(
            logoImage,
            Math.round((deltaWidth / 2).toFloat()), Math.round((deltaHeight / 2).toFloat()), null
        )

        return combined
    }

    val alipayLogo by lazy {
        val fileObj = File(plugin.dataFolder, "icon${File.separator}alipay.png")
        if (!fileObj.exists()) {
            if (!fileObj.parentFile.exists()) {
                fileObj.parentFile.mkdirs()
            }
            plugin.saveResource("icon/alipay.png", false)
        }
        fileObj
    }

    val wechatLogo by lazy {
        val fileObj = File(plugin.dataFolder, "icon${File.separator}wechat.png")
        if (!fileObj.exists()) {
            if (!fileObj.parentFile.exists()) {
                fileObj.parentFile.mkdirs()
            }
            plugin.saveResource("icon/wechat.png", false)
        }
        fileObj
    }


}