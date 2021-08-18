package net.foulest.kitpvp;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.SneakyThrows;
import net.foulest.kitpvp.cmds.*;
import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.kits.*;
import net.foulest.kitpvp.listeners.*;
import net.foulest.kitpvp.region.Spawn;
import net.foulest.kitpvp.util.ItemBuilder;
import net.foulest.kitpvp.util.PlaceholderUtil;
import net.foulest.kitpvp.util.Settings;
import net.foulest.kitpvp.util.SkullCreatorUtil;
import net.foulest.kitpvp.util.command.CommandFramework;
import net.foulest.kitpvp.util.kits.Kit;
import net.foulest.kitpvp.util.kits.KitManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

/**
 * @author Foulest
 * @project KitPvP
 */
@Getter
public class KitPvP extends JavaPlugin {

    // TODO: refund missed snowballs
    // TODO: block fisherman and imprisoner from hitting on koth
    // TODO: golems don't move
    // TODO: fix hulk ability
    // TODO: fix KOTH bugs

    private static KitPvP instance;
    private CommandFramework framework;
    private HikariDataSource hikari;

    public static KitPvP getInstance() {
        return instance;
    }

    public static void giveDefaultItems(Player player) {
        PlayerData playerData = PlayerData.getInstance(player);

        if (playerData == null) {
            player.kickPlayer("Disconnected");
            return;
        }

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);

        ItemStack kitSelector = new ItemBuilder(Material.NETHER_STAR).name("&aKit Selector &7(Right Click)").getItem();
        player.getInventory().setItem(0, kitSelector);

        ItemStack shopSelector = new ItemBuilder(Material.ENDER_CHEST).name("&aKit Shop &7(Right Click)").getItem();
        player.getInventory().setItem(1, shopSelector);

        ItemStack previousKit = new ItemBuilder(Material.WATCH).name("&aPrevious Kit &7(Right Click)").getItem();
        player.getInventory().setItem(2, previousKit);

        ItemStack yourStats = new ItemBuilder(SkullCreatorUtil.itemFromUuid(player.getUniqueId())).name("&aYour Stats &7(Right Click)").getItem();
        player.getInventory().setItem(4, yourStats);

        ItemStack healingItem;
        if (playerData.isUsingSoup()) {
            healingItem = new ItemBuilder(Material.MUSHROOM_SOUP).name("&aUsing Soup &7(Right Click)").getItem();
        } else {
            healingItem = new ItemBuilder(Material.POTION).hideInfo().durability(16421).name("&aUsing Potions &7(Right Click)").getItem();
        }
        player.getInventory().setItem(6, healingItem);

        ItemStack kitEnchanter = new ItemBuilder(Material.ENCHANTED_BOOK).name("&aKit Enchanter &7(Right Click)").getItem();
        player.getInventory().setItem(7, kitEnchanter);

