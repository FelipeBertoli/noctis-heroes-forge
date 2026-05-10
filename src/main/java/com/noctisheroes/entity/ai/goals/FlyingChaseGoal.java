package com.noctisheroes.entity.ai.goals;

import com.noctisheroes.entity.ai.flight.FlightState;
import com.noctisheroes.entity.ai.flight.FlightWarriorComponent;
import com.noctisheroes.entity.interfaces.IFlightCapable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class FlyingChaseGoal extends Goal {

    private final Mob mob;
    private final IFlightCapable flightMob;
    private final FlightWarriorComponent flightComponent;
    private LivingEntity target;

    // =========================================
    // 🏗️ CONSTRUTOR
    // =========================================

    public FlyingChaseGoal(
            Mob mob,
            IFlightCapable flightMob,
            FlightWarriorComponent flightComponent
    ) {

        this.mob = mob;
        this.flightMob = flightMob;
        this.flightComponent = flightComponent;

        this.setFlags(
                EnumSet.of(
                        Flag.MOVE,
                        Flag.LOOK
                )
        );
    }

    // =========================================
    // ✅ LÓGICA DE ATIVAÇÃO
    // =========================================

    @Override
    public boolean canUse() {

        target = mob.getTarget();

        if (target == null || !target.isAlive()) {
            return false;
        }

        // =====================================
        // VERIFICA PROXIMIDADE E VISIBILIDADE
        // =====================================

        double distance = mob.distanceTo(target);

        return distance <= 30
                && mob.hasLineOfSight(target);
    }

    // =========================================
    // ⏱️ CONTINUAR USANDO
    // =========================================

    @Override
    public boolean canContinueToUse() {

        if (target == null || !target.isAlive()) {
            return false;
        }

        // Continua enquanto o alvo estiver vivo
        return mob.hasLineOfSight(target);
    }

    // =========================================
    // 🔄 TICK PRINCIPAL
    // =========================================

    @Override
    public void tick() {

        FlightState currentState = flightMob.getFlightState();

        // =====================================
        // 1️⃣ SE ESTÁ NO CHÃO
        // =====================================

        if (currentState == FlightState.GROUNDED) {

            // Persegue no chão usando navegação padrão
            mob.getNavigation().moveTo(target, 1.0);

            // Se alvo está longe, começa a voar
            if (mob.distanceTo(target) > 5) {
                // O FlightWarriorComponent vai mudar o estado automaticamente
            }
        }

        // =====================================
        // 2️⃣ SE ESTÁ VOANDO (qualquer estado)
        // =====================================

        else {

            // Para navegação no chão
            mob.getNavigation().stop();

            // O FlightWarriorComponent controla a física do voo
            // Apenas mantemos o alvo atualizado
            updateTargetRotation();
        }
    }

    // =========================================
    // 🎯 ROTAÇÃO PARA ALVO
    // =========================================

    private void updateTargetRotation() {

        if (target == null) {
            return;
        }

        // Calcula direção para o alvo
        double dx = target.getX() - mob.getX();
        double dy = target.getY() - mob.getY();
        double dz = target.getZ() - mob.getZ();

        double horizontal = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) (
                Math.toDegrees(
                        Math.atan2(dz, dx)
                ) - 90F
        );

        float pitch = (float) (
                -Math.toDegrees(
                        Math.atan2(dy, horizontal)
                )
        );

        // Aplica rotação suavemente
        mob.setYRot(yaw);
        mob.yBodyRot = yaw;
        mob.yHeadRot = yaw;

        if (flightMob.getFlightState() != FlightState.LEVITATE) {
            mob.setXRot(pitch);
        }
    }

    // =========================================
    // 🛑 PARAR
    // =========================================

    @Override
    public void stop() {

        target = null;
        mob.getNavigation().stop();

        // O FlightWarriorComponent vai transicionar
        // para FLIGHT_STOP e depois LEVITATE automaticamente
    }

    // =========================================
    // 📊 GETTERS
    // =========================================

    public LivingEntity getTarget() {
        return target;
    }
}