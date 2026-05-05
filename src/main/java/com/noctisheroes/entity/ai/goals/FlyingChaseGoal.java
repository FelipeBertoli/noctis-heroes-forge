package com.noctisheroes.entity.ai.goals;

import com.noctisheroes.entity.ai.states.FlightState;
import com.noctisheroes.entity.interfaces.IFlightCapable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class FlyingChaseGoal extends Goal {

    private final IFlightCapable flightMob;
    private final Mob mob; // 🔥 acesso ao mundo real (Minecraft)
    private LivingEntity target;

    public FlyingChaseGoal(Mob mob, IFlightCapable flightMob) {
        this.mob = mob;
        this.flightMob = flightMob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        target = mob.getTarget();
        return target != null && flightMob.getFlightState() == FlightState.HUNT_FLIGHT;
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && target.isAlive()
                && flightMob.getFlightState() == FlightState.HUNT_FLIGHT;
    }

    @Override
    public void stop() {
        this.target = null;
    }

    @Override
    public void tick() {
        if (target == null) return;

        double dx = target.getX() - mob.getX();
        double dy = (target.getY() + target.getBbHeight() * 0.5) - mob.getY();
        double dz = target.getZ() - mob.getZ();

        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (distance <= 0.1) return;

        dx /= distance;
        dy /= distance;
        dz /= distance;

        double speed = 0.08;

        if (distance < 4.0) speed *= 0.5;
        if (distance > 10.0) speed *= 1.4; // 🔥 sensação de caça

        Vec3 currentVelocity = mob.getDeltaMovement();
        Vec3 desiredVelocity = new Vec3(dx * speed, dy * speed, dz * speed);

        Vec3 newVelocity = currentVelocity.lerp(desiredVelocity, 0.15);
        mob.setDeltaMovement(newVelocity);

        double pitch = -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
        double yaw = Math.toDegrees(Math.atan2(dz, dx)) - 90;

        mob.setXRot((float) pitch);
        mob.setYRot((float) yaw);
    }
}