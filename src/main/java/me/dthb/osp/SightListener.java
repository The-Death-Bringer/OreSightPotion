package me.dthb.osp;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.dthb.osp.potion.Potion;
import me.dthb.osp.potion.PotionManager;
import me.dthb.osp.potion.PotionTask;
import me.dthb.osp.team.TeamManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class SightListener implements Listener {

    private final Map<UUID, Map<String, Integer>> views = Maps.newHashMap();
    private final Map<UUID, BukkitRunnable> tasks = Maps.newHashMap();
    private final Map<UUID, Vector> lastPos = Maps.newHashMap();
    private final Map<UUID, Potion> viewing = Maps.newHashMap();
    private final PotionManager potionManager;
    private final TeamManager teamManager;
    private final OreSightPotion plugin;

    public SightListener(OreSightPotion plugin) {
        this.plugin = plugin;
        this.teamManager = plugin.teamManager();
        this.potionManager = plugin.potionManager();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {

        if (event.getItem().getType() != Material.POTION)
            return;

        NamespacedKey potionKey = findPotionKey(event.getItem());

        if (potionKey == null)
            return;

        Potion potion = potionManager.findByKey(potionKey);
        if (potion == null)
            return;

        UUID uuid = event.getPlayer().getUniqueId();
        viewing.put(uuid, potion);

        if (tasks.containsKey(uuid)) {
            tasks.get(uuid).cancel();
            tasks.remove(uuid);
        }

        PotionTask task = new PotionTask(event.getPlayer(), this, potion);
        task.runTaskTimerAsynchronously(plugin, 0L, 10L);
        tasks.put(uuid, task);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (viewing.containsKey(player.getUniqueId()))
            handle(player);
    }

    public void stopViewing(Player player) {
        UUID uuid = player.getUniqueId();
        viewing.remove(uuid);
        if (views.containsKey(uuid)) {
            views.get(uuid).values().forEach(id -> plugin.killEntity(player, id));
            views.remove(uuid);
        }

        if (tasks.containsKey(uuid)) {
            tasks.get(uuid).cancel();
            tasks.remove(uuid);
        }

        lastPos.remove(uuid);
    }

    private NamespacedKey findPotionKey(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        for (NamespacedKey key : meta.getPersistentDataContainer().getKeys())
            if (key.getNamespace().equals(plugin.getName().toLowerCase(Locale.ROOT)))
                return key;
        return null;
    }

    private void handle(Player player) {
        UUID uuid = player.getUniqueId();
        Vector pos = player.getEyeLocation().toVector();

        // Same place as before?
        if (pos.equals(lastPos.get(uuid)))
            return;

        Potion potion = viewing.get(uuid);

        // Despawn old outlines
        if (views.containsKey(uuid)) {
            teamManager.wrapper(potion.glowColor()).remove(player, views.get(uuid).keySet());
            views.get(uuid).values().forEach(id -> plugin.killEntity(player, id));
            views.remove(uuid);
        }

        // Update the player pos
        lastPos.put(uuid, pos);

        // Spawn new outlines
        Map<String, Integer> mobInfo = Maps.newHashMap();
        findValidBlocks(player).stream().map(block -> plugin.spawnOutline(player, block.getLocation()))
                .forEach(mc -> mobInfo.put(mc.getStringUUID(), mc.getId()));
        teamManager.wrapper(potion.glowColor()).add(player, mobInfo.keySet());
        views.put(uuid, mobInfo);
    }

    private List<Block> findValidBlocks(Player player) {
        List<Block> blockList = Lists.newArrayList();
        Location center = player.getEyeLocation();
        Potion potion = viewing.get(player.getUniqueId());

        int radius = 10;
        for (int x = -radius; x < radius; x++) {
            for (int y = -radius; y < radius; y++) {
                for (int z = -radius; z < radius; z++) {
                    Vector offset = new Vector(x, y, z);
                    center.add(offset);
                    Block block = center.getBlock();
                    if (potion.isValid(block.getType()))
                        blockList.add(block);
                    center.subtract(offset);
                }
            }
        }

        return blockList;
    }

}
