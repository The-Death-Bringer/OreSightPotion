package me.dthb.osp.team;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class TeamListener implements Listener {

    private final TeamManager teamManager;

    public TeamListener(TeamManager teamManager) {
        this.teamManager = teamManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        teamManager.sendAll(player, false);
        teamManager.sendAll(player, true);
    }

}
