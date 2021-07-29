package net.foulest.kitpvp.util.kits;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Foulest
 * @project KitPvP
 */
@Getter
public class KitManager {

    private static final KitManager INSTANCE = new KitManager();
    private final List<Kit> kits = new ArrayList<>();

    public static KitManager getInstance() {
        return INSTANCE;
    }

    public void registerKit(Kit kit) {
        kits.add(kit);
    }

    public void unloadKits() {
        kits.clear();
    }

    public Kit valueOf(String name) {
        for (Kit kit : kits) {
            if (kit.getName().equalsIgnoreCase(name)) {
                return kit;
            }
        }

        return null;
    }
}
