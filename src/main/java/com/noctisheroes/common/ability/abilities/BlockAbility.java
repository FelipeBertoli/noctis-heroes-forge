package com.noctisheroes.common.ability.abilities;

import com.noctisheroes.common.ability.helpers.TimedAbility;
import com.noctisheroes.entity.NoctisEntity;
import com.noctisheroes.entity.ai.flight.FlightState;
import com.noctisheroes.entity.interfaces.IFlightCapable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class BlockAbility extends TimedAbility<NoctisEntity> {

    private float damageAbsorbed;
    private int blockedAttacks;

    private static final int BLOCK_DURATION = 80;
    private static final float BLOCK_CHANCE = 0.02f;
    private static final double MAX_DISTANCE = 7.0;

    @Override
    public int getCooldown() {
        return 120;
    }

    @Override
    public int getPriority() {
        return 25;
    }

    @Override
    public boolean canUse(NoctisEntity entity) {

        if (entity instanceof IFlightCapable flightCapable) if (flightCapable.getFlightState() == FlightState.HUNT_FLIGHT) return false;

        LivingEntity target = entity.getTarget();

        if (target == null) return false;
        if (entity.distanceTo(target) > MAX_DISTANCE) return false;

        if (target.swingTime > 0) {
            return entity.getRandom().nextFloat() < BLOCK_CHANCE * 3;
        }

        return entity.getRandom().nextFloat() < BLOCK_CHANCE;
    }

    @Override
    protected void onStart(NoctisEntity entity) {
        damageAbsorbed = 0f;
        blockedAttacks = 0;

        entity.setBlocking(true);
        entity.getNavigation().stop();
        entity.triggerAnim("actions", "block");

        entity.level().players().forEach(player -> {

            player.displayClientMessage(
                    Component.literal("Começando bloqueio"),
                    false
            );
        });
    }

    @Override
    protected void onTick(NoctisEntity entity, int ticks) {
        entity.getNavigation().stop();
        entity.setDeltaMovement(Vec3.ZERO);
    }

    @Override
    protected void onStop(NoctisEntity entity) {

        entity.setBlocking(false);
        entity.stopTriggeredAnim("actions");
        entity.level().players().forEach(player -> {

            player.displayClientMessage(
                    Component.literal("Finalizando bloqueio"),
                    false
            );
        });
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