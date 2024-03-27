package scripts

import com.xbaimiao.easypay.api.AbstractScriptingExtension
import com.xbaimiao.easypay.entity.Order
import com.xbaimiao.easypay.entity.PayService
import com.xbaimiao.easypay.scripting.GroovyToolkit
import org.bukkit.entity.Player

import java.util.logging.Logger

class ExampleScript extends AbstractScriptingExtension {
    private Logger logger = Logger.getLogger("EasyPay_ExampleScript")

    @Override
    void orderReward(Player player, PayService service, Order order) {
        logger.info("${player.getName()} 使用 ${service.getDisplayName()}方式 购买了 ${order.getItem().getName()}")
        player.sendMessage("您已充值 ${GroovyToolkit.parsePlaceholders(player, "%easypay_price_${player.getName()}%")}元")
    }

    @Override
    boolean preCreateOrder(Player player, PayService service, Order order) {
        logger.info("${player.getName()} 使用 ${service.getDisplayName()}方式 预创建购买 ${order.getItem().getName()} 的订单")
        return true // 返回true为允许创建此订单, false则为不允许
    }

    @Override
    void createOrder(Player player, PayService service, Order order) {
        logger.info("${player.getName()} 使用 ${service.getDisplayName()}方式 预创建购买 ${order.getItem().getName()} 的订单")
    }
}
