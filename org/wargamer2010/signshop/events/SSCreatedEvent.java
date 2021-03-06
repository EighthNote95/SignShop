package org.wargamer2010.signshop.events;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.wargamer2010.signshop.player.SignShopPlayer;

public class SSCreatedEvent extends SSEvent {
    private static final HandlerList handlers = new HandlerList();

    private Float fPrice = -1.0f;
    private ItemStack[] isItems = null;
    private List<Block> containables = null;
    private List<Block> activatables = null;
    private SignShopPlayer ssPlayer = null;
    private Block bSign = null;
    private String sOperation = "";
    private Map<String, String> messageParts = new HashMap<String, String>();

    public SSCreatedEvent(Float pPrice, ItemStack[] pItems, List<Block> pContainables, List<Block> pActivatables, SignShopPlayer pPlayer, Block pSign, String pOperation, Map<String, String> pMessageParts) {
        fPrice = pPrice;
        isItems = pItems;
        containables = pContainables;
        activatables = pActivatables;
        ssPlayer = pPlayer;
        bSign = pSign;
        sOperation = pOperation;
        messageParts = pMessageParts;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Float getPrice() {
        return fPrice;
    }

    public ItemStack[] getItems() {
        return isItems;
    }

    public List<Block> getContainables() {
        return containables;
    }

    public List<Block> getActivatables() {
        return activatables;
    }

    public SignShopPlayer getPlayer() {
        return ssPlayer;
    }

    public Block getSign() {
        return bSign;
    }

    public String getOperation() {
        return sOperation;
    }

    public Map<String, String> getMessageParts() {
        return messageParts;
    }
}
