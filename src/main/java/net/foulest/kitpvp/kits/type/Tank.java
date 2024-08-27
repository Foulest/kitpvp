/*
 * KitPvP - a fully-featured core plugin for the KitPvP gamemode.
 * Copyright (C) 2024 Foulest (https://github.com/Foulest)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package net.foulest.kitpvp.kits.type;

import net.foulest.kitpvp.kits.Kit;
import net.foulest.kitpvp.util.Settings;
import net.foulest.kitpvp.util.item.ItemBuilder;
import net.foulest.kitpvp.util.item.SkullBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/**
 * Represents the Tank kit.
 *
 * @author Foulest
 */
public class Tank implements Kit {

    @Override
    public String getName() {
        return "Tank";
    }

    @Override
    public ItemStack getDisplayItem() {
        return new ItemStack(Objects.requireNonNull(Material.DIAMOND_CHESTPLATE));
    }

    @Override
    public PotionEffect[] getPotionEffects() {
        return new PotionEffect[]{
                new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 1, false, false)
        };
    }

    @Override
    public List<ItemBuilder> getItems() {
        ItemBuilder weapon = new ItemBuilder(Material.STONE_AXE).unbreakable(true).hideInfo();
        return Collections.singletonList(weapon);
    }

    @Override
    public ItemBuilder[] getArmor() {
        String base64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYT"
                + "Y1OWIyYmIwNzBjMTIwOGJhNTE0NTIzNjFmZDMwYTY2NzIxMzI5NWYyMWRiNDM3ZGY1NzI4MWQ1ODJjODlhZCJ9fX0=";

        return new ItemBuilder[]{
                new ItemBuilder(SkullBuilder.itemFromBase64(base64)).name("&fTank's Head"),
                new ItemBuilder(Material.DIAMOND_CHESTPLATE).unbreakable(true).hideInfo(),
                new ItemBuilder(Material.DIAMOND_LEGGINGS).unbreakable(true).hideInfo(),
                new ItemBuilder(Material.DIAMOND_BOOTS).unbreakable(true).hideInfo()
        };
    }

    @Override
    public List<String> getLore() {
        return new ArrayList<>(Arrays.asList("&7Style: &aDefensive", "", "&7Slow but very resistant."));
    }

    @Override
    public boolean enabled() {
        return Settings.tankKitEnabled;
    }

    @Override
    public int getCost() {
        return Settings.tankKitCost;
    }

    @Override
    public Permission permission() {
        return new Permission("kitpvp.kit.tank", PermissionDefault.TRUE);
    }
}
