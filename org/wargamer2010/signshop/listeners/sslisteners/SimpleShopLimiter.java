
package org.wargamer2010.signshop.listeners.sslisteners;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.events.SSPreCreatedEvent;
import org.wargamer2010.signshop.util.itemUtil;

public class SimpleShopLimiter implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onSSBuildEvent(SSPreCreatedEvent event) {
        if(event.isCancelled())
            return;
        int iLimit = event.getPlayer().reachedMaxShops();
        if(!SignShopConfig.getBlocks(event.getOperation()).contains("playerIsOp") && iLimit > 0) {
            event.getPlayer().sendMessage(SignShopConfig.getError("too_many_shops", null).replace("!max", Integer.toString(iLimit)));
            itemUtil.setSignStatus(event.getSign(), ChatColor.BLACK);
            event.setCancelled(true);
        }
    }
}