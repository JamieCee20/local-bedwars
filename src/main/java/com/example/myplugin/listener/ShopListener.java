package com.example.myplugin.listener;

import com.example.myplugin.MyPlugin;
import com.example.myplugin.enums.ShopCategory;
import com.example.myplugin.game.GameInstance;
import com.example.myplugin.player.PlayerData;
import com.example.myplugin.ui.ShopInventory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class ShopListener implements Listener {
    private final MyPlugin plugin;

    public ShopListener(MyPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        // Find the player's game — only in-game players can use the shop
        GameInstance instance = plugin.getInstanceManager().getInstanceForPlayer(event.getPlayer().getUniqueId());
        if (instance == null) return;
        if (!instance.getShopManager().isShopEntity(event.getRightClicked().getUniqueId())) return;
        if (event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) return;
        event.setCancelled(true);

        Player player = event.getPlayer();
        ShopInventory shop = new ShopInventory(instance.getShopManager().getCategoryItems());
        player.openInventory(shop.getInventory());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof ShopInventory shop)) return;
        event.setCancelled(true);
        if (event.getRawSlot() >= 54 || event.getCurrentItem() == null) return;

        Player player = (Player) event.getWhoClicked();

        // Need the instance to get the player's team (for wool/terracotta colour)
        GameInstance instance = plugin.getInstanceManager().getInstanceForPlayer(player.getUniqueId());
        if (instance == null) return;

        int slot = event.getRawSlot();
        if (slot <= 8) handleNavClick(slot, shop);
        else if (slot >= 18) handleItemPurchase(slot, shop, player, instance);
    }

    private void handleNavClick(int slot, ShopInventory shop) {
        ShopCategory[] categories = ShopCategory.values();

        // Slot 0 = first category, slot 1 = second, etc.
        if (slot < categories.length) {
            shop.showCategory(categories[slot]);
        }
    }

    private void handleItemPurchase(int slot, ShopInventory shop, Player player, GameInstance instance) {
        // Ask the shop which ShopItem sits at this slot for the current category
        com.example.myplugin.data.ShopItem shopItem = shop.getItemAt(slot);
        if (shopItem == null) return;

        PlayerInventory inv = player.getInventory();
        ItemStack cost = new ItemStack(shopItem.getCurrency(), shopItem.getPrice());

        // Check player can afford it
        if (!inv.containsAtLeast(new ItemStack(shopItem.getCurrency()), shopItem.getPrice())) {
            player.sendMessage(Component.text(
                "You need " + shopItem.getPrice() + " " + friendlyName(shopItem.getCurrency()) + "!",
                NamedTextColor.RED));
            return;
        }

        inv.removeItem(cost);

        Material material = shopItem.getMaterial();
        if (material == Material.WHITE_WOOL) {
            com.example.myplugin.player.PlayerData data = instance.getPlayerManager().getPlayer(player.getUniqueId());
            if (data != null && data.getTeam() != null) {
                material = data.getTeam().getWoolMaterial();
            }
        }

        if(material == Material.TERRACOTTA) {
            PlayerData data = instance.getPlayerManager().getPlayer(player.getUniqueId());
            if(data != null && data.getTeam() != null) {
                material = data.getTeam().getTerracottaMaterial();
            }
        }

        if(material == Material.GLASS) {
            PlayerData data = instance.getPlayerManager().getPlayer(player.getUniqueId());
            if(data != null && data.getTeam() != null) {
                material = data.getTeam().getGlassMaterial();
            }
        }

        inv.addItem(new ItemStack(material, shopItem.getQuantity()));
        player.sendMessage(Component.text("Purchased!", NamedTextColor.GREEN));
    }

    private String friendlyName(Material mat) {
        return switch (mat) {
            case IRON_INGOT -> "Iron";
            case GOLD_INGOT -> "Gold";
            case DIAMOND    -> "Diamond";
            case EMERALD    -> "Emerald";
            default -> mat.name();
        };
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        // Check every instance — the shop entity might belong to any game
        for (var instance : plugin.getInstanceManager().getInstances()) {
            if (instance.getShopManager().isShopEntity(event.getEntity().getUniqueId())) {
                event.setCancelled(true);
                return;
            }
        }
    }
}
