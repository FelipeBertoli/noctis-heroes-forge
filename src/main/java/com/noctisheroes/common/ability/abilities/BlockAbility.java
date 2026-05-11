package com.noctisheroes.common.ability.abilities;

import com.noctisheroes.common.ability.helpers.TimedAbility;
import com.noctisheroes.entity.NoctisEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class BlockAbility extends TimedAbility<NoctisEntity> {

    private float damageAbsorbed;
    private int blockedAttacks;

    private static final int BLOCK_DURATION = 100;
    private static final float BLOCK_CHANCE = 0.1f;
    private static final double MAX_DISTANCE = 15.0;

    @Override
    public int getCooldown() {
        return 60;
    }

    @Override
    public int getPriority() {
        return 25;
    }

    @Override
    public boolean canUse(NoctisEntity entity) {
        LivingEntity target = entity.getTarget();

        if (target == null) return false;
        if (entity.distanceTo(target) > MAX_DISTANCE) return false;

        return entity.getRandom().nextFloat() < BLOCK_CHANCE;
    }

    @Override
    protected void onStart(NoctisEntity entity) {
        damageAbsorbed = 0f;
        blockedAttacks = 0;

        entity.getNavigation().stop();
        entity.triggerAnim("actions", "block");
        entity.setBlocking(true);
    }

    @Override
    protected void onTick(NoctisEntity entity, int ticks) {
        entity.getNavigation().stop();
        entity.setDeltaMovement(Vec3.ZERO);
        entity.hasImpulse = true;
    }

    @Override
    protected void onStop(NoctisEntity entity) {
        entity.setBlocking(false);
    }

    @Override
    public String getId() {
        return "block";
    }

    @Override
    protected int getDuration() {
        return BLOCK_DURATION;
    }

    @Override
    public boolean overridesAttackAnimation() {
        return true;
    }

    @Override
    public boolean locksAbilities() {
        return true;
    }

    @Override
    public boolean preventsAttacking() {
        return true;
    }

    @Override
    public boolean preventsMovement() {
        return true;
    }

    @Override
    public boolean grantsInvulnerability() {
        return true;
    }

    public float getDamageAbsorbed() {
        return damageAbsorbed;
    }

    public int getBlockedAttacks() {
        return blockedAttacks;
    }

    public void registerBlockedDamage(float damage) {
        blockedAttacks++;
        damageAbsorbed += damage;
    }
}