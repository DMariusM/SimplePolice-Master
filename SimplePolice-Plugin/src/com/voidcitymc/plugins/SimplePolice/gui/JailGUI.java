package com.voidcitymc.plugins.SimplePolice.gui;

import com.voidcitymc.plugins.SimplePolice.config.ConfigValues;
import com.voidcitymc.plugins.SimplePolice.events.Jail;
import com.voidcitymc.plugins.SimplePolice.Utility;
import com.voidcitymc.plugins.SimplePolice.LegacyUtils;
import com.voidcitymc.plugins.SimplePolice.messages.Messages;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class JailGUI implements Listener {

    private static final String guiName = "Select Jail Time";
    public static HashMap<String, String> lastArrest = new HashMap<>();
    public static HashMap<String, String> currentJail = new HashMap<>();

    @EventHandler
    public void gui(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        final Player player = (Player) event.getWhoClicked();
        if (player.getOpenInventory().getTitle().equalsIgnoreCase(guiName)) {
            event.setCancelled(true);

            final ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                UUID jailedPlayer = UUID.fromString(lastArrest.get(player.getUniqueId().toString()));

                // dam jail imediat pentru 15 minute
                Jail.jailPlayer(jailedPlayer, 900.0, currentJail.get(jailedPlayer.toString()));

                currentJail.remove(jailedPlayer.toString());
                lastArrest.remove(player.getUniqueId().toString());
                // scoatem linia care inchide inventory-ul
                // player.closeInventory();
            }
        }
    }


    @EventHandler
    public void preventShiftgui(InventoryMoveItemEvent event) {
        if ((event.getSource().getHolder() instanceof Player) && ((Player) event.getSource().getHolder()).getOpenInventory().getTitle().equalsIgnoreCase(guiName)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void inventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer().getOpenInventory().getTitle().equalsIgnoreCase(guiName) &&
                lastArrest.containsKey(event.getPlayer().getUniqueId().toString())) {
            openInventory((Player) event.getPlayer());
        }
    }


    public void openInventory(final Player player) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("SimplePolice"), () -> player.openInventory(createGUI(player)), 1L);

    }


    public Inventory createGUI(Player player) {
        Inventory guiInventory = Bukkit.createInventory(null, 9, guiName);

        ItemStack guiItemStack = LegacyUtils.getStainedClay(DyeColor.RED); // sticla slot

        double[] jailGUITimes = new double[]{900.0}; // punem doar 15 minute

        for (int i = 2; i < 3; i++) {
            guiInventory.setItem(i, Utility.createGuiItem(guiItemStack, Messages.getMessage("JailGUIBlock", String.valueOf(jailGUITimes[0]))));
        }

        return guiInventory;
    }

    public static void onPlayerArrest(Player police, Player arrestedPlayer, String jailName) {
        UUID arrestedPlayerUUID = arrestedPlayer.getUniqueId();
        double jailTime = 900.0; // punem 15 minute, 900 sec = 15 minute

        Jail.jailPlayer(arrestedPlayerUUID, jailTime, jailName); // punem jucatorul in jail direct

        lastArrest.put(police.getUniqueId().toString(), arrestedPlayerUUID.toString());
        currentJail.put(arrestedPlayerUUID.toString(), jailName);
    }
}
