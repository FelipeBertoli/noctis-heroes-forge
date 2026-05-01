package com.noctisheroes.entity.ai.goals;

import com.noctisheroes.entity.ai.states.FlightState;
import com.noctisheroes.entity.entities.viltrumite.base.AbstractViltrumite;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/**
 * Goal de perseguição em voo com efeitos de sonic boom.
 *
 * ✅ Melhorias:
 * - Rastro visual durante hunt flight
 * - Efeitos sincronizados com movimento
 * - Interpolação suave de velocidade
 */
public class FlyingChaseGoal extends Goal {

    private final AbstractViltrumite mob;
    private LivingEntity target;

    public FlyingChaseGoal(AbstractViltrumite mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        target = mob.getTarget();
        return target != null && mob.getFlightState() == FlightState.HUNT_FLIGHT;
    }

    @Override
    public boolean canContinueToUse() {
        return target != null && target.isAlive()
                && mob.getFlightState() == FlightState.HUNT_FLIGHT;
    }

    @Override
    public void stop() {
        this.target = null;
    }

    @Override
    public void tick() {
        if (target == null) return;

        // =============================
        // 🚀 MOVIMENTO E DIREÇÃO
        // =============================

        double dx = target.getX() - mob.getX();
        double dy = (target.getY() + target.getBbHeight() * 0.5) - mob.getY();
        double dz = target.getZ() - mob.getZ();

        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

        // Normaliza para evitar movimento infinito
        if (distance > 0.1) {
            dx /= distance;
            dy /= distance;
            dz /= distance;
        } else {
            return; // Muito perto, parar
        }

        // Ajusta velocidade baseado na distância
        double speed = 0.08;

        // Reduz velocidade quando perto demais
        if (distance < 4.0) {
            speed *= 0.5;
        }

        // Aplica movimento com interpolação suave
        Vec3 currentVelocity = mob.getDeltaMovement();
        Vec3 desiredVelocity = new Vec3(dx * speed, dy * speed, dz * speed);
        Vec3 newVelocity = currentVelocity.lerp(desiredVelocity, 0.15);

        mob.setDeltaMovement(newVelocity);

        // =============================
        // 👁️ OLHAR PARA ALVO
        // =============================

        double pitch = -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
        double yaw = Math.toDegrees(Math.atan2(dz, dx)) - 90;

        mob.setXRot((float) pitch);
        mob.setYRot((float) yaw);

    }
}