package com.xbaimiao.easypay.scripting

import com.xbaimiao.easylib.util.debug
import com.xbaimiao.easylib.util.info
import com.xbaimiao.easylib.util.warn
import com.xbaimiao.easypay.EasyPay
import com.xbaimiao.easypay.api.AbstractScriptingExtension
import com.xbaimiao.easypay.entity.Order
import com.xbaimiao.easypay.entity.PayService
import groovy.lang.GroovyClassLoader
import org.bukkit.entity.Player
import org.codehaus.groovy.control.CompilerConfiguration
import java.io.File

@Suppress("UNCHECKED_CAST")
class GroovyScriptManager(
    workPath: File,
    extension: String,
    enabled: Boolean
) {
    private val map: MutableMap<Class<out AbstractScriptingExtension>, String> = mutableMapOf()
    private val instances: MutableList<AbstractScriptingExtension> = mutableListOf()
    private val groovyClassLoader: GroovyClassLoader

    init {
        val compilerConfig = CompilerConfiguration()
        compilerConfig.sourceEncoding = "UTF-8"
        groovyClassLoader = GroovyClassLoader(EasyPay::class.java.classLoader, compilerConfig)
        if (!workPath.exists()) workPath.mkdirs()
        if (enabled) {
            info("开始解析Groovy脚本")
            val list = workPath.listFiles { _, name ->
                name.endsWith(".$extension")
            }?.toList() ?: emptyList<File>()
            info("已获取到${list.size}个脚本文件, 开始载入")
            list.forEach { file ->
                runCatching {
                    val clazz = groovyClassLoader.parseClass(file)
                    if (AbstractScriptingExtension::class.java.isAssignableFrom(clazz)) {
                        map[clazz as Class<out AbstractScriptingExtension>] = file.name
                        debug("已解析Groovy脚本 ${file.name} 编译后类名 ${clazz.name}")
                        instances.add(clazz.newInstance())
                        debug("已创建Groovy脚本 ${file.name} 的实例")
                    } else {
                        warn("解析Groovy脚本 ${file.name} 时 出现错误 [脚本未扩展AbstractScriptingExtension类]")
                        return@forEach
                    }
                }.onFailure {
                    warn("解析Groovy脚本 ${file.name} 时 出现错误:")
                    warn(it)
                }
            }
            info("已加载${instances.size}个Groovy脚本")
        }
        instance = this
    }

    fun createOrder(player: Player, service: PayService, order: Order) {
        instances.forEach {
            it.createOrder(player, service, order)
        }
    }

    fun preCreateOrder(player: Player, service: PayService, order: Order): Boolean {
        var boolean = true
        for (instance in instances) {
            if (!boolean) break
            boolean = instance.preCreateOrder(player, service, order)
        }
        return boolean
    }

    fun orderReward(player: Player, payService: PayService, order: Order) {
        instances.forEach {
            it.orderReward(player, payService, order)
        }
    }

    companion object {
        lateinit var instance: GroovyScriptManager
    }
}