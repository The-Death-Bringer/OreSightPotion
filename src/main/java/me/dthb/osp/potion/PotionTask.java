package me.dthb.osp.potion;

import me.dthb.osp.SightListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PotionTask extends BukkitRunnable {

    private final SightListener listener;
    private final Component name;
    private final Player player;
    private final int duration;

    private boolean shouldCount;
    private int count = 0;

    public PotionTask(Player player, SightListener listener, Potion potion) {
        this.player = player;
        this.listener = listener;
        this.name = potion.name();
        this.duration = potion.duration();
    }

    @Override
    public void run() {
        if (count >= duration) {
            listener.stopViewing(player);
            return;
        }

        Component time = MiniMessage.miniMessage().deserialize("<gray>: <gold>" + (duration - count));
        player.sendActionBar(Component.text().append(name).append(time).build());

        if (shouldCount) {
            count++;
        }

        shouldCount = !shouldCount;
    }

}
