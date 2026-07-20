package com.xbaimiao.easypay.util

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
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

    private const val LOGO_SIZE = 16

    fun generate(content: String, width: Int = 128, height: Int = 128): BufferedImage {
        val encodeHints: MutableMap<EncodeHintType, Any> = EnumMap(EncodeHintType::class.java)
        encodeHints[EncodeHintType.CHARACTER_SET] = "UTF-8"
        encodeHints[EncodeHintType.MARGIN] = 0
        val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 0, 0, encodeHints)
        return renderFullSize(matrix, width, height)
    }

    fun generate(text: String, logoFile: File, width: Int = 128, height: Int = 128): BufferedImage {
        val encodeHints: MutableMap<EncodeHintType, Any> = EnumMap(EncodeHintType::class.java)
        encodeHints[EncodeHintType.CHARACTER_SET] = "UTF-8"
        encodeHints[EncodeHintType.MARGIN] = 0
        encodeHints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H

        val writer = QRCodeWriter()
        val matrix = writer.encode(text, BarcodeFormat.QR_CODE, 0, 0, encodeHints)
        val qrImage = renderFullSize(matrix, width, height)
        val ogImage = ImageIO.read(logoFile)

        val logoImage = BufferedImage(LOGO_SIZE, LOGO_SIZE, BufferedImage.TYPE_INT_ARGB).also {
            val graphics = it.createGraphics()
            try {
                graphics.color = Color.WHITE
                graphics.fillRect(0, 0, LOGO_SIZE, LOGO_SIZE)
                graphics.drawImage(ogImage, 0, 0, LOGO_SIZE, LOGO_SIZE, null)
            } finally {
                graphics.dispose()
            }
        }

        val deltaHeight = qrImage.height - logoImage.height
        val deltaWidth = qrImage.width - logoImage.width

        val combined = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g = combined.graphics as Graphics2D
        try {
            g.drawImage(qrImage, 0, 0, null)
            g.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f)
            g.drawImage(logoImage, deltaWidth / 2, deltaHeight / 2, null)
        } finally {
            g.dispose()
        }

        return combined
    }

    private fun renderFullSize(matrix: BitMatrix, width: Int, height: Int): BufferedImage {
        val image = BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY)
        val rowPixels = IntArray(width)
        for (y in 0 until height) {
            val matrixY = y * matrix.height / height
            for (x in 0 until width) {
                val matrixX = x * matrix.width / width
                rowPixels[x] = if (matrix[matrixX, matrixY]) Color.BLACK.rgb else Color.WHITE.rgb
            }
            image.setRGB(0, y, width, 1, rowPixels, 0, width)
        }
        return image
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
