package me.dthb.osp.potion;

import io.papermc.paper.potion.PotionMix;
import me.dthb.osp.OreSightPotion;
import me.dthb.osp.util.ItemBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.io.File;
import java.util.EnumSet;
import java.util.List;

public class Potion {

    private final EnumSet<Material> applicableMaterials;
    private final NamespacedKey potionKey;
    private final ChatColor glowColor;
    private final Component name;
    private final int duration;

    private final PotionType potionType;
    private final Material ingredient;
    private final Color color;

    public Potion(OreSightPotion plugin, File file) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        name = MiniMessage.miniMessage().deserialize(config.getString("name", "<gray>No name"));
        glowColor = ChatColor.valueOf(config.getString("glow-color", "GRAY").toUpperCase());
        color = Color.fromRGB(Integer.decode(config.getString("potion-color", "#000000")));
        ingredient = Material.matchMaterial(config.getString("ingredient", "BEDROCK"));
        potionType = PotionType.valueOf(config.getString("base-potion", "MUNDANE").toUpperCase());
        duration = config.getInt("duration", 50);
        List<Material> materialList = config.getStringList("materials").stream().map(Material::matchMaterial).toList();
        applicableMaterials = EnumSet.copyOf(materialList);

        String key = PlainTextComponentSerializer.plainText().serialize(name).replace(" ", "_");
        potionKey = new NamespacedKey(plugin, key);
    }

    public boolean isValid(Material material) {
        return applicableMaterials.contains(material);
    }

    public NamespacedKey potionKey() {
        return potionKey;
    }

    public Component name() {
        return name;
    }

    public ChatColor glowColor() {
        return glowColor;
    }

    public int duration() {
        return duration;
    }

    public void register() {

        ItemStack choice = ItemBuilder.start(Material.POTION)
                .meta(PotionMeta.class, m -> m.setBasePotionData(new PotionData(potionType)))
                .build();
        RecipeChoice.ExactChoice inputChoice = new RecipeChoice.ExactChoice(choice);
        RecipeChoice.MaterialChoice ingredientChoice = new RecipeChoice.MaterialChoice(ingredient);

        ItemStack result = ItemBuilder.start(Material.POTION)
                .name(name)
                .meta(PotionMeta.class, meta -> {
                    meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_ENCHANTS);
                    meta.addEnchant(Enchantment.CHANNELING, 0, true);
                    meta.setColor(color);
                    meta.getPersistentDataContainer().set(potionKey, PersistentDataType.INTEGER, 1);
                }).build();

        PotionMix diamondMix = new PotionMix(potionKey, result, inputChoice, ingredientChoice);
        Bukkit.getPotionBrewer().addPotionMix(diamondMix);
    }

}
