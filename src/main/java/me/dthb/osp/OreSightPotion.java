package me.dthb.osp;

import me.dthb.osp.potion.PotionManager;
import me.dthb.osp.team.TeamListener;
import me.dthb.osp.team.TeamManager;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddMobPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.world.entity.monster.MagmaCube;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftMagmaCube;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class OreSightPotion extends JavaPlugin {

    private PotionManager potionManager;
    private TeamManager teamManager;

    public void onLoad() {
        this.potionManager = new PotionManager(this);
        this.potionManager.register();
    }

    public void onEnable() {
        this.teamManager = new TeamManager();

        getServer().getPluginManager().registerEvents(new TeamListener(teamManager), this);
        getServer().getPluginManager().registerEvents(new SightListener(this), this);
    }

    public PotionManager potionManager() {
        return potionManager;
    }

    public TeamManager teamManager() {
        return teamManager;
    }

    public MagmaCube spawnOutline(Player target, Location loc) {
        CraftWorld nmsWorld = (CraftWorld) loc.getWorld();
        MagmaCube cube = (MagmaCube) nmsWorld.createEntity(loc, CraftMagmaCube.class);

        cube.setSize(1, false);
        cube.setGlowingTag(true);
        cube.setSharedFlag(6, true);
        cube.setInvisible(true);
        cube.noPhysics = true;
        cube.moveTo(loc.getX() + 0.5, loc.getY() + 0.5, loc.getZ() + 0.5, loc.getYaw(), loc.getPitch());
        Packet<?> spawn = new ClientboundAddMobPacket(cube);
        Packet<?> metadata = new ClientboundSetEntityDataPacket(cube.getId(), cube.getEntityData(), true);
        CraftPlayer cp = (CraftPlayer) target;
        cp.getHandle().connection.send(spawn);
        cp.getHandle().connection.send(metadata);

        return cube;
    }

    public void killEntity(Player target, int id) {
        ((CraftPlayer) target).getHandle().connection.send(new ClientboundRemoveEntitiesPacket(id));
    }

}
