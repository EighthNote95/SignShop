package org.wargamer2010.signshop.operations;

import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Lever;
import org.bukkit.block.Block;
import org.wargamer2010.signshop.util.signshopUtil;
import org.wargamer2010.signshop.configuration.SignShopConfig;

public class toggleRedstone implements SignShopOperation {    
    @Override
    public Boolean setupOperation(SignShopArguments ssArgs) {
        Boolean foundLever = false;
        for(Block block : ssArgs.get_activatables())
            if(block.getType() == Material.getMaterial("LEVER"))
                foundLever = true;
        if(!foundLever) {
            ssArgs.get_ssPlayer().sendMessage(SignShopConfig.getError("lever_missing", ssArgs.messageParts));            
            return false;
        }
        return true;
    }
    
    @Override
    public Boolean checkRequirements(SignShopArguments ssArgs, Boolean activeCheck) {
        return true;
    }
    
    @Override
    public Boolean runOperation(SignShopArguments ssArgs) {
        Block bLever = null;
        
        for(int i = 0; i < ssArgs.get_activatables().size(); i++) {
            bLever = ssArgs.get_activatables().get(i);
            if(bLever.getType() == Material.getMaterial("LEVER")) {
                BlockState state = bLever.getState();
                MaterialData data = state.getData();                                        
                Lever lever = (Lever)data;                               
                if(!lever.isPowered())
                    lever.setPowered(true);                       
                else
                    lever.setPowered(false);
                state.setData(lever);
                state.update();
                signshopUtil.generateInteractEvent(bLever, ssArgs.get_ssPlayer().getPlayer(), ssArgs.get_bfBlockFace());
            }
        }

        return true;
    }
}
