package com.noctisheroes.entity;

import com.noctisheroes.entity.base.AbstractViltrumite;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ViltrumiteEntity extends AbstractViltrumite {

    private static final int   SKIN_COUNT    = 3;
    private static final double SPEED_PASSIVE = 0.20D;
    private static final double SPEED_ALERT   = 1.50D;
    private static final double SPEED_ATTACK  = 0.80D;
    private static final double DIST_DETECT   = 30.0D;
    private static final double DIST_ATTACK   = 3.0D;

    public ViltrumiteEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    // ── Configuração ─────────────────────────────────────────────────────────

    @Override protected int    getSkinCount()    { return SKIN_COUNT; }
    @Override protected double getSpeedPassive() { return SPEED_PASSIVE; }
    @Override protected double getSpeedAlert()   { return SPEED_ALERT; }
    @Override protected double getSpeedAttack()  { return SPEED_ATTACK; }
    @Override protected double getDetectRange()  { return DIST_DETECT; }
    @Override protected double getAttackRange()  { return DIST_ATTACK; }

    // ── Goals ─────────────────────────────────────────────────────────────────

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
        goalSelector.addGoal(2, new WaterAvoidingRandomFlyingGoal(this, 1.0D));
        goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(2, new RandomLookAroundGoal(this));

        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // ── Atributos ─────────────────────────────────────────────────────────────

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100.0D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.MOVEMENT_SPEED, SPEED_PASSIVE)
                .add(Attributes.FLYING_SPEED, SPEED_PASSIVE)
                .add(Attributes.FOLLOW_RANGE, 35.0D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5D);
    }
}