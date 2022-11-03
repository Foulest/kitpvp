package net.foulest.kitpvp.util;

import lombok.Getter;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author nonamesldev, Foulest
 * @project KitPvP
 * <p>
 * https://www.spigotmc.org/threads/util-itembuilder-manage-items-easily.48397/
 */
@Getter
public class ItemBuilder {

    private final ItemStack item;
    private int slot;

    public ItemBuilder(Material mat) {
        item = new ItemStack(mat);
    }

    public ItemBuilder(ItemStack stack) {
        item = new ItemStack(stack);
    }

    public ItemBuilder addTag(String name) {
        return this;
    }

    public ItemBuilder name(String name) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(MessageUtil.colorize(name));
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder slot(int slot) {
        this.slot = slot;
        return this;
    }

    public ItemBuilder unbreakable(boolean status) {
        ItemMeta meta = item.getItemMeta();

        if (item.getType() == Material.AIR) {
            return this;
        }

        meta.spigot().setUnbreakable(status);
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder addGlow() {
        enchant(Enchantment.WATER_WORKER, 1);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder hideInfo() {
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder lore(String lore) {
        List<String> itemLore = item.getItemMeta().hasLore() ? item.getItemMeta().getLore() : new ArrayList<>();
        itemLore.add(MessageUtil.colorize(lore));
        lore(itemLore);
        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        ItemMeta meta = item.getItemMeta();
        List<String> loreList = new ArrayList<>();

        for (String str : lore) {
            loreList.add(MessageUtil.colorize(str));
        }

        meta.setLore(loreList);
        item.setItemMeta(meta);
        return this;
    }

    public ItemBuilder clearLore() {
        lore(Collections.emptyList());
        return this;
    }

    public ItemBuilder clearEnchantments() {
        if (item.getItemMeta().hasEnchants()) {
            for (Enchantment enchantments : item.getEnchantments().keySet()) {
                item.removeEnchantment(enchantments);
            }
        }

        return this;
    }

    public ItemBuilder clearEnchantment(Enchantment enchant) {
        ItemMeta meta;

        if (item.getItemMeta().hasEnchants() && item.getEnchantments().containsKey(enchant)) {
            meta = item.getItemMeta();
            meta.removeEnchant(enchant);
            item.setItemMeta(meta);
        }

        return this;
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        if (item.containsEnchantment(enchantment)) {
            item.addUnsafeEnchantment(enchantment, level + item.getEnchantmentLevel(enchantment));
        } else {
            item.addUnsafeEnchantment(enchantment, level);
        }
        return this;
    }

    public ItemBuilder amount(int amount) {
        item.setAmount(amount > 0 ? amount : 1);
        return this;
    }

    public ItemBuilder durability(int durability) {
        item.setDurability((short) durability);
        return this;
    }

    public ItemBuilder color(Color color) {
        LeatherArmorMeta meta;

        if (item.getType().toString().contains("LEATHER_")) {
            meta = (LeatherArmorMeta) item.getItemMeta();
            meta.setColor(color);
            item.setItemMeta(meta);
        }

        return this;
    }
}