        player.updateInventory();

//        if (player.hasPermission("KitPvP.staff")) {
//            ItemStack staffMode = new ItemBuilder(Material.EYE_OF_ENDER).name("&aStaff Mode &7(Right Click)").getItem();
//            player.getInventory().setItem(8, staffMode);
//        }
    }

    /**
     * Loads the plugin's kits.
     *
     * @param kits Kit to load.
     */
    private static void loadKits(Kit... kits) {
        for (Kit kit : kits) {
            KitManager.getInstance().registerKit(kit);
        }
    }

    @Override
    @SneakyThrows
    public void onEnable() {
        instance = this;
        framework = new CommandFramework(this);

        // Registers placeholders with PlaceholderAPI.
        Bukkit.getLogger().info("[KitPvP] - Loading Placeholders...");
        new PlaceholderUtil().register();

        // Creates the default settings config.
        Bukkit.getLogger().info("[KitPvP] - Loading Settings...");
        Settings.setupSettings();
        Settings.loadSettings();

        // Sets up the MySQL database.
        Bukkit.getLogger().info("[KitPvP] - Loading Hikari...");
        hikari = new HikariDataSource();
        hikari.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
        hikari.addDataSourceProperty("serverName", Settings.mysqlHost);
        hikari.addDataSourceProperty("port", Settings.mysqlPort);
        hikari.addDataSourceProperty("databaseName", Settings.mysqlDatabase);
        hikari.addDataSourceProperty("user", Settings.mysqlUser);
        hikari.addDataSourceProperty("password", Settings.mysqlPassword);
        hikari.addDataSourceProperty("characterEncoding", "utf8");
        hikari.addDataSourceProperty("useUnicode", "true");

        // Creates missing tables in the MySQL database.
        Bukkit.getLogger().info("[KitPvP] - Loading MySQL...");
        try (Connection connection = hikari.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS PlayerStats (uuid VARCHAR(36), coins INT, " +
                    "experience INT, kills INT, deaths INT, killstreak INT, topKillstreak INT, usingSoup BOOLEAN, " +
                    "previousKit VARCHAR(36))");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS PlayerKits (uuid VARCHAR(36), kitName VARCHAR(36))");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS Bounties (uuid VARCHAR(36), bounty INT, benefactor VARCHAR(36))");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS Enchants (uuid VARCHAR(36), featherFalling BOOLEAN," +
                    " thorns BOOLEAN, protection BOOLEAN, knockback BOOLEAN, sharpness BOOLEAN, punch BOOLEAN, power BOOLEAN)");
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // Loads the plugin's listeners.
        Bukkit.getLogger().info("[KitPvP] - Loading Listeners...");
        loadListeners(new DeathListener(), new EventListener(), new KitListener(), new StaffModeListener(), new KOTHListener());

        // Loads the plugin's commands.
        Bukkit.getLogger().info("[KitPvP] - Loading Commands...");
        loadCommands(new BalanceCmd(), new BountyCmd(), new ClearKitCmd(), new CombatLogCmd(), new EcoGiveCmd(),
                new EcoSetCmd(), new KitsCmd(), new PayCmd(), new SetSpawnCmd(), new SpawnCmd(), new StatsCmd(),
                new KitShopCmd(), new EcoTakeCmd(), new ArmorColorCmd(), new KitEnchanterCmd(), new SoupCmd(),
                new PotionsCmd(), new ReloadCmd(), new KOTHCmd());

        // Loads the plugin's kits.
        Bukkit.getLogger().info("[KitPvP] - Loading Kits...");
        loadKits(new Archer(), new Burrower(), new Cactus(), new Dragon(), new Fisherman(), new Ghost(), new Tamer(),
                new Hulk(), new Imprisoner(), new Kangaroo(), new Knight(), new Mage(), new Monk(), new Ninja(),
                new Pyro(), new Spiderman(), new Summoner(), new Tank(), new Thor(), new Timelord(), new Vampire(),
                new Zen());

        // Loads the spawn.
        Bukkit.getLogger().info("[KitPvP] - Loading Spawn...");
        Spawn.getInstance().load();

        // Loads online players' user data.
        Bukkit.getLogger().info("[KitPvP] - Loading Player Data...");
        for (Player player : Bukkit.getOnlinePlayers()) {
            Objects.requireNonNull(PlayerData.getInstance(player)).load();
            Spawn.getInstance().teleport(player);
            player.getInventory().setHeldItemSlot(0);
        }

        Bukkit.getLogger().info("[KitPvP] Loaded successfully.");
    }

    @Override
    public void onDisable() {
        // Unloads the kits saved in the Kit Manager.
        Bukkit.getLogger().info("[KitPvP] - Unloading Kits...");
        KitManager.getInstance().unloadKits();

        // Saves the spawn.
        Bukkit.getLogger().info("[KitPvP] - Saving Settings...");
        Settings.saveSettings();

        // Saves online players' data.
        Bukkit.getLogger().info("[KitPvP] - Saving Player Data...");
        for (Player player : Bukkit.getOnlinePlayers()) {
            Objects.requireNonNull(PlayerData.getInstance(player)).saveAll();

            if (CombatLog.isInCombat(player)) {
                CombatLog.remove(player);
            }
        }

        // Closes the MySQL connection.
        Bukkit.getLogger().info("[KitPvP] - Closing Hikari...");
        if (hikari != null) {
            hikari.close();
        }

        Bukkit.getLogger().info("[KitPvP] Shut down successfully.");
    }

    /**
     * Loads the plugin's listeners.
     *
     * @param listeners Listener to load.
     */
    private void loadListeners(Listener... listeners) {
        for (Listener listener : listeners) {
            Bukkit.getPluginManager().registerEvents(listener, this);
        }
    }

    /**
     * Loads the plugin's commands.
     *
     * @param commands Command to load.
     */
    private void loadCommands(Object... commands) {
        for (Object command : commands) {
            framework.registerCommands(command);
        }
    }
}
