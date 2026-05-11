package com.noctisheroes.entity.ai.flight;

import com.noctisheroes.common.particle.AbilityParticleEffects;
import com.noctisheroes.entity.NoctisEntity;
import com.noctisheroes.entity.interfaces.IFlightCapable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class FlightWarriorComponent {

    private final IFlightCapable flightEntity;
    private int stateTicks = 0;
    private int sonicBoomCooldown = 0;
    private double sonicSpeedMultiplier = 1.3;
    private int sonicTicks = 0;

    private static final int MIN_TICKS_BETWEEN_FLIGHT_CHANGE = 10;
    private static final int SONIC_BOOM_INTERVAL = 2;


    public FlightWarriorComponent(IFlightCapable flightEntity) {
        this.flightEntity = flightEntity;
    }

    public void tick( NoctisEntity entity) {
        if (entity.level().isClientSide) return;
        stateTicks++;
        updateState(entity);
        updateSonicScaling();
        applyFlightPhysics(entity);
        handleSonicBoom(entity);
    }

    private void updateState( NoctisEntity entity) {

        if (entity.abilityBlocking()) return;

        LivingEntity target = entity.getTarget();
        FlightState current = flightEntity.getFlightState();

        double distance = target != null ? entity.distanceTo(target) : 999;

        switch (current) {
            case GROUNDED -> {
                if (target != null && distance > 5) { switchState( FlightState.FLIGHT_START);}
            }

            case FLIGHT_START -> {
                if (stateTicks > 12) { switchState(FlightState.HUNT_FLIGHT);}
            }

            case HUNT_FLIGHT -> {
                if (target == null) { switchState(FlightState.FLIGHT_STOP); }
                else if (stateTicks > 25 && target.getDeltaMovement().lengthSqr() < 0.04 && entity.distanceTo(target) <= 10) { switchState(FlightState.FLIGHT_STOP); }
            }

            case FLIGHT_STOP -> {
                if (stateTicks > 10) { switchState( FlightState.LEVITATE);}
            }

            case LEVITATE -> {
                if (target != null && distance > 10) {switchState( FlightState.FLIGHT_START);}
            }
        }
    }

    private void updateSonicScaling() {

        FlightState state = flightEntity.getFlightState();

        if (state == FlightState.HUNT_FLIGHT) {
            sonicTicks++;
            sonicSpeedMultiplier = Math.min(2.0, 1.0 + (sonicTicks * 0.015));
        }

        else {
            sonicTicks = 0;
            sonicSpeedMultiplier = 1.3;
        }
    }

    private void applyFlightPhysics(NoctisEntity entity) {

        if (entity.abilityBlocking()) {
            entity.getNavigation().stop();
            entity.setDeltaMovement(Vec3.ZERO);
            entity.lerpMotion(0, 0, 0);
            return;
        }
        FlightState state = flightEntity.getFlightState();

        if (state == FlightState.GROUNDED) { return; }
        LivingEntity target = entity.getTarget();
        Vec3 velocity = entity.getDeltaMovement();
        Vec3 desired = Vec3.ZERO;

        if (target != null) {
            Vec3 targetPos = target.position().add(0, target.getBbHeight() * 0.5, 0);
            desired = targetPos.subtract(entity.position()).normalize();
        }

        double acceleration = switch (state) {
            case FLIGHT_START -> 0.3;
            case HUNT_FLIGHT -> 0.3 * sonicSpeedMultiplier;
            case LEVITATE -> 0.06;
            case FLIGHT_STOP -> 0.009;
            default -> 0.0;
        };

        double maxSpeed = switch (state) {
            case FLIGHT_START -> 0.5;
            case HUNT_FLIGHT -> 0.5 * sonicSpeedMultiplier;
            case LEVITATE -> 0.08;
            case FLIGHT_STOP -> 0.13;
            default -> 0.0;
        };

        double drag = switch (state) {
            case HUNT_FLIGHT -> 0.985 + Math.min(0.01, sonicSpeedMultiplier * 0.003);
            case LEVITATE -> 0.90;
            default -> 0.93;
        };

        velocity = velocity.add(desired.scale(acceleration));

        if (velocity.length() > maxSpeed) {
            velocity = velocity.normalize().scale(maxSpeed);
        }


        velocity = velocity.scale(drag);
        entity.setDeltaMovement(velocity);
        rotateTowardsVelocity(entity, velocity);
    }

    private void handleSonicBoom( NoctisEntity entity) {
        if (sonicBoomCooldown > 0) { sonicBoomCooldown--;}
        double speed = entity.getDeltaMovement().length();
        if (speed > 0.65 && sonicBoomCooldown <= 0) {
            AbilityParticleEffects.spawnSonicBoomEffect(entity);
            sonicBoomCooldown = 40;
        }
    }

    private void rotateTowardsVelocity( NoctisEntity entity, Vec3 velocity) {

        if (velocity.lengthSqr() < 0.0001) { return; }

        double horizontal = Math.sqrt( velocity.x * velocity.x + velocity.z * velocity.z);
        float yaw = (float) ( Math.toDegrees( Math.atan2(velocity.z, velocity.x)) - 90F);
        float pitch = (float) (-Math.toDegrees(Math.atan2(velocity.y, horizontal)));

        entity.setYRot(yaw);
        entity.setXRot(pitch);
        entity.yBodyRot = yaw;
        entity.yHeadRot = yaw;
    }

    private void switchState( FlightState state) {
        flightEntity.setFlightState(state);
        stateTicks = 0;
    }

    public double getSonicSpeedMultiplier() { return sonicSpeedMultiplier;}

    public int getStateTicks() { return stateTicks;}

    public FlightState getCurrentState() { return flightEntity.getFlightState(); }
}