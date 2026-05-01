package com.noctisheroes.entity.entities.viltrumite.base;

import com.noctisheroes.common.ability.helpers.AbilityParticleEffects;
import com.noctisheroes.common.ability.abilities.DestructiveDashAbility;
import com.noctisheroes.common.ability.abilities.SuperPunchAbility;
import com.noctisheroes.entity.NoctisEntity;
import com.noctisheroes.entity.ai.goals.FlyingChaseGoal;
import com.noctisheroes.entity.ai.states.FlightState;
import net.minecraft.network.syncher.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;

/**
 * Viltrumita base
 *
*/
public abstract class AbstractViltrumite extends NoctisEntity {

    // =============================
    // 🎬 ANIMAÇÕES
    // =============================

    protected static final RawAnimation LEVITATE_IDLE =
            RawAnimation.begin().thenLoop("animation.viltrumite.levitate_idle");

    protected static final RawAnimation LEVITATE_FLIGHT =
            RawAnimation.begin().thenLoop("animation.viltrumite.levitate_flight");

    protected static final RawAnimation FLIGHT_START =
            RawAnimation.begin().thenPlay("animation.viltrumite.flight_start");

    protected static final RawAnimation HUNT_FLIGHT =
            RawAnimation.begin().thenLoop("animation.viltrumite.hunt_flight");

    protected static final RawAnimation FLIGHT_STOP =
            RawAnimation.begin().thenPlay("animation.viltrumite.flight_stop");

    // =============================
    // 🔄 STATE MACHINE
    // =============================

    private static final EntityDataAccessor<Integer> FLIGHT_STATE =
            SynchedEntityData.defineId(AbstractViltrumite.class, EntityDataSerializers.INT);

    private int stateTimer = 0;

    // =============================
    // 🧠 CACHE / CONTROLE
    // =============================

    private FlyingPathNavigation cachedFlyingNavigation;
    private int ticksSinceFlightChange = 0;
    private static final int MIN_TICKS_BETWEEN_FLIGHT_CHANGE = 10;

    protected static final float RANDOM_FLIGHT_TOGGLE_CHANCE = 0.002f;
    protected static final float COMBAT_FLIGHT_CHANCE = 0.01f;

    // =============================
    // ✨ EFEITOS DE PARTÍCULAS
    // =============================

    private int sonicBoomTicks = 0;
    private static final int SONIC_BOOM_INTERVAL = 2; // A cada 2 ticks em HUNT_FLIGHT

    // =============================
    // 🛡️ PROTEÇÕES
    // =============================

    private static final float FIRE_RESISTANCE = 0.5f;      // 50% de resistência a fogo
    private static final float PROJECTILE_RESISTANCE = 0.6f; // 60% de resistência a projéteis

    // =============================
    // 🏗️ CONSTRUTOR
    // =============================

    protected AbstractViltrumite(EntityType<? extends Monster> type, Level level, String tag) {
        super(type, level, tag);
        this.moveControl = new FlyingMoveControl(this, 10, true);
        this.getAbilityManager().register(new DestructiveDashAbility());
        this.getAbilityManager().register(new SuperPunchAbility());
    }

    // =============================
    // 🔄 SYNC DATA
    // =============================

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(FLIGHT_STATE, FlightState.GROUNDED.ordinal());
    }

    public FlightState getFlightState() {
        return FlightState.values()[this.entityData.get(FLIGHT_STATE)];
    }

    public void setFlightState(FlightState state) {

        if (this.getFlightState() == state) return;

        if (this.ticksSinceFlightChange < MIN_TICKS_BETWEEN_FLIGHT_CHANGE) return;

        this.entityData.set(FLIGHT_STATE, state.ordinal());
        this.setNoGravity(state != FlightState.GROUNDED);

        this.stateTimer = 0;
        this.ticksSinceFlightChange = 0;
    }

    protected void forceSetFlightState(FlightState state) {
        this.entityData.set(FLIGHT_STATE, state.ordinal());
        this.setNoGravity(state != FlightState.GROUNDED);
        this.stateTimer = 0;
        this.ticksSinceFlightChange = 0;
    }



    // =============================
    // 🎯 GOALS
    // =============================

    @Override
    protected void registerMovementGoals() {
        goalSelector.addGoal(3, new FlyingChaseGoal(this));
        goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1.0D));
    }

    @Override
    protected void registerLookingGoals() {
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 12.0F));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    // =============================
    // 🎬 ANIMAÇÃO
    // =============================

    @Override
    protected <E extends NoctisEntity> PlayState movementController(AnimationState<E> event) {

        var controller = event.getController();
        FlightState state = getFlightState();

        switch (state) {

            case LEVITATE -> {
                if (event.isMoving()) {
                    controller.setAnimation(LEVITATE_FLIGHT);
                } else {
                    controller.setAnimation(LEVITATE_IDLE);
                }
            }

            case FLIGHT_START -> controller.setAnimation(FLIGHT_START);
            case HUNT_FLIGHT -> controller.setAnimation(HUNT_FLIGHT);
            case FLIGHT_STOP -> controller.setAnimation(FLIGHT_STOP);

            case GROUNDED -> {
                return super.movementController(event);
            }
        }

        if (this.getAbilityManager().isUsingAbility()) {
            return PlayState.STOP;
        }

        return PlayState.CONTINUE;
    }

    // =============================
    // 🧭 PATHFINDING
    // =============================

    @Override
    protected PathNavigation createNavigation(Level level) {
        this.cachedFlyingNavigation = new FlyingPathNavigation(this, level);
        this.cachedFlyingNavigation.setCanOpenDoors(false);
        this.cachedFlyingNavigation.setCanFloat(true);
        return this.cachedFlyingNavigation;
    }

    protected FlyingPathNavigation getFlyingNavigation() {
        if (this.cachedFlyingNavigation == null) {
            this.cachedFlyingNavigation = new FlyingPathNavigation(this, this.level());
            this.cachedFlyingNavigation.setCanOpenDoors(false);
            this.cachedFlyingNavigation.setCanFloat(true);
        }
        return this.cachedFlyingNavigation;
    }

    // =============================
    // 🚀 MOVIMENTO
    // =============================

    @Override
    public void travel(Vec3 travelVector) {

        if (getFlightState() != FlightState.GROUNDED) {

            double speed = switch (getFlightState()) {
                case HUNT_FLIGHT -> 0.08;
                case FLIGHT_START -> 0.04;
                case FLIGHT_STOP -> 0.02;
                default -> 0.04;
            };

            this.moveRelative((float) speed, travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.91D));

        } else {
            super.travel(travelVector);
        }
    }

    // =============================
    // 🔄 TICK
    // =============================

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) return;

        ticksSinceFlightChange++;
        stateTimer++;

        updateFlightState();
        updateParticleEffects();
    }

    // =============================
    // 🧠 LÓGICA DE VOO
    // =============================

    protected void updateFlightState() {

        LivingEntity target = this.getTarget();
        double distance = target != null ? this.distanceTo(target) : 999;

        switch (getFlightState()) {

            case GROUNDED -> {
                if (target != null && random.nextFloat() < getCombatFlightChance()) {
                    setFlightState(FlightState.FLIGHT_START);
                }
            }

            case LEVITATE -> {
                if (target != null && distance > 6) {
                    setFlightState(FlightState.FLIGHT_START);
                }
            }

            case FLIGHT_START -> {
                if (stateTimer > 10) {
                    forceSetFlightState(FlightState.HUNT_FLIGHT);
                }
            }

            case HUNT_FLIGHT -> {
                if (distance < 3) {
                    setFlightState(FlightState.FLIGHT_STOP);
                }
            }

            case FLIGHT_STOP -> {
                if (stateTimer > 10) {
                    setFlightState(FlightState.LEVITATE);
                }
            }
        }
    }

    // =============================
    // ✨ EFEITOS DE PARTÍCULAS
    // =============================

    /**
     * Atualiza efeitos visuais baseado no estado de voo.
     */
    private void updateParticleEffects() {
        FlightState currentState = getFlightState();

        if (currentState == FlightState.HUNT_FLIGHT) {
            sonicBoomTicks++;

            // Spawn rastro a cada SONIC_BOOM_INTERVAL ticks
            if (sonicBoomTicks % SONIC_BOOM_INTERVAL == 0) {
                AbilityParticleEffects.spawnHuntFlightTrail(
                        this.level(),
                        this.position(),
                        this.getDeltaMovement()
                );
            }

            // Efeito de boom adicional a cada 10 ticks
            if (sonicBoomTicks % 10 == 0) {
                AbilityParticleEffects.spawnSonicBoomEffect(
                        this.level(),
                        this.position(),
                        1.2
                );
            }
        }
        else if (currentState == FlightState.FLIGHT_START) {
            sonicBoomTicks++;

            if (sonicBoomTicks % 3 == 0) {
                AbilityParticleEffects.spawnSonicBoomEffect(
                        this.level(),
                        this.position(),
                        0.8
                );
            }
        }
        else {
            sonicBoomTicks = 0;
        }
    }

    // =============================
    // 🛡️ PROTEÇÕES VILTRUMITA
    // =============================


    // =============================
    // ⚙️ CONFIG OVERRIDES
    // =============================

    protected float getRandomFlightToggleChance() {
        return RANDOM_FLIGHT_TOGGLE_CHANCE;
    }

    protected float getCombatFlightChance() {
        return COMBAT_FLIGHT_CHANCE;
    }
}