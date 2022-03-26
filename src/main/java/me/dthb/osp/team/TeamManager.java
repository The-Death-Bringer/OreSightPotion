package me.dthb.osp.team;

import com.google.common.collect.Sets;
import net.minecraft.ChatFormatting;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Set;

public class TeamManager {

    private final Set<TeamWrapper> teams = Sets.newHashSet();

    public TeamManager() {
        for (ChatFormatting formatting : ChatFormatting.values())
            if (formatting.isColor())
                teams.add(new TeamWrapper(formatting));
    }

    public TeamWrapper wrapper(ChatColor color) {
        ChatFormatting formatting = ChatFormatting.values()[color.ordinal()];
        return teams.stream().filter(tw -> tw.formatting() == formatting).findFirst().orElse(null);
    }

    public void sendAll(Player player, boolean create) {
        teams.forEach(tw -> {
            if (create)
                tw.create(player);
            else
                tw.delete(player);
        });
    }

}
