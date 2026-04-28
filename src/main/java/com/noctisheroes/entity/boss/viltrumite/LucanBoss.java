package com.noctisheroes.entity.boss.viltrumite;

import com.noctisheroes.entity.base.AbstractViltrumite;
import com.noctisheroes.entity.base.components.BossComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

public class LucanBoss extends AbstractViltrumite {
    private static final int   SKIN_COUNT    = 1;
    private final BossComponent boss = new BossComponent(this);
    @Override protected int    getSkinCount()    { return SKIN_COUNT; }

    public LucanBoss(EntityType<? extends Monster> type, Level level) {
        super(type, level, "lucan_boss");
        this.xpReward = 60;
    }

    public static AttributeSupplier setAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 300.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.4D)
                .add(Attributes.FLYING_SPEED, 0.8D) // importante pra mobs voadores
                .add(Attributes.ATTACK_DAMAGE, 20.0D)
                .add(Attributes.ATTACK_KNOCKBACK, 1.0D)
                .add(Attributes.ATTACK_SPEED, 0.5D)
                .add(Attributes.FOLLOW_RANGE, 50.0D)
                .add(Attributes.ARMOR, 0.9D)
                .add(Attributes.ARMOR_TOUGHNESS, 0.36)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.7D).build();
    }

    @Override
    public void tick() {
        super.tick();
        boss.tick();
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        boss.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        boss.removePlayer(player);
    }

}
