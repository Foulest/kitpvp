package net.foulest.kitpvp.listeners;

import com.lunarclient.bukkitapi.LunarClientAPI;
import com.lunarclient.bukkitapi.nethandler.client.LCPacketCooldown;
import net.foulest.kitpvp.KitPvP;
import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.util.MessageUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * @author Foulest
 * @project KitPvP
 */
public class CombatLog {

    private static final Map<Player, BukkitTask> COMBAT_SCHEDULER = new HashMap<>();
    private static final Map<Player, Integer> COMBAT_HANDLER = new HashMap<>();
    private static final Map<Player, Player> LAST_ATTACKER = new HashMap<>();
    private static final LunarClientAPI LUNAR_API = LunarClientAPI.getInstance();
    private static final KitPvP KITPVP = KitPvP.getInstance();

    public static void markForCombat(Player damager, Player receiver) {
        List<Player> players = new ArrayList<>(Arrays.asList(damager, receiver));

        for (Player player : players) {
            PlayerData playerData = PlayerData.getInstance(player);

            if (LUNAR_API.isRunningLunarClient(player)) {
                LUNAR_API.sendPacket(player, new LCPacketCooldown("Combat Tag", 15000L, Material.IRON_SWORD.getId()));
            }

            // Handles combat tagging for the receiver.
            if (!isInCombat(player)) {
                COMBAT_HANDLER.put(player, 15);

                COMBAT_SCHEDULER.put(player, new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (isInCombat(player)) {
                            if (getRemainingTime(player) > 1) {
                                COMBAT_HANDLER.replace(player, getRemainingTime(player), getRemainingTime(player) - 1);
                            } else {
                                remove(player);
                            }
                        }
                    }
                }.runTaskTimer(KITPVP, 0L, 20L));
            } else {
                COMBAT_HANDLER.replace(player, getRemainingTime(player), 15);
            }

            // Cancels the damager's pending teleportation when taking damage for.
            if (playerData.getTeleportingToSpawn() != null) {
                MessageUtil.messagePlayer(player, MessageUtil.colorize("&cTeleportation cancelled, you entered combat."));
                playerData.getTeleportingToSpawn().cancel();
                playerData.setTeleportingToSpawn(null);
            }
        }

        // Sets the last attackers.
        LAST_ATTACKER.put(receiver, damager);
    }

    public static boolean isInCombat(Player player) {
        return COMBAT_HANDLER.containsKey(player);
    }

    public static int getRemainingTime(Player player) {
        return !isInCombat(player) ? -1 : COMBAT_HANDLER.get(player);
    }

    public static Player getLastAttacker(Player player) {
        return LAST_ATTACKER.get(player);
    }

    public static void remove(Player player) {
        COMBAT_HANDLER.remove(player);

        if (COMBAT_SCHEDULER.containsKey(player)) {
            COMBAT_SCHEDULER.get(player).cancel();
            COMBAT_SCHEDULER.remove(player);
        }

        LAST_ATTACKER.remove(player);

        if (LUNAR_API.isRunningLunarClient(player)) {
            LUNAR_API.sendPacket(player, new LCPacketCooldown("Combat Tag", 0L, Material.IRON_SWORD.getId()));
        }
    }
}
