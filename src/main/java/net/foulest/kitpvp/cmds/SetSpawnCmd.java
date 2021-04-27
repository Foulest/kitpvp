package net.foulest.kitpvp.cmds;

import net.foulest.kitpvp.util.MessageUtil;
import net.foulest.kitpvp.region.Spawn;
import net.foulest.kitpvp.util.command.Command;
import net.foulest.kitpvp.util.command.CommandArgs;
import org.bukkit.entity.Player;

/**
 * @author Foulest
 * @project KitPvP
 */
@SuppressWarnings("MethodMayBeStatic")
public class SetSpawnCmd {

    private static final Spawn SPAWN = Spawn.getInstance();

    @Command(name = "setspawn", usage = "/setspawn", description = "Sets the spawn point.",
            permission = "kitpvp.setspawn", inGameOnly = true)
    public void onCommand(CommandArgs args) {
        Player player = args.getPlayer();

        SPAWN.setLocation(player.getLocation());
        MessageUtil.messagePlayer(player, "&aSpawn has been set.");
    }
}
