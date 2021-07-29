package net.foulest.kitpvp.listeners;

import net.foulest.fstaff.events.StaffModeEvent;
import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.region.Spawn;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * @author Foulest
 * @project KitPvP
 * <p>
 * Hooks into very specific staff mode plugin I made.
 * Safe to remove.
 */
public class StaffModeListener implements Listener {

    private static final Spawn SPAWN = Spawn.getInstance();

    @EventHandler(priority = EventPriority.MONITOR)
    public static void onStaffMode(StaffModeEvent event) {
        Player player = Bukkit.getPlayer(event.getUUID());
        PlayerData playerData = PlayerData.getInstance(player);

        if (playerData == null) {
            player.kickPlayer("Disconnected");
            return;
        }

        if (event.isInStaffMode()) {
            playerData.setKit(null);
        } else {
            SPAWN.teleport(player);
        }
    }
}
