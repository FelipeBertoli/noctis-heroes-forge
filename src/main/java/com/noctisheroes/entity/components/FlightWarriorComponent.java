package com.noctisheroes.entity.components;

import com.noctisheroes.common.ability.helpers.AbilityParticleEffects;
import com.noctisheroes.entity.NoctisEntity;
import com.noctisheroes.entity.ai.states.FlightState;
import com.noctisheroes.entity.interfaces.IFlightCapable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;


public class FlightWarriorComponent {

    // =============================
    // 📦 CONFIGURAÇÃO (STATIC)
    // =============================

    private int huntBoostCooldown = 0;

    protected static final int BOOST_INTERVAL = 40; // 2s
    protected static final double BOOST_FORCE = 1.8;

    private static final int MIN_TICKS_BETWEEN_FLIGHT_CHANGE = 10;
    private static final int SONIC_BOOM_INTERVAL = 2;

    // =============================
    // 🔄 STATE
    // =============================

    private final IFlightCapable entity;
    private int stateTimer = 0;
    private int ticksSinceFlightChange = 0;
    private int sonicBoomTicks = 0;

    // =============================
    // 🏗️ CONSTRUTOR
    // =============================

    /**
     * @param entity Entidade que implementa IFlightCapable
     */
    public FlightWarriorComponent(IFlightCapable entity) {
        this.entity = entity;
    }

    // =============================
    // 🔄 TICK PRINCIPAL
    // =============================

    /**
     * Chamado a cada tick do servidor.
     * Atualiza estado de voo e efeitos.
     */
    public void tick(NoctisEntity noctisEntity) {
        // Apenas no servidor
        if (noctisEntity.level().isClientSide) return;

        ticksSinceFlightChange++;
        stateTimer++;

        // Atualizar lógica de estado
        updateFlightState(noctisEntity);

        // Atualizar efeitos visuais
        updateParticleEffects(noctisEntity);

        handleHuntBoost(noctisEntity);
    }

    // =============================
    // 🧠 LÓGICA DE VOO
    // =============================

    /**
     * Atualiza o estado de voo baseado na situação da entidade.
     *
     * State machine:
     * GROUNDED → FLIGHT_START → HUNT_FLIGHT → FLIGHT_STOP → LEVITATE → GROUNDED
     */
    private void updateFlightState(NoctisEntity noctisEntity) {
        LivingEntity target = noctisEntity.getTarget();
        double distance = target != null ? noctisEntity.distanceTo(target) : 999;

        FlightState currentState = entity.getFlightState();
        FlightState nextState = currentState;

        switch (currentState) {
            case GROUNDED -> {
                // Em combate, iniciar voo
                if (target != null && noctisEntity.getRandom().nextFloat() < entity.getCombatFlightChance()) {
                    nextState = FlightState.FLIGHT_START;
                }
            }

            case LEVITATE -> {
                // Se alvo está longe, começar perseguição aérea
                if (target != null && distance > 6) {
                    nextState = FlightState.FLIGHT_START;
                }
            }

            case FLIGHT_START -> {
                // Depois de 10 ticks, passar para hunt
                if (stateTimer > 10) {
                    nextState = FlightState.HUNT_FLIGHT;
                    stateTimer = 0;
                }
            }

            case HUNT_FLIGHT -> {
                // Se chegou muito perto, parar voo
                if (distance < 3) {
                    nextState = FlightState.FLIGHT_STOP;
                }
            }

            case FLIGHT_STOP -> {
                // Depois de transição, volta ao levitate
                if (stateTimer > 10) {
                    nextState = FlightState.LEVITATE;
                    stateTimer = 0;
                }
            }
        }

        // Aplicar mudança de estado
        if (nextState != currentState) {
            entity.setFlightState(nextState);
        }
    }

    // =============================
    // ✨ EFEITOS DE PARTÍCULAS
    // =============================

