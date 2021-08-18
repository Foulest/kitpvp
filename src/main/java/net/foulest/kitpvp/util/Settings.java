package net.foulest.kitpvp.util;

import lombok.Getter;
import net.foulest.kitpvp.koth.KOTH;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

@Getter
public class Settings {

    public static String spawnWorld;
    public static double spawnX;
    public static double spawnY;
    public static double spawnZ;
    public static double spawnYaw;
    public static double spawnPitch;
    public static int killCoinBonus;
    public static int killExpBonus;
    public static int killStreakBonus;
    public static String mysqlHost;
    public static String mysqlUser;
    public static String mysqlPassword;
    public static String mysqlDatabase;
    public static int mysqlPort;
    public static File file;
    public static FileConfiguration config;

    public static void setupSettings() {
        file = new File(Bukkit.getServer().getPluginManager().getPlugin("KitPvP").getDataFolder(), "settings.yml");

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ignored) {
                MessageUtil.log("&c[KitPvP] Couldn't create the config file.");
                return;
            }
        }

        config = YamlConfiguration.loadConfiguration(file);

        config.addDefault("spawn.world", "world");
        config.addDefault("spawn.x", 0.5);
        config.addDefault("spawn.y", 64.0);
        config.addDefault("spawn.z", 0.5);
        config.addDefault("spawn.yaw", 90.0);
        config.addDefault("spawn.pitch", 0.0);
        config.addDefault("kill.coin-bonus", 10);
        config.addDefault("kill.exp-bonus", 15);
        config.addDefault("kill.streak-bonus", 5);
        config.addDefault("mysql.host", "host");
        config.addDefault("mysql.user", "user");
        config.addDefault("mysql.password", "password");
        config.addDefault("mysql.database", "database");
        config.addDefault("mysql.port", 3306);
        config.addDefault("koth", Collections.<String> emptyList());
        config.options().copyDefaults(true);

        try {
            config.save(file);
        } catch (IOException exception) {
            MessageUtil.log("&c[KitPvP] Couldn't save the config file.");
        }
    }

    public static void loadSettings() {
        config = YamlConfiguration.loadConfiguration(file);

        spawnWorld = config.getString("spawn.world");
        spawnX = config.getDouble("spawn.x");
        spawnY = config.getDouble("spawn.y");
        spawnZ = config.getDouble("spawn.z");
        spawnYaw = config.getDouble("spawn.yaw");
        spawnPitch = config.getDouble("spawn.pitch");

        killCoinBonus = config.getInt("kill.coin-bonus");
        killExpBonus = config.getInt("kill.exp-bonus");
        killStreakBonus = config.getInt("kill.streak-bonus");

        mysqlHost = config.getString("mysql.host");
        mysqlUser = config.getString("mysql.user");
        mysqlPassword = config.getString("mysql.password");
        mysqlDatabase = config.getString("mysql.database");
        mysqlPort = config.getInt("mysql.port");

        KOTH.loadKoths();
    }

    public static void saveSettings() {
        config.set("spawn.world", spawnWorld);
        config.set("spawn.x", spawnX);
        config.set("spawn.y", spawnY);
        config.set("spawn.z", spawnZ);
        config.set("spawn.yaw", spawnYaw);
        config.set("spawn.pitch", spawnPitch);

        config.set("kill.coin-bonus", killCoinBonus);
        config.set("kill.exp-bonus", killExpBonus);
        config.set("kill.streak-bonus", killStreakBonus);

        config.set("mysql.host", mysqlHost);
        config.set("mysql.user", mysqlUser);
        config.set("mysql.password", mysqlPassword);
        config.set("mysql.database", mysqlDatabase);
        config.set("mysql.port", mysqlPort);

        try {
            config.save(file);
        } catch (IOException exception) {
            MessageUtil.log("&c[KitPvP] Couldn't save the config file.");
        }
    }
}
