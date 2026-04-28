package com.noctisheroes.entity.base.components;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Mob;

public class BossComponent {

    private final Mob mob;
    private final ServerBossEvent bossEvent;

    public BossComponent(Mob mob) {
        this.mob = mob;

        this.bossEvent = new ServerBossEvent(
                mob.getDisplayName(),
                BossEvent.BossBarColor.RED,
                BossEvent.BossBarOverlay.PROGRESS
        );

        this.bossEvent.setDarkenScreen(false);
        this.bossEvent.setCreateWorldFog(false);
    }

    public void tick() {
        if (!mob.level().isClientSide) {
            bossEvent.setProgress(mob.getHealth() / mob.getMaxHealth());
        }
    }

    public void addPlayer(ServerPlayer player) {
        bossEvent.addPlayer(player);
    }

    public void removePlayer(ServerPlayer player) {
        bossEvent.removePlayer(player);
    }
}