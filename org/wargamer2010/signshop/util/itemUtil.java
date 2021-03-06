package org.wargamer2010.signshop.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.material.MaterialData;
import org.bukkit.Material;
import org.bukkit.material.SimpleAttachableMaterialData;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.bukkit.Bukkit;
import org.wargamer2010.signshop.Seller;
import org.wargamer2010.signshop.SignShop;
import org.wargamer2010.signshop.blocks.BookFactory;
import org.wargamer2010.signshop.blocks.IBookItem;
import org.wargamer2010.signshop.blocks.IItemTags;
import org.wargamer2010.signshop.blocks.SignShopBooks;
import org.wargamer2010.signshop.configuration.SignShopConfig;
import org.wargamer2010.signshop.configuration.Storage;
import org.wargamer2010.signshop.operations.SignShopOperation;
import org.wargamer2010.signshop.operations.SignShopArguments;

public class itemUtil {
    private static HashMap<Integer, String> discs;

    private itemUtil() {

    }

    public static void initDiscs() {
        // This is pretty ugly but I really couldn't find another way in
        // bukkit's source to get this via a native function
        // Source: http://www.minecraftwiki.net/wiki/Data_values
        discs = new HashMap<Integer, String>();
        discs.put(2256, "13 Disc");
        discs.put(2257, "Cat Disc");
        discs.put(2258, "Blocks Disc");
        discs.put(2259, "Chirp Disc");
        discs.put(2260, "Far Disc");
        discs.put(2261, "Mall Disc");
        discs.put(2262, "Mellohi Disc");
        discs.put(2263, "Stal Disc");
        discs.put(2264, "Strad Disc");
        discs.put(2265, "Ward Disc");
        discs.put(2266, "11 Disc");
        discs.put(2267, "wait Disc");
    }

    public static ItemStack[] getSingleAmount(ItemStack[] isItems) {
        List<ItemStack> items = new ArrayList<ItemStack>();
        for(ItemStack item: isItems) {
            ItemStack isBackup = getCraftItemstack(
                item.getType(),
                1,
                item.getDurability()
            );
            addSafeEnchantments(isBackup, item.getEnchantments());
            if(item.getData() != null) {
                isBackup.setData(item.getData());
            }
            if(!items.contains(isBackup))
                items.add(isBackup);
        }
        ItemStack[] isBackupToTake = new ItemStack[items.size()];
        int i = 0;
        for(ItemStack entry : items) {
            isBackupToTake[i] = entry;
            i++;
        }
        return isBackupToTake;
    }

    public static Boolean singeAmountStockOK(Inventory iiInventory, ItemStack[] isItemsToTake, Boolean bTakeOrGive) {
        return isStockOK(iiInventory, getSingleAmount(isItemsToTake), bTakeOrGive);
    }

    public static Boolean isStockOK(Inventory iiInventory, ItemStack[] isItemsToTake, Boolean bTakeOrGive) {
        try {
            ItemStack[] isChestItems = iiInventory.getContents();
            ItemStack[] isBackup = getBackupItemStack(isChestItems);
            ItemStack[] isBackupToTake = getBackupItemStack(isItemsToTake);
            HashMap<Integer, ItemStack> leftOver;
            if(bTakeOrGive)
                leftOver = iiInventory.removeItem(isBackupToTake);
            else
                leftOver = iiInventory.addItem(isBackupToTake);
            Boolean bStockOK = true;
            if(!leftOver.isEmpty())
                bStockOK = false;
            iiInventory.setContents(isBackup);
            return bStockOK;
        } catch(NullPointerException ex) {
            // Chest is not available, contents are NULL. So let's assume the Stock is not OK
            return false;
        }
    }

    private static String binaryToRoman(int binary) {
        final String[] RCODE = {"M", "CM", "D", "CD", "C", "XC", "L",
                                           "XL", "X", "IX", "V", "IV", "I"};
        final int[]    BVAL  = {1000, 900, 500, 400,  100,   90,  50,
                                               40,   10,    9,   5,   4,    1};
        if (binary <= 0 || binary >= 4000) {
            return "";
        }
        String roman = "";
        for (int i = 0; i < RCODE.length; i++) {
            while (binary >= BVAL[i]) {
                binary -= BVAL[i];
                roman  += RCODE[i];
            }
        }
        return roman;
    }

    private static String lookupDisc(int id) {
        if(discs.containsKey(id))
            return discs.get(id);
        else
            return "";
    }

    public static boolean isDisc(int id) {
        return (discs.containsKey(id) || id > 2267);
    }

    public static String formatData(MaterialData data) {
        Short s = 0;
        return formatData(data, s);
    }

    public static String formatData(MaterialData data, Short durability) {
        String sData;
        // Lookup spout custom material
        if(Bukkit.getServer().getPluginManager().isPluginEnabled("Spout")) {
            sData = spoutUtil.getName(data, durability);
            if(sData != null)
                return sData;
        }

        // For some reason running tostring on data when it's from an attachable material
        // will cause a NullPointerException, thus if we're dealing with an attachable, go the easy way :)
        if(data instanceof SimpleAttachableMaterialData)
            return stringFormat(data.getItemType().name());

        if(!(sData = lookupDisc(data.getItemTypeId())).isEmpty())
            return sData;
        else
            sData = data.toString().toLowerCase();
        Pattern p = Pattern.compile("\\(-?[0-9]+\\)");
        Matcher m = p.matcher(sData);
        sData = m.replaceAll("");
        sData = sData.replace("_", " ");

        StringBuffer sb = new StringBuffer(sData.length());
        p = Pattern.compile("(^|\\W)([a-z])");
        m = p.matcher(sData);
        while(m.find()) {
            m.appendReplacement(sb, m.group(1) + m.group(2).toUpperCase() );
        }

        m.appendTail(sb);

        return sb.toString();
    }

    private static String stringFormat(String sMaterial){
        sMaterial = sMaterial.replace("_"," ");
        Pattern p = Pattern.compile("(^|\\W)([a-z])");
        Matcher m = p.matcher(sMaterial.toLowerCase());
        StringBuffer sb = new StringBuffer(sMaterial.length());

        while(m.find()){
            m.appendReplacement(sb, m.group(1) + m.group(2).toUpperCase() );
        }

        m.appendTail(sb);

        return sb.toString();
    }

    public static String itemStackToString(ItemStack[] isStacks) {
        HashMap<ItemStack, Integer> items = new HashMap<ItemStack, Integer>();
        HashMap<ItemStack, Map<Enchantment,Integer>> enchantments = new HashMap<ItemStack, Map<Enchantment,Integer>>();
        String sItems = "";
        Boolean first = true;
        Integer tempAmount = 0;
        IItemTags tags = BookFactory.getItemTags();
        for(ItemStack item: isStacks) {
            if(item == null)
                continue;
            ItemStack isBackup = getCraftItemstack(
                item.getType(),
                1,
                item.getDurability()
            );
            addSafeEnchantments(isBackup, item.getEnchantments());
            if(item.getData() != null){
                isBackup.setData(item.getData());
            }
            if(itemUtil.isWriteableBook(item)) {
                tags.copyTags(item, isBackup);
            }

            if(item.getEnchantments().size() > 0)
                enchantments.put(isBackup, item.getEnchantments());
            if(items.containsKey(isBackup)) {
                tempAmount = (items.get(isBackup) + item.getAmount());
                items.put(isBackup, tempAmount);
            } else
                items.put(isBackup, item.getAmount());
        }
        for(Map.Entry<ItemStack, Integer> entry : items.entrySet()) {
            if(first) first = false;
            else sItems += ", ";
            if(enchantments.containsKey(entry.getKey()))
                sItems += ChatColor.DARK_PURPLE;
            String sDamaged = " ";
            if(entry.getKey().getType().getMaxDurability() >= 30 && entry.getKey().getDurability() != 0)
                sDamaged = " Damaged ";
            sItems += (entry.getValue()) + sDamaged + formatData(entry.getKey().getData(), entry.getKey().getDurability());
            if(enchantments.containsKey(entry.getKey())) {
                sItems += (ChatColor.WHITE + " " + enchantmentsToMessageFormat(enchantments.get(entry.getKey())));
            }
            if(itemUtil.isWriteableBook(entry.getKey())) {
                IBookItem book = BookFactory.getBookItem(entry.getKey());
                if(book != null && (book.getAuthor() != null || book.getTitle() != null))
                    sItems += (" (" + (book.getTitle() == null ? "Unkown" : book.getTitle())  + " by " + (book.getAuthor() == null ? "Unkown" : book.getAuthor()) + ")");
            }
        }

        return sItems;
    }

    public static String enchantmentsToMessageFormat(Map<Enchantment,Integer> enchantments) {
        String enchantmentMessage = "";
        Boolean eFirst = true;

        enchantmentMessage += "(";
        for(Map.Entry<Enchantment,Integer> eEntry : enchantments.entrySet()) {
            if(eFirst) eFirst = false;
            else enchantmentMessage += ", ";
            enchantmentMessage += (stringFormat(eEntry.getKey().getName()) + " " + binaryToRoman(eEntry.getValue()));
        }
        enchantmentMessage += ")";
        return enchantmentMessage;
    }

    public static void setSignStatus(Block sign, ChatColor color) {
        if(clickedSign(sign)) {
            Sign signblock = ((Sign) sign.getState());
            String[] sLines = signblock.getLines();
            if(ChatColor.stripColor(sLines[0]).length() < 14) {
                signblock.setLine(0, (color + ChatColor.stripColor(sLines[0])));
                signblock.update();
            }
        }
    }

    public static Boolean addSafeEnchantments(ItemStack isEnchantMe, Map<Enchantment, Integer> enchantments) {
        if(enchantments.isEmpty())
            return true;
        try {
            isEnchantMe.addEnchantments(enchantments);
        } catch(IllegalArgumentException ex) {
            if(SignShopConfig.getAllowUnsafeEnchantments()) {
                try {
                    isEnchantMe.addUnsafeEnchantments(enchantments);
                } catch(IllegalArgumentException exfinal) {
                    return false;
                }
            } else
                return false;
        }
        return true;
    }

    public static HashMap<ItemStack, Integer> StackToMap(ItemStack[] isStacks) {
        ItemStack[] isBackup = getBackupItemStack(isStacks);
        HashMap<ItemStack, Integer> mReturn = new HashMap<ItemStack, Integer>();
        int tempAmount = 0;
        for(int i = 0; i < isBackup.length; i++) {
            if(isBackup[i] == null) continue;
            tempAmount = isBackup[i].getAmount();
            isBackup[i].setAmount(1);
            if(mReturn.containsKey(isBackup[i])) {
                tempAmount += mReturn.get(isBackup[i]);
                mReturn.remove(isBackup[i]);
                mReturn.put(isBackup[i], tempAmount);
            } else
                mReturn.put(isBackup[i], tempAmount);
        }
        return mReturn;
    }

    public static ItemStack[] getBackupItemStack(ItemStack[] isOriginal) {
        ItemStack[] isBackup = new ItemStack[isOriginal.length];
        for(int i = 0; i < isOriginal.length; i++){
            if(isOriginal[i] != null) {
                isBackup[i] = getBackupSingleItemStack(isOriginal[i]);
            }
        }
        return isBackup;
    }

    public static ItemStack getBackupSingleItemStack(ItemStack isOriginal) {
        IItemTags tags = BookFactory.getItemTags();
        ItemStack isBackup = getCraftItemstack(
            isOriginal.getType(),
            isOriginal.getAmount(),
            isOriginal.getDurability()
        );
        itemUtil.addSafeEnchantments(isBackup, isOriginal.getEnchantments());
        if(itemUtil.isWriteableBook(isOriginal)) {
            tags.copyTags(isOriginal, isBackup);
        }

        if(isOriginal.getData() != null) {
            isBackup.setData(isOriginal.getData());
        }

        return isBackup;
    }

    public static HashMap<ItemStack[], Float> variableAmount(Inventory iiFrom, ItemStack[] isItemsToTake, Boolean bTake) {
        ItemStack[] isBackup = getBackupItemStack(isItemsToTake);
        HashMap<ItemStack[], Float> returnMap = new HashMap<ItemStack[], Float>();
        returnMap.put(isItemsToTake, 1.0f);
        Boolean fromOK = itemUtil.isStockOK(iiFrom, isBackup, true);
        IItemTags tags = BookFactory.getItemTags();
        if(fromOK) {
            returnMap.put(isItemsToTake, 1.0f);
            if(bTake)
                iiFrom.removeItem(isBackup);
            return returnMap;
        } else if(!SignShopConfig.getAllowVariableAmounts() && !fromOK) {
            returnMap.put(isItemsToTake, 0.0f);
            return returnMap;
        }
        returnMap.put(isItemsToTake, 0.0f);
        float iCount = 0;
        float tempCount;
        int i = 0;
        HashMap<ItemStack, Integer> mItemsToTake = StackToMap(isBackup);
        HashMap<ItemStack, Integer> mInventory = StackToMap(iiFrom.getContents());
        ItemStack[] isActual = new ItemStack[mItemsToTake.size()];
        for(Map.Entry<ItemStack, Integer> entry : mItemsToTake.entrySet()) {
            if(iCount == 0 && mInventory.containsKey(entry.getKey()))
                iCount = ((float)mInventory.get(entry.getKey()) / (float)entry.getValue());
            else if(iCount != 0 && mInventory.containsKey(entry.getKey())) {
                tempCount = ((float)mInventory.get(entry.getKey()) / (float)entry.getValue());
                if(tempCount != iCount)
                    return returnMap;
            } else
                return returnMap;

            isActual[i] = getCraftItemstack(
                entry.getKey().getType(),
                mInventory.get(entry.getKey()),
                entry.getKey().getDurability()
            );
            addSafeEnchantments(isActual[i], entry.getKey().getEnchantments());
            if(itemUtil.isWriteableBook(entry.getKey())) {
                tags.copyTags(entry.getKey(), isActual[i]);
            }
            if(entry.getKey().getData() != null) {
                isActual[i].setData(entry.getKey().getData());
            }
            i++;
        }
        returnMap.clear();
        returnMap.put(isActual, iCount);
        if(bTake)
            iiFrom.removeItem(isActual);
        return returnMap;
    }

    public static void updateStockStatusPerChest(Block bHolder, Block bIgnore) {
        List<Block> signs = Storage.get().getSignsFromHolder(bHolder);
        if(signs != null) {
            for (Block temp : signs) {
                if(temp == bIgnore)
                    continue;
                if(!clickedSign(temp))
                    continue;
                Seller seller = Storage.get().getSeller(temp.getLocation());
                updateStockStatusPerShop(seller);
            }
        }
    }

    public static void updateStockStatusPerShop(Seller pSeller) {
        if(pSeller != null) {
            Block pSign = Storage.get().getSignFromSeller(pSeller);
            if(pSign == null)
                return;
            String[] sLines = ((Sign) pSign.getState()).getLines();
            if(SignShopConfig.getBlocks(signshopUtil.getOperation(sLines[0])).isEmpty())
                return;
            List<String> operation = SignShopConfig.getBlocks(signshopUtil.getOperation(sLines[0]));
            Map<SignShopOperation, List<String>> SignShopOperations = signshopUtil.getSignShopOps(operation);
            if(SignShopOperations == null)
                return;
            SignShopArguments ssArgs = new SignShopArguments(economyUtil.parsePrice(sLines[3]), pSeller.getItems(), pSeller.getContainables(), pSeller.getActivatables(),
                                                                null, null, pSign, signshopUtil.getOperation(sLines[0]), null);
            if(pSeller.getMisc() != null)
                ssArgs.miscSettings = pSeller.getMisc();
            Boolean reqOK = true;
            for(Map.Entry<SignShopOperation, List<String>> ssOperation : SignShopOperations.entrySet()) {
                ssArgs.set_operationParameters(ssOperation.getValue());
                reqOK = ssOperation.getKey().checkRequirements(ssArgs, false);
                if(!reqOK) {
                    itemUtil.setSignStatus(pSign, ChatColor.DARK_RED);
                    break;
                }
            }
            if(reqOK)
                itemUtil.setSignStatus(pSign, ChatColor.DARK_BLUE);
        }
    }

    public static void updateStockStatus(Block bSign, ChatColor ccColor) {
        Seller seTemp = Storage.get().getSeller(bSign.getLocation());
        if(seTemp != null) {
            List<Block> iChests = seTemp.getContainables();
            for(Block bHolder : iChests)
                updateStockStatusPerChest(bHolder, bSign);
        }
        setSignStatus(bSign, ccColor);
    }

    public static Boolean clickedSign(Block bBlock) {
        if(bBlock.getType() == Material.getMaterial("SIGN") || bBlock.getType() == Material.getMaterial("WALL_SIGN") || bBlock.getType() == Material.getMaterial("SIGN_POST"))
            return true;
        else
            return false;
    }

    public static ItemStack[] convertStringtoItemStacks(List<String> sItems) {
        ItemStack isItems[] = new ItemStack[sItems.size()];
        for(int i = 0; i < sItems.size(); i++) {
            try {
                String[] sItemprops = sItems.get(i).split(Storage.getItemSeperator());
                if(sItemprops.length < 4)
                    continue;
                isItems[i] = getCraftItemstack(
                        Material.getMaterial(Integer.parseInt(sItemprops[1])),
                        Integer.parseInt(sItemprops[0]),
                        Short.parseShort(sItemprops[2])
                );
                isItems[i].getData().setData(new Byte(sItemprops[3]));
                if(sItemprops.length > 4)
                    addSafeEnchantments(isItems[i], signshopUtil.convertStringToEnchantments(sItemprops[4]));
                if(sItemprops.length > 5) {
                    try {
                        SignShopBooks.addBooksProps(isItems[i], Integer.parseInt(sItemprops[5]));
                        Seller.addMeta(isItems[i], Integer.parseInt(sItemprops[5]));
                    } catch(NumberFormatException ex) {

                    }
                }
            } catch(Exception ex) {
                continue;
            }
        }
        return isItems;
    }

    public static boolean isWriteableBook(ItemStack item) {
        if(item == null) return false;
        return (item.getType() == Material.getMaterial("WRITTEN_BOOK") || item.getType() == Material.getMaterial("BOOK_AND_QUILL"));
    }

    public static String[] convertItemStacksToString(ItemStack[] isItems) {
        return convertItemStacksToString(isItems, null);
    }

    public static String[] convertItemStacksToString(ItemStack[] isItems, Map<ItemStack, Integer> meta) {
        List<String> sItems = new ArrayList<String>();

        ItemStack isCurrent = null;
        for(int i = 0; i < isItems.length; i++) {
            if(isItems[i] != null) {
                isCurrent = isItems[i];
                String ID = (meta != null && meta.containsKey(isItems[i]) ? meta.get(isItems[i]).toString() : "");
                sItems.add((isCurrent.getAmount() + Storage.getItemSeperator()
                        + isCurrent.getTypeId() + Storage.getItemSeperator()
                        + isCurrent.getDurability() + Storage.getItemSeperator()
                        + isCurrent.getData().getData() + Storage.getItemSeperator()
                        + signshopUtil.convertEnchantmentsToString(isCurrent.getEnchantments()) + Storage.getItemSeperator()
                        + ID));
            }

        }
        String[] items = new String[sItems.size()];
        sItems.toArray(items);
        return items;
    }

    protected static Object tryReflection(String fullClassname, Material pMat, int pAmount, short pDurability) {
        try {
            Class<?> fc = (Class<?>)Class.forName(fullClassname);
            return fc.getConstructor(Material.class, int.class, short.class).newInstance(pMat, pAmount, pDurability);
        } catch (Exception ex) {
            // Way too many exceptions could be thrown by the statements above
            // So for the sake of my sanity, we'll just catch everything
            return null;
        }
    }

    public static ItemStack[] getCraftItemstacks(int size, Material pMat, int pAmount, short pDurability) {
        ItemStack[] stacks = new ItemStack[size];
        for(int i = 0; i < size; i++) {
            Object temp = getCraftItemstack(pMat, pAmount, pDurability);
            if(temp != null)
                stacks[i] = (ItemStack)temp;
        }
        return stacks;
    }

    public static ItemStack getCraftItemstack(Material pMat, int pAmount, short pDurability) {
        String useType;
        if(tryReflection("org.bukkit.craftbukkit.inventory.CraftItemStack", pMat, pAmount, pDurability) != null)
            useType = "org.bukkit.craftbukkit.inventory.CraftItemStack";
        else if(tryReflection("org.bukkit.craftbukkit.v1_4_5.inventory.CraftItemStack", pMat, pAmount, pDurability) != null)
            useType = "org.bukkit.craftbukkit.v1_4_5.inventory.CraftItemStack";
        else {
            SignShop.log("Could not create a CraftItemStack instance!", Level.SEVERE);
            return null;
        }
        Object temp = tryReflection(useType, pMat, pAmount, pDurability);
        if(temp != null)
            return (ItemStack)temp;
        return null;
    }
}
