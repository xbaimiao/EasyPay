package com.xbaimiao.easypay.util

import com.xbaimiao.easylib.EasyPlugin
import com.xbaimiao.easylib.util.info
import com.xbaimiao.easylib.util.warn
import com.xbaimiao.easypay.EasyPay
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.nio.charset.StandardCharsets


object ConfigurationPatcher {
    private val ignoreKeys = listOf("items")
    fun patchConfiguration(source: String, target: String) {
        val plugin = EasyPlugin.getPlugin<EasyPay>()
        val inputStream = plugin.getResource(source)
        if (inputStream == null) {
            warn("无法读取JAR中的资源: $source")
            return
        }
        val configString = inputStream.readBytes().toString(charset = StandardCharsets.UTF_8)
        val patchConfig = YamlConfiguration()
        patchConfig.loadFromString(configString)

        var changed = false

        val targetFile = File(plugin.dataFolder, target)
        val targetConfig = YamlConfiguration.loadConfiguration(targetFile)
        for (key in patchConfig.getKeys(true)) {
            if (ignoreKeys.any { key.startsWith(it) }) continue
            if (targetConfig.get(key) == null) {
                info("[ConfigurationPatcher] 更新配置节点 $key")
                targetConfig.set(key, patchConfig[key])
                patchComments(patchConfig, targetConfig, key)
                changed = true
            }
        }
        if (changed) targetConfig.save(targetFile)
    }

    private fun patchComments(patchConfig: YamlConfiguration, targetConfig: YamlConfiguration, key: String) {
        try {
            val getCommentsMethod =
                ConfigurationSection::class.java.getDeclaredMethod("getComments", String::class.java)
            val setCommentsMethod =
                ConfigurationSection::class.java.getDeclaredMethod("setComments", String::class.java, List::class.java)

            val list = getCommentsMethod.invoke(patchConfig, key) as List<*>
            setCommentsMethod.invoke(targetConfig, key, list)
        } catch (ignore: NoSuchMethodException) {
            return
        }
    }
}