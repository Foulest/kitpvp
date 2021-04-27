package net.foulest.kitpvp.util.kits;

import net.foulest.kitpvp.data.PlayerData;
import net.foulest.kitpvp.util.ItemBuilder;
import net.foulest.kitpvp.util.MessageUtil;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.List;

/**
 * @author Foulest
 * @project KitPvP
 */
public interface Kit {

    /**
     * The name of the kit.
     */
    String getName();

    /**
     * The display item of the kit.
     */
    ItemStack getDisplayItem();

    /**
     * The items of the kit.
     */
    List<ItemStack> getItems();

    /**
     * The armor of the kit.
     */
    ItemStack[] getArmor();

    /**
     * The potion effects of the kit.
     */
    PotionEffect[] getPotionEffects();

    /**
     * The lore of the kit.
     */
    List<String> getLore();

    /**
     * The cost of the kit in coins.
     */
    int getCost();

    /**
     * Applies a kit to a player.
     *
     * @param player The player to apply the kit to.
     */
    default void apply(Player player) {
        PlayerData playerData = PlayerData.getInstance(player);

        // Checks if the player owns the kit they're trying to equip.
        if (!playerData.getOwnedKits().contains(this)) {
            MessageUtil.messagePlayer(player, "&cYou do not own the " + getName() + " kit.");
            return;
        }

        // Sets the player's kit data.
        playerData.setKit(this);

        // Clears the player's inventory and armor.
        player.getInventory().clear();
        player.getInventory().setHeldItemSlot(0);
        player.getInventory().setArmorContents(null);

        // Clears the player's potion effects.
        for (PotionEffect effects : player.getActivePotionEffects()) {
            player.removePotionEffect(effects.getType());
        }

        // Sets the player's potion effects.
        if (getPotionEffects() != null) {
            for (PotionEffect effect : getPotionEffects()) {
                if (effect == null) {
                    break;
                }

                player.addPotionEffect(effect);
            }
        }

        // Sets the player's healing item.
        for (int i = 0; i < player.getInventory().getSize(); ++i) {
            if (playerData.isUsingSoup()) {
                player.getInventory().addItem(new ItemBuilder(Material.MUSHROOM_SOUP).name("&fMushroom Stew").getItem());
            } else {
                player.getInventory().addItem(new ItemBuilder(Material.POTION).durability(16421).name("&fSplash Potion of Healing").getItem());
            }
        }

        // Sets the player's kit items.
        for (int i = 0; i < getItems().size(); ++i) {
            ItemStack item = getItems().get(i);

            if (item.getType().toString().toLowerCase().contains("sword")
                    || item.getType().toString().toLowerCase().contains("cactus")
                    || item.getType().toString().toLowerCase().contains("axe")) {
                if (playerData.isKnockbackEnchant()) {
                    item = new ItemBuilder(item).enchant(Enchantment.KNOCKBACK, 2).getItem();
                }

                if (playerData.isSharpnessEnchant()) {
                    item = new ItemBuilder(item).enchant(Enchantment.DAMAGE_ALL, 2).getItem();
                }
            }

            if (item.getType().toString().toLowerCase().contains("bow")) {
                if (playerData.isPunchEnchant()) {
                    item = new ItemBuilder(item).enchant(Enchantment.ARROW_KNOCKBACK, 2).getItem();
                }

                if (playerData.isPowerEnchant()) {
                    item = new ItemBuilder(item).enchant(Enchantment.ARROW_DAMAGE, 2).getItem();
                }
            }

            player.getInventory().setItem(i, item);
        }

        // Sets the player's armor.
        ItemStack helmet = getArmor()[0];
        ItemStack chestplate = getArmor()[1];
        ItemStack leggings = getArmor()[2];
        ItemStack boots = getArmor()[3];

        if (playerData.isThornsEnchant()) {
            if (helmet != null && helmet.getType() != Material.AIR && helmet.getType() != Material.SKULL_ITEM) {
                helmet = new ItemBuilder(helmet).enchant(Enchantment.THORNS, 2).getItem();
            }

            if (chestplate != null && chestplate.getType() != Material.AIR) {
                chestplate = new ItemBuilder(chestplate).enchant(Enchantment.THORNS, 2).getItem();
            }

            if (leggings != null && leggings.getType() != Material.AIR) {
                leggings = new ItemBuilder(leggings).enchant(Enchantment.THORNS, 2).getItem();
            }

            if (boots != null && boots.getType() != Material.AIR) {
                boots = new ItemBuilder(boots).enchant(Enchantment.THORNS, 2).getItem();
            }
        }

        if (playerData.isProtectionEnchant()) {
            if (helmet != null && helmet.getType() != Material.AIR && helmet.getType() != Material.SKULL_ITEM) {
                helmet = new ItemBuilder(helmet).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2).getItem();
            }

            if (chestplate != null && chestplate.getType() != Material.AIR) {
                chestplate = new ItemBuilder(chestplate).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2).getItem();
            }

            if (leggings != null && leggings.getType() != Material.AIR) {
                leggings = new ItemBuilder(leggings).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2).getItem();
            }

            if (boots != null && boots.getType() != Material.AIR) {
                boots = new ItemBuilder(boots).enchant(Enchantment.PROTECTION_ENVIRONMENTAL, 2).getItem();
            }
        }

        if (playerData.isFeatherFallingEnchant() && boots != null && boots.getType() != Material.AIR) {
            boots = new ItemBuilder(boots).enchant(Enchantment.PROTECTION_FALL, 4).getItem();
        }

        player.getInventory().setHelmet(helmet);
        player.getInventory().setChestplate(chestplate);
        player.getInventory().setLeggings(leggings);
        player.getInventory().setBoots(boots);
    }
}
