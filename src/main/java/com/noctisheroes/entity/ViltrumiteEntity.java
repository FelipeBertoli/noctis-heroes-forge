package com.noctisheroes.entity;

import com.noctisheroes.entity.ai.ViltrumiteMoveControl;
import com.noctisheroes.entity.ai.ViltrumiteNavigation;
import com.noctisheroes.entity.ai.ViltrumiteState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.phys.Vec3;

public class ViltrumiteEntity extends Monster {

    // ── Syncronização de dados ────────────────────────────────────────────────
    private static final EntityDataAccessor<Integer> SKIN_ID =
            SynchedEntityData.defineId(ViltrumiteEntity.class, EntityDataSerializers.INT);

    // Estado sincronizado para o cliente (animações futuras, partículas, etc.)
    private static final EntityDataAccessor<Integer> STATE_ID =
            SynchedEntityData.defineId(ViltrumiteEntity.class, EntityDataSerializers.INT);

    public static final int SKIN_COUNT = 3;

    // ── Constantes de velocidade por estado ──────────────────────────────────
    private static final double SPEED_PASSIVE = 0.30D;   // 🟢 patrulha normal
    private static final double SPEED_ALERT   = 2.00D;   // 🟡 detectou, acelera
    private static final double SPEED_ATTACK  = 1.00D;   // 🔴 ataque, velocidade máxima

    // Distância (blocos) para transições de estado
    private static final double DIST_DETECT = 24.0D;  // entra em ALERT
    private static final double DIST_ATTACK  = 8.0D;  // entra em ATTACK

    // ─────────────────────────────────────────────────────────────────────────

    // Construtor — troca o FlyingMoveControl pelo custom
    public ViltrumiteEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.moveControl = new ViltrumiteMoveControl(this); // <-- troca aqui
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new ViltrumiteNavigation(this, level);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier,
                                   net.minecraft.world.damagesource.DamageSource source) {
        return false;
    }

    // ── SynchedData ──────────────────────────────────────────────────────────
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SKIN_ID, 0);
        this.entityData.define(STATE_ID, ViltrumiteState.PASSIVE.ordinal());
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
        int stateOrdinal = tag.getInt("ViltrumiteState");
        setState(ViltrumiteState.values()[stateOrdinal]);
    }

    // ── Spawn ─────────────────────────────────────────────────────────────────
    @Override
    public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(
            net.minecraft.world.level.ServerLevelAccessor level,
            net.minecraft.world.DifficultyInstance difficulty,
            net.minecraft.world.entity.MobSpawnType spawnType,
            net.minecraft.world.entity.SpawnGroupData spawnGroupData,
            CompoundTag dataTag) {

        setSkinId(this.random.nextInt(SKIN_COUNT));
        setState(ViltrumiteState.PASSIVE);
        applySpeedForState(ViltrumiteState.PASSIVE);

        return super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData, dataTag);
    }

    // ── Tick principal — máquina de estados ──────────────────────────────────
    // tick() — remove o tratamento de onGround, não é mais necessário
    @Override
    public void tick() {
        super.tick();

        // Zera gravidade manualmente — impede o mob de cair
        if (!this.level().isClientSide) {
            Vec3 mov = this.getDeltaMovement();
            this.setDeltaMovement(mov.x, mov.y - 0.0D, mov.z); // sem gravidade
            updateState();
        }
    }

    private void updateState() {
        LivingEntity target = this.getTarget();
        ViltrumiteState current = getState();
        ViltrumiteState next = current;

        if (target != null && target.isAlive()) {
            double dist = this.distanceTo(target); // <-- sem Sqr

            if (dist <= DIST_ATTACK) {
                next = ViltrumiteState.ATTACK;
            } else {
                next = ViltrumiteState.ALERT;
            }
        } else {
            Player nearestPlayer = this.level().getNearestPlayer(this, DIST_DETECT);
            if (nearestPlayer != null && !nearestPlayer.isSpectator() && !nearestPlayer.isCreative()) {
                next = ViltrumiteState.ALERT;
                this.setTarget(nearestPlayer);
            } else {
                next = ViltrumiteState.PASSIVE;
            }
        }

        if (next != current) {
            setState(next);
            applySpeedForState(next);

            String msg = switch (next) {
                case PASSIVE -> "§a[Viltrumita] Voltou à patrulha.";
                case ALERT   -> "§e[Viltrumita] Detectou um alvo! Perseguindo...";
                case ATTACK  -> "§c[Viltrumita] Modo de ataque ativado!";
            };

            this.level().players().forEach(p ->
                    p.sendSystemMessage(net.minecraft.network.chat.Component.literal(msg))
            );
        }
    }

    /** Ajusta os atributos de velocidade conforme o estado. */
    private void applySpeedForState(ViltrumiteState state) {
        var speedAttr    = this.getAttribute(Attributes.MOVEMENT_SPEED);
        var flyingAttr   = this.getAttribute(Attributes.FLYING_SPEED);

        if (speedAttr == null || flyingAttr == null) return;

        switch (state) {
            case PASSIVE -> {
                speedAttr.setBaseValue(SPEED_PASSIVE);
                flyingAttr.setBaseValue(SPEED_PASSIVE);
            }
            case ALERT -> {
                speedAttr.setBaseValue(SPEED_ALERT);
                flyingAttr.setBaseValue(SPEED_ALERT);
            }
            case ATTACK -> {
                speedAttr.setBaseValue(SPEED_ATTACK);
                flyingAttr.setBaseValue(SPEED_ATTACK);
            }
        }
    }

    // ── Goals ─────────────────────────────────────────────────────────────────
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomFlyingGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // ── Atributos base ───────────────────────────────────────────────────────
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

    // ── Getters/Setters ──────────────────────────────────────────────────────
    public int getSkinId() { return this.entityData.get(SKIN_ID); }
    public void setSkinId(int id) { this.entityData.set(SKIN_ID, id); }

    public ViltrumiteState getState() {
        return ViltrumiteState.values()[this.entityData.get(STATE_ID)];
    }
    public void setState(ViltrumiteState state) {
        this.entityData.set(STATE_ID, state.ordinal());
    }
}