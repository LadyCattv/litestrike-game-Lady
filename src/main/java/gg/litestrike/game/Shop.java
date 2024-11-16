package gg.litestrike.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

import static net.kyori.adventure.text.format.NamedTextColor.WHITE;
import static org.bukkit.Material.EMERALD;

public class Shop implements InventoryHolder {
    public List<LSItem> shopItems;
    public Inventory currentView;
    public Player player;
    public List<LSItem> buyHistory;
    public static HashMap<String, Shop> shopList = new HashMap<>();

    public Shop(Player p) {
        if(p == null){
            return;
        }
        shopItems = LSItem.createItems();
        player = p;
        currentView = Bukkit.getServer().createInventory(this, 54, title(p));
        buyHistory = new ArrayList<>();
            //TODO make the Shop its own item
    }

    public static Shop getShop(Player p){
        return shopList.get(p.getName());
    }

    public static void setItems(Player p, List<LSItem> ware){
        for(LSItem i : ware) {
            LSItem.updateDescription(p, i);
        }
        Shop s = getShop(p);
        Inventory i = s.currentView;
        for (LSItem item : ware) {
            Component itemDisplayName = item.item.displayName();
            PlainTextComponentSerializer plainSerializer = PlainTextComponentSerializer.plainText();
            String itemName = plainSerializer.serialize(itemDisplayName);
            switch (itemName) {
                case "[Diamond Chestplate]": {
                    i.setItem(31, item.displayItem);
                }
                case "[Iron Sword]": {
                    i.setItem(0, item.displayItem);
                }
                case "[Iron Axe]": {
                    i.setItem(2, item.displayItem);
                }
                case "[Arrow]": {
                    i.setItem(50, item.displayItem);
                }
                case "[Golden Apple]": {
                    i.setItem(49, item.displayItem);
                }
                case "[Iron Chestplate]": {
                    i.setItem(40, item.displayItem);
                }
                case "[Quickdraw Crossbow]": {
                    i.setItem(24, item.displayItem);
                }
                case "[Pufferfish Sword]": {
                    i.setItem(18, item.displayItem);
                }
                case "[Slime Sword]": {
                    i.setItem(20, item.displayItem);
                }
                case "[Multishot Crossbow]": {
                    p.sendMessage("set multishot crossbow");
                    i.setItem(26, item.displayItem);
                }
                case "[Marksman Bow]": {
                    i.setItem(6, item.displayItem);
                }
                case "[Ricochet Bow]": {
                    i.setItem(20, item.displayItem);
                }
            }
        }
    }

    public static void setDefuser(Player p){
        if(Teams.get_team(p) == Team.Breaker){
            Shop s = getShop(p);
            s.currentView.setItem(22, s.shopItems.get(6).displayItem);
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return Bukkit.getServer().createInventory(this, 1);
    }



    public static Component title(Player p){
            PlayerData pd = Litestrike.getInstance().game_controller.getPlayerData(p);
            return Component.text("\uA000" + "\uA001" +"\uE104"+ pd.getMoney()).color(WHITE);
    }

    public void updateTitle(Player p){
        Shop s = Shop.getShop(p);
        s.currentView.close();
        s.currentView = Bukkit.getServer().createInventory(this, 54, title(p));
        p.openInventory(s.currentView);
        setItems(p, s.shopItems);
        setDefuser(p);
    }

    public static void giveShop(){
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.getInventory().addItem(new ItemStack(Material.EMERALD, 1));
        }
    }

    public static void createShop(Player p){
        shopList.put(p.getName(), new Shop(p));
    }

    public static int findInvIndex(Player p, LSItem item){
        String cate = item.categ.toString();
        for (int i = 0; i <= 40; i++) {
            ItemStack it = p.getInventory().getItem(i);
            if(it == null){
                continue;
            }
            if(Objects.equals(LSItem.getItemCategory(it), cate)){
                return i;
            }
        }
        return -1;
    }

    public static void giveDefaultArmor(Player p) {
            if(Teams.get_team(p) == Team.Placer) {
                PlayerInventory inv = p.getInventory();
                inv.setHelmet(colorArmor(Color.fromRGB(0xe31724), new ItemStack(Material.LEATHER_HELMET)));
                inv.setChestplate(colorArmor(Color.fromRGB(0xe31724), new ItemStack(Material.LEATHER_CHESTPLATE)));
                inv.setLeggings(colorArmor(Color.fromRGB(0xe31724), new ItemStack(Material.LEATHER_LEGGINGS)));
                inv.setBoots(colorArmor(Color.fromRGB(0xe31724), new ItemStack(Material.LEATHER_BOOTS)));
                inv.setItem(0, new ItemStack(Material.STONE_SWORD));
                inv.setItem(1, new ItemStack(Material.BOW));
                inv.setItem(2, new ItemStack(Material.ARROW, 6));
            }else if(Teams.get_team(p) == Team.Breaker) {
                PlayerInventory inv = p.getInventory();
                inv.setHelmet(colorArmor(Color.fromRGB(0x0f9415), new ItemStack(Material.LEATHER_HELMET)));
                inv.setChestplate(colorArmor(Color.fromRGB(0x0f9415), new ItemStack(Material.LEATHER_CHESTPLATE)));
                inv.setLeggings(colorArmor(Color.fromRGB(0x0f9415), new ItemStack(Material.LEATHER_LEGGINGS)));
                inv.setBoots(colorArmor(Color.fromRGB(0x0f9415), new ItemStack(Material.LEATHER_BOOTS)));
                inv.setItem(0, new ItemStack(Material.STONE_SWORD));
                inv.setItem(1, new ItemStack(Material.BOW));
                inv.setItem(2, new ItemStack(Material.ARROW, 6));
                inv.addItem(new ItemStack(Material.STONE_PICKAXE));
            }
    }

    public static ItemStack colorArmor(Color c, ItemStack i) {
        LeatherArmorMeta lam = (LeatherArmorMeta) i.getItemMeta();
        lam.setColor(c);
        i.setItemMeta(lam);
        return i;
    }

    public static boolean alreadyHasThis(Player p, ItemStack item){
        for (int i = 0; i <= 40; i++) {
            Component it = Objects.requireNonNull(p.getInventory().getItem(i)).displayName();
            Component ite = item.displayName();
            PlainTextComponentSerializer plainSerializer = PlainTextComponentSerializer.plainText();
            String itName = plainSerializer.serialize(it);
            String iteName = plainSerializer.serialize(ite);
            if(it == null){
                continue;
            }
            if(itName.equals(iteName)){
                return true;
            }
        }
        return false;
    }

    public static void removeShop(Player p){
        Inventory inv = p.getInventory();
        Shop s = getShop(p);
        for (int i = 0; i <= 40; i++) {
            ItemStack it = p.getInventory().getItem(i);
            if (it == null) {
                continue;
            }
            if (it.getType() == Material.EMERALD){
                inv.clear(i);
                s.currentView.close();
            }
        }
    }
}