    /**
     * Atualiza efeitos visuais (partículas) baseado no estado.
     */
    private void updateParticleEffects(NoctisEntity noctisEntity) {
        FlightState currentState = entity.getFlightState();

        if (currentState == FlightState.HUNT_FLIGHT) {
            sonicBoomTicks++;

            // Spawn rastro de voo a cada SONIC_BOOM_INTERVAL ticks
            if (sonicBoomTicks % SONIC_BOOM_INTERVAL == 0) {
                AbilityParticleEffects.spawnKineticHuntFlightTrail(
                        noctisEntity.level(),
                        noctisEntity.position(),
                        noctisEntity.getDeltaMovement()
                );
            }
        }
        else if (currentState == FlightState.FLIGHT_START) {
            sonicBoomTicks++;

            // Efeito de boom no início do voo
            if (sonicBoomTicks % 3 == 0) {
                AbilityParticleEffects.spawnKineticBoomEffect(
                        noctisEntity.level(),
                        noctisEntity.position(),
                        0.8
                );
            }
        }
        else {
            // Reset quando não está voando
            sonicBoomTicks = 0;
        }
    }

    // =============================
    // 🚀 LÓGICA DE MOVIMENTO
    // =============================

    /**
     * Aplica velocidade de voo à entidade.
     *
     * Chamado em travel() da entidade.
     *
     * @param travelVector Direção do movimento
     * @param noctisEntity Entidade controlada
     */
    public void applyFlightMovement(Vec3 travelVector, NoctisEntity noctisEntity) {
        FlightState state = entity.getFlightState();

        if (state == FlightState.GROUNDED) {
            // Sem voo, movimento normal é aplicado pela entidade
            return;
        }

        // Calcular velocidade baseado no estado
        double speed = switch (state) {
            case HUNT_FLIGHT -> 0.08;
            case FLIGHT_START -> 0.04;
            case FLIGHT_STOP -> 0.02;
            default -> 0.04;
        };

        // Aplicar movimento relativo
        noctisEntity.moveRelative((float) speed, travelVector);
        noctisEntity.move(MoverType.SELF, noctisEntity.getDeltaMovement());

        // Aplicar drag/friction
        noctisEntity.setDeltaMovement(noctisEntity.getDeltaMovement().scale(0.91D));
    }

    // =============================
    // 📊 GETTERS / ESTADO
    // =============================

    public int getStateTimer() {
        return stateTimer;
    }

    public int getTicksSinceFlightChange() {
        return ticksSinceFlightChange;
    }

    public int getSonicBoomTicks() {
        return sonicBoomTicks;
    }

    public void resetStateTimer() {
        this.stateTimer = 0;
    }

    public void resetFlightChangeTimer() {
        this.ticksSinceFlightChange = 0;
    }

    // =============================
    // 🔧 RESET PARA NOVA ENTIDADE
    // =============================

    /**
     * Reseta o componente (chamar ao criar nova entidade).
     */
    public void reset() {
        this.stateTimer = 0;
        this.ticksSinceFlightChange = 0;
        this.sonicBoomTicks = 0;
    }

    private void handleHuntBoost(NoctisEntity entity) {
        if (this.entity.getFlightState() != FlightState.HUNT_FLIGHT) return;

        sonicBoomTicks++;

        if (sonicBoomTicks < BOOST_INTERVAL) return;

        sonicBoomTicks = 0;

        Vec3 direction;

        if (entity.getTarget() != null) {
            direction = entity.getTarget()
                    .position()
                    .subtract(entity.position())
                    .normalize();
        } else {
            direction = entity.getLookAngle();
        }

        entity.setDeltaMovement(
                entity.getDeltaMovement().add(
                        direction.x * BOOST_FORCE,
                        direction.y * 0.3,
                        direction.z * BOOST_FORCE
                )
        );

        spawnBoostEffect(entity);
    }

    private void spawnBoostEffect(NoctisEntity entity) {
        if (!(entity.level() instanceof ServerLevel server)) return;

        server.sendParticles(
                ParticleTypes.EXPLOSION_EMITTER,
                entity.getX(),
                entity.getY(0.5),
                entity.getZ(),
                1,
                0, 0, 0,
                0
        );

        entity.level().playSound(
                null,
                entity.blockPosition(),
                SoundEvents.GENERIC_EXPLODE,
                SoundSource.HOSTILE,
                0.6f,
                1.2f
        );
    }
}