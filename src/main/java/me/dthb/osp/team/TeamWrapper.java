package me.dthb.osp.team;

import net.minecraft.ChatFormatting;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class TeamWrapper {

    private final ChatFormatting formatting;
    private final PlayerTeam team;

    public TeamWrapper(ChatFormatting formatting) {
        this.formatting = formatting;
        this.team = new PlayerTeam(new Scoreboard(), genCode());
        team.setCollisionRule(Team.CollisionRule.NEVER);
        team.setColor(formatting);
    }

    public ChatFormatting formatting() {
        return formatting;
    }

    public void create(Player player) {
        Packet<?> packet = ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true);
        ((CraftPlayer) player).getHandle().connection.send(packet);
    }

    public void delete(Player player) {
        Packet<?> packet = ClientboundSetPlayerTeamPacket.createRemovePacket(team);
        ((CraftPlayer) player).getHandle().connection.send(packet);
    }

    public void add(Player player, Collection<String> entries) {
        Packet<?> packet = ClientboundSetPlayerTeamPacket.createMultiplePlayerPacket(team, entries, ClientboundSetPlayerTeamPacket.Action.ADD);
        ((CraftPlayer) player).getHandle().connection.send(packet);
    }

    public void remove(Player player, Collection<String> entries) {
        Packet<?> packet = ClientboundSetPlayerTeamPacket.createMultiplePlayerPacket(team, entries, ClientboundSetPlayerTeamPacket.Action.REMOVE);
        ((CraftPlayer) player).getHandle().connection.send(packet);
    }

    private String genCode() {
        UUID uuid = UUID.randomUUID();
        String string = uuid.toString().replace("-", "");
        int index = ThreadLocalRandom.current().nextInt(0, string.length() - 16);
        return string.substring(index, index + 16);
    }

}
