package com.example.myplugin.listener;

import com.example.myplugin.MyPlugin;
import com.example.myplugin.enums.ShopCategory;
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
        // Only care about our shop entity
        if (!plugin.getShopManager().isShopEntity(event.getRightClicked().getUniqueId())) return;

        // PlayerInteractEntityEvent fires for both hands — only handle the main hand
        if (event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND) return;

        event.setCancelled(true); // prevent default interact behaviour

        Player player = event.getPlayer();

        // Only players in the game can use the shop
        if (!plugin.getPlayerManager().isInGame(player.getUniqueId())) {
            player.sendMessage(Component.text("You must be in a game to use the shop.", NamedTextColor.RED));
            return;
        }

        // Open a fresh ShopInventory for this player
        ShopInventory shop = new ShopInventory(plugin.getShopManager().getCategoryItems());
        player.openInventory(shop.getInventory());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Check the top inventory (the shop) is ours — ignore all other inventories
        if (!(event.getInventory().getHolder() instanceof ShopInventory shop)) return;

        event.setCancelled(true); // always cancel — items must NEVER be dragged out

        // Ignore clicks in the player's own bottom inventory
        if (event.getRawSlot() >= 54) return;

        // Ignore null clicks (clicking empty space)
        if (event.getCurrentItem() == null) return;

        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();

        if (slot <= 8) {
            handleNavClick(slot, shop);
        } else if (slot >= 18) {
            handleItemPurchase(slot, shop, player);
        }
        // slots 9-17 are the border — do nothing
    }

    private void handleNavClick(int slot, ShopInventory shop) {
        ShopCategory[] categories = ShopCategory.values();

        // Slot 0 = first category, slot 1 = second, etc.
        if (slot < categories.length) {
            shop.showCategory(categories[slot]);
        }
    }

    private void handleItemPurchase(int slot, ShopInventory shop, Player player) {
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
            com.example.myplugin.player.PlayerData data = plugin.getPlayerManager().getPlayer(player.getUniqueId());
            if (data != null && data.getTeam() != null) {
                material = data.getTeam().getWoolMaterial();
            }
        }

        if(material == Material.TERRACOTTA) {
            PlayerData data = plugin.getPlayerManager().getPlayer(player.getUniqueId());
            if(data != null && data.getTeam() != null) {
                material = data.getTeam().getTerracottaMaterial();
            }
        }

        inv.addItem(new ItemStack(material, shopItem.getQuantity()));
        player.sendMessage(Component.text("Purchased!", NamedTextColor.GREEN));
    }

    private String friendlyName(Material mat) {
        return switch (mat) {
            case IRON_INGOT -> "Iron";
            case GOLD_INGOT -> "Gold";
            default -> mat.name();
        };
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!plugin.getShopManager().isShopEntity(event.getEntity().getUniqueId())) return;
        event.setCancelled(true);
    }
}
