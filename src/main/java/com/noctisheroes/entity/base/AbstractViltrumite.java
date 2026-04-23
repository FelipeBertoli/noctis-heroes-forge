package com.noctisheroes.entity.base;

import com.noctisheroes.entity.ai.ViltrumiteMoveControl;
import com.noctisheroes.entity.ai.ViltrumiteNavigation;
import com.noctisheroes.entity.ai.ViltrumiteState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public abstract class AbstractViltrumite extends AbstractMob
        implements software.bernie.geckolib.animatable.GeoEntity {

    // ── Animações ─────────────────────────────────────────────────────────────
    private static final RawAnimation ANIM_IDLE           = RawAnimation.begin().thenLoop("animation.viltrumite.idle");
    private static final RawAnimation ANIM_WALK           = RawAnimation.begin().thenLoop("animation.viltrumite.walk");
    private static final RawAnimation ANIM_ATTACK         = RawAnimation.begin().thenPlayAndHold("animation.viltrumite.attack");
    private static final RawAnimation ANIM_FLIGHT         = RawAnimation.begin().thenLoop("animation.viltrumite.flight");
    private static final RawAnimation ANIM_FLIGHT_START   = RawAnimation.begin().thenPlay("animation.viltrumite.flight_start").thenLoop("animation.viltrumite.flight");
    private static final RawAnimation ANIM_FLIGHT_STOP    = RawAnimation.begin().thenPlay("animation.viltrumite.flight_stop").thenLoop("animation.viltrumite.idle");
    private static final RawAnimation ANIM_FLIGHT_ATTACK  = RawAnimation.begin().thenPlayAndHold("animation.viltrumite.flight_attack");

    private final AnimatableInstanceCache animCache = GeckoLibUtil.createInstanceCache(this);

    // ── Estado de voo anterior — para detectar transições ─────────────────────
    private boolean wasFlying = false;
    private int swingingTicks = 0;

    // ── SynchedData ──────────────────────────────────────────────────────────
    private static final EntityDataAccessor<Integer> SKIN_ID =
            SynchedEntityData.defineId(AbstractViltrumite.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> STATE_ID =
            SynchedEntityData.defineId(AbstractViltrumite.class, EntityDataSerializers.INT);

    // ── Configuração — implemente nas subclasses ──────────────────────────────
    protected abstract int    getSkinCount();
    protected abstract double getSpeedPassive();
    protected abstract double getSpeedAlert();
    protected abstract double getSpeedAttack();
    protected abstract double getDetectRange();
    protected abstract double getAttackRange();

    // ── Construtor ────────────────────────────────────────────────────────────
    protected AbstractViltrumite(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.lookControl = new net.minecraft.world.entity.ai.control.LookControl(this);
        this.moveControl = new ViltrumiteMoveControl(this);
    }

    // ── Geckolib ─────────────────────────────────────────────────────────────
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {

        // Controlador de movimento — lógica completa de estados
        registrar.add(new AnimationController<>(this, "movement", 5, state -> {
            ViltrumiteState current = getState();
            boolean isFlying = current == ViltrumiteState.ALERT || current == ViltrumiteState.ATTACK;

            // Detecta transição de voo: entrou em ALERT/ATTACK (inicia voo)
            if (isFlying && !wasFlying) {
                wasFlying = true;
                return state.setAndContinue(ANIM_FLIGHT_START);
            }

            // Detecta transição de voo: saiu de ALERT/ATTACK (para voo)
            if (!isFlying && wasFlying) {
                wasFlying = false;
                return state.setAndContinue(ANIM_FLIGHT_STOP);
            }

            // Lógica de animação dentro de cada estado
            return switch (current) {
                case PASSIVE -> {
                    // No chão: walk se movendo, idle parado
                    // No ar: flight
                    if (this.onGround()) {
                        yield state.setAndContinue(state.isMoving() ? ANIM_WALK : ANIM_IDLE);
                    } else {
                        yield state.setAndContinue(ANIM_FLIGHT);
                    }
                }
                case ALERT, ATTACK -> state.setAndContinue(ANIM_FLIGHT);
            };
        }));

        // Controlador de ataque — monitorar swinging com timeout
        registrar.add(new AnimationController<>(this, "attack", 2, state -> {
            // Verifica se está atacando (swinging = true)
            if (this.swinging) {
                // Reseta a animação e incrementa o contador
                state.getController().forceAnimationReset();
                swingingTicks++;

                ViltrumiteState current = getState();

                return state.setAndContinue(ANIM_ATTACK);
            } else {
                // Não está atacando - reseta o contador
                swingingTicks = 0;
                return PlayState.STOP;
            }
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animCache;
    }

    // ── Navegação ─────────────────────────────────────────────────────────────
    @Override
    protected PathNavigation createNavigation(Level level) {
        return new ViltrumiteNavigation(this, level);
    }

    // ── Sem dano de queda ─────────────────────────────────────────────────────
    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier,
                                   net.minecraft.world.damagesource.DamageSource source) {
        return false;
    }

    @Override
    protected Vec3 getFluidFallVector() {
        return Vec3.ZERO;
    }

    // ── SynchedData ──────────────────────────────────────────────────────────
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(SKIN_ID, 0);
        entityData.define(STATE_ID, ViltrumiteState.PASSIVE.ordinal());
    }

    // ── NBT ──────────────────────────────────────────────────────────────────
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("SkinId", getSkinId());
        tag.putInt("ViltrumiteState", getState().ordinal());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setSkinId(tag.getInt("SkinId"));
        setState(ViltrumiteState.values()[tag.getInt("ViltrumiteState")]);
    }

    // ── Spawn ─────────────────────────────────────────────────────────────────
    @Override
    public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
            net.minecraft.world.level.ServerLevelAccessor level,
            net.minecraft.world.DifficultyInstance difficulty,
            net.minecraft.world.entity.MobSpawnType spawnType,
            net.minecraft.world.entity.SpawnGroupData spawnGroupData,
            CompoundTag dataTag) {
        setSkinId(random.nextInt(getSkinCount()));
        setState(ViltrumiteState.PASSIVE);
        applySpeedForState(ViltrumiteState.PASSIVE);
        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData, dataTag);
    }

    // ── Tick ──────────────────────────────────────────────────────────────────
    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            updateState();
        }
    }

    private void updateState() {
        LivingEntity target = getTarget();
        ViltrumiteState current = getState();
        ViltrumiteState next;

        if (target != null && target.isAlive()) {
            next = distanceTo(target) <= getAttackRange()
                    ? ViltrumiteState.ATTACK
                    : ViltrumiteState.ALERT;
        } else {
            Player nearest = level().getNearestPlayer(this, getDetectRange());
            if (nearest != null && !nearest.isSpectator() && !nearest.isCreative()) {
                setTarget(nearest);
                next = ViltrumiteState.ALERT;
            } else {
                next = ViltrumiteState.PASSIVE;
            }
        }

        if (next != current) {
            setState(next);
            applySpeedForState(next);
            broadcastStateMessage(next);
        }
    }

    private void applySpeedForState(ViltrumiteState state) {
        var speed  = getAttribute(Attributes.MOVEMENT_SPEED);
        var flying = getAttribute(Attributes.FLYING_SPEED);
        if (speed == null || flying == null) return;

        double value = switch (state) {
            case PASSIVE -> getSpeedPassive();
            case ALERT   -> getSpeedAlert();
            case ATTACK  -> getSpeedAttack();
        };

        speed.setBaseValue(value);
        flying.setBaseValue(value);
    }

    private void broadcastStateMessage(ViltrumiteState state) {
        String msg = switch (state) {
            case PASSIVE -> "§a[Viltrumita] Voltou à patrulha.";
            case ALERT   -> "§e[Viltrumita] Detectou um alvo! Perseguindo...";
            case ATTACK  -> "§c[Viltrumita] Modo de ataque ativado!";
        };
        level().players().forEach(p -> p.sendSystemMessage(Component.literal(msg)));
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────
    public int getSkinId() {
        return entityData.get(SKIN_ID);
    }

    protected void setSkinId(int id) {
        entityData.set(SKIN_ID, id);
    }

    public ViltrumiteState getState() {
        return ViltrumiteState.values()[entityData.get(STATE_ID)];
    }

    protected void setState(ViltrumiteState state) {
        entityData.set(STATE_ID, state.ordinal());
    }
}