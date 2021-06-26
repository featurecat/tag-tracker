package me.windcat.tagtracker;

import net.minecraft.server.v1_16_R3.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.AbstractMap;
import java.util.ArrayList;

public class TagTracker extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, this::updateCompasses, 1L, 5L);
    }

    public void updateCompasses() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            ArrayList<AbstractMap.SimpleImmutableEntry<ItemStack, String>> compasses = getCompasses(player);
            for (AbstractMap.SimpleImmutableEntry<ItemStack, String> entry : compasses) {
                CompassMeta meta = (CompassMeta) entry.getKey().getItemMeta();
                meta.setLodestoneTracked(false);
                meta.setLodestone(findNearestPlayerWithTag(player, entry.getValue()));
                entry.getKey().setItemMeta(meta);
            }
        }
    }

    public ArrayList<AbstractMap.SimpleImmutableEntry<ItemStack, String>> getCompasses(Player player) {
        ArrayList<AbstractMap.SimpleImmutableEntry<ItemStack, String>> tagTrackerCompasses = new ArrayList<>();
        Inventory inventory = player.getInventory();
        for (ItemStack item : inventory.getContents()) {
            if (item != null && !item.getType().equals(Material.COMPASS)) continue;
            net.minecraft.server.v1_16_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
            if (!nmsItem.hasTag()) continue;
            NBTTagCompound tag = nmsItem.getTag();
            String tagTrackerString = tag.getString("TagTracker");
            if (!tagTrackerString.isEmpty()) {
                tagTrackerCompasses.add(new AbstractMap.SimpleImmutableEntry<>(item, tagTrackerString));
            }
        }
        return tagTrackerCompasses;
    }

    public Location findNearestPlayerWithTag(Player player, String tag) {
        Location nearestLocation = null;
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (player == target || !player.getWorld().equals(target.getWorld())) {
                continue;
            }
            if (target.getScoreboardTags().contains(tag)) {
                if (nearestLocation == null || player.getLocation().distance(target.getLocation()) < player.getLocation().distance(nearestLocation)) {
                    nearestLocation = target.getLocation();
                }
            }
        }
        return nearestLocation;
    }
}
