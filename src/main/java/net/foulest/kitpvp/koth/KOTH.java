package net.foulest.kitpvp.koth;

import com.lunarclient.bukkitapi.LunarClientAPI;
import com.lunarclient.bukkitapi.nethandler.shared.LCPacketWaypointAdd;
import com.lunarclient.bukkitapi.object.LCWaypoint;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import lombok.Getter;
import lombok.Setter;
import me.neznamy.tab.api.TABAPI;
import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.util.Settings;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class KOTH {

    private static final LunarClientAPI LUNAR_API = LunarClientAPI.getInstance();
    public static BukkitTask countdown = null;
    public static BukkitTask kothRefresh = null;
    public static List<KOTH> kothList = new ArrayList<>();
    public static Map<KOTH, ArrayList<Player>> playersInKOTH = new HashMap<>();
    private final String internalName;
    private final String name;
    private final String regionName;
    private final int capTime;
    private final int coinReward;
    private final int expReward;
    private final int locX;
    private final int locY;
    private final int locZ;
    private Player capper = null;
    private Player lastCapper = null;
    private boolean contested = false;
    private boolean active = false;
    private int timeLeft = 9999;
    private World world;

    public KOTH(String internalName, String name, String regionName, int capTime, int coinReward, int expReward) {
        this.internalName = internalName;
        this.name = name;
        this.regionName = regionName;
        this.capTime = capTime;
        this.coinReward = coinReward;
        this.expReward = expReward;
        ProtectedRegion region = WorldGuardPlugin.inst().getRegionManager(Bukkit.getWorld(Settings.spawnWorld)).getRegion(regionName);
        world = Bukkit.getWorld(Settings.spawnWorld);

        if (region == null) {
            MessageUtil.log("&c[KitPvP] Region '" + regionName + "' doesn't exist.");
            this.locX = 0;
            this.locY = 0;
            this.locZ = 0;
            return;
        }

        BlockVector regionMin = region.getMinimumPoint();
        BlockVector regionMax = region.getMaximumPoint();
        this.locX = (int) (regionMin.getX() + regionMax.getX()) / 2;
        this.locY = (int) (regionMin.getY() + regionMax.getY()) / 2;
        this.locZ = (int) (regionMin.getZ() + regionMax.getZ()) / 2;
    }

    public static void loadKoths() {
        if (!Settings.config.getConfigurationSection("koth").getKeys(false).isEmpty()) {
            kothList.clear();

            for (String internalName : Settings.config.getConfigurationSection("koth").getKeys(false)) {
                kothList.add(new KOTH(internalName, Settings.config.getString("koth." + internalName + ".name"),
                        Settings.config.getString("koth." + internalName + ".region-name"),
                        Settings.config.getInt("koth." + internalName + ".cap-time"),
                        Settings.config.getInt("koth." + internalName + ".coin-reward"),
                        Settings.config.getInt("koth." + internalName + ".exp-reward")));
            }
        }
    }

    public static KOTH getActiveKoth() {
        for (KOTH koth : kothList) {
            if (koth.isActive()) {
                return koth;
            }
        }

        return null;
    }

    public static void addNewKOTH(String internalName, String name, String regionName,
                                  int capTime, int coinReward, int expReward) {
        Settings.config.set("koth." + internalName + ".name", name);
        Settings.config.set("koth." + internalName + ".region-name", regionName);
        Settings.config.set("koth." + internalName + ".cap-time", capTime);
        Settings.config.set("koth." + internalName + ".coin-reward", coinReward);
        Settings.config.set("koth." + internalName + ".exp-reward", expReward);
        Settings.saveSettings();
        Settings.loadSettings();
    }

    public static void deleteKOTH(KOTH koth) {
        if (koth.isActive()) {
            endKOTH(koth, null);
        }

        if (Settings.config.get("koth." + koth.getInternalName()) != null) {
            Settings.config.set("koth." + koth.getInternalName(), null);
        }

        Settings.saveSettings();
        Settings.loadSettings();
    }

    public static void startKOTH(KOTH koth) {
        MessageUtil.broadcastMessage("");
        MessageUtil.broadcastMessage("&9[KOTH] &6" + koth.getName() + " &eKOTH has been started." +
                " &7(" + koth.getLocX() + ", " + koth.getLocY() + ", " + koth.getLocZ() + ")");
        MessageUtil.broadcastMessage("");

        koth.setActive(true);
        koth.setCapper(null);
        koth.setContested(false);
        koth.setTimeLeft(koth.getCapTime());

        for (Player player : Bukkit.getOnlinePlayers()) {
            TABAPI.getPlayer(player.getUniqueId()).showScoreboard("KOTH");

            if (LUNAR_API.isRunningLunarClient(player)) {
                LUNAR_API.sendPacket(player, new LCPacketWaypointAdd("KOTH", koth.getWorld().getName(),
                        Color.NAVY.asRGB(), koth.getLocX(), koth.getLocY(), koth.getLocZ(), true, true));
            }
        }
    }

    public static void endKOTH(KOTH koth, Player capper) {
        MessageUtil.broadcastMessage("");

        if (capper == null) {
            MessageUtil.broadcastMessage("&9[KOTH] &6" + koth.getName() + " &eKOTH has been ended.");
        } else {
            MessageUtil.broadcastMessage("&9[KOTH] &6" + koth.getName() + " &eKOTH was captured by &6" + capper.getDisplayName() + "&e.");

            PlayerData capperData = PlayerData.getInstance(capper);

            if (capperData == null) {
                capper.kickPlayer("Disconnected");
                return;
            }

            MessageUtil.broadcastMessage("&6+" + koth.getCoinReward() + " coins! (KOTH Captured)");
            capperData.addCoins(koth.getCoinReward());

            MessageUtil.broadcastMessage("&b+" + koth.getCoinReward() + " exp! (KOTH Captured)");
            capperData.addExperience(koth.getExpReward());
        }

        MessageUtil.broadcastMessage("");

        koth.setActive(false);
        koth.setCapper(null);
        koth.setContested(false);
        koth.setTimeLeft(9999);

        for (Player player : Bukkit.getOnlinePlayers()) {
            TABAPI.getPlayer(player.getUniqueId()).showScoreboard("KitPvP");

            if (LUNAR_API.isRunningLunarClient(player)) {
                LUNAR_API.removeWaypoint(player, new LCWaypoint("KOTH", koth.getLocX(), koth.getLocY(),
                        koth.getLocZ(), koth.getWorld().getName(), Color.NAVY.asRGB(), true, true));
            }
        }
    }

    public static void sendKOTHStatus(CommandSender sender) {
        KOTH activeKOTH = KOTH.getActiveKoth();

        if (activeKOTH == null) {
            MessageUtil.messagePlayer(sender, "&cThere are no active KOTHs.");

        } else {
            MessageUtil.messagePlayer(sender, "");
            MessageUtil.messagePlayer(sender, " &9&l" + activeKOTH.getName() + " KOTH");
            MessageUtil.messagePlayer(sender, " &eLocation: " + getLocation(activeKOTH));

            if (activeKOTH.isContested()) {
                MessageUtil.messagePlayer(sender, " &eCapper: &cContested");
            } else if (activeKOTH.getCapper() != null) {
                MessageUtil.messagePlayer(sender, " &eCapper: &a" + activeKOTH.getCapper().getDisplayName());
            } else {
                MessageUtil.messagePlayer(sender, " &eCapper: &cNot Contested");
            }

            MessageUtil.messagePlayer(sender, " &eTime Left: &c" + getTimeLeftNeat(activeKOTH));
            MessageUtil.messagePlayer(sender, "");
        }
    }

    public static boolean isInKOTH(Location loc) {
        if (KOTH.getActiveKoth() == null) {
            return false;
        }

        KOTH koth = KOTH.getActiveKoth();
        ProtectedRegion kothRegion = WorldGuardPlugin.inst().getRegionManager(Bukkit.getWorld(Settings.spawnWorld)).getRegion(koth.getRegionName());

        if (kothRegion == null) {
            return false;
        }

        BlockVector regionMin = kothRegion.getMinimumPoint();
        BlockVector regionMax = kothRegion.getMaximumPoint();

        if (regionMin.getX() <= loc.getX() && regionMax.getX() + 1 >= loc.getX()) {
            if (regionMin.getY() <= loc.getY() && regionMax.getY() >= loc.getY()) {
                return regionMin.getZ() <= loc.getZ() && regionMax.getZ() + 1 >= loc.getZ();
            }
        }

        return false;
    }

    public static String getLocation(KOTH activeKOTH) {
        return "&7(" + activeKOTH.getLocX() + ", " + activeKOTH.getLocY() + ", " + activeKOTH.getLocZ() + ")";
    }

    public static String getTimeLeftNeat(KOTH activeKOTH) {
        int seconds = activeKOTH.getTimeLeft() % 60;
        int hours = activeKOTH.getTimeLeft() / 60;
        int minutes = hours % 60;

        return ((minutes > 9) ? minutes : "0" + minutes) + ":" + ((seconds > 9) ? seconds : "0" + seconds);
    }
}
