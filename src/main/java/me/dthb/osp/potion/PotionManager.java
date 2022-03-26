package me.dthb.osp.potion;

import com.google.common.collect.Sets;
import me.dthb.osp.OreSightPotion;
import org.bukkit.NamespacedKey;

import java.io.File;
import java.util.Set;

public class PotionManager {

    private final Set<Potion> potions = Sets.newHashSet();

    public PotionManager(OreSightPotion plugin) {
        File potionFolder = new File(plugin.getDataFolder(), "potions");
        if (!potionFolder.exists())
            potionFolder.mkdirs();

        for (File potionFile : potionFolder.listFiles()) {
            potions.add(new Potion(plugin, potionFile));
        }
        plugin.getLogger().info("Loaded " + potions.size() + " potions");
    }

    public void register() {
        potions.forEach(Potion::register);
    }

    public Potion findByKey(NamespacedKey potionKey) {
        return potions.stream().filter(p -> p.potionKey().equals(potionKey)).findFirst().orElse(null);
    }

}
