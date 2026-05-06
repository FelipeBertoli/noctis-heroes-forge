package com.noctisheroes.entity.ai.goals;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

/**
 * DynamicTargetGoal - Um NearestAttackableTargetGoal melhorado com funcionalidades dinâmicas
 *
 * Funcionalidades:
 * - Sempre ataca o alvo mais próximo, trocando automaticamente se outro alvo estiver mais perto
 * - Detection & Follow Range baseados em FOLLOW_RANGE (atributo vanilla)
 * - Follow Range customizável via multiplier do FOLLOW_RANGE
 * - Suporte para detecção através de paredes (ignoreLineOfSight)
 * - Cooldown para evitar spam de mudanças de alvo
 */
public class DynamicTargetGoal<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {

    // =============================
    // 📊 CONFIGURAÇÃO
    // =============================

    /** Multiplicador do FOLLOW_RANGE para definir Follow Range */
    private final double followRangeMultiplier;

    /** Se true, ignora Line of Sight (vê através de paredes) */
    private final boolean ignoreLineOfSight;

    /** Cooldown em ticks para evitar trocar de alvo muito rapidamente */
    private final int targetSwitchCooldown;

    /** Ticker para o cooldown */
    private int targetSwitchTicker = 0;

    // =============================
    // 🎯 CACHE
    // =============================

    /** Cache do último alvo mais próximo */
    @Nullable
    private LivingEntity lastNearestTarget;

    /** Distância do último alvo mais próximo */
    private double lastNearestDistance = Double.MAX_VALUE;

    // =============================
    // 🏗️ CONSTRUTORES
    // =============================

    /**
     * Cria um DynamicTargetGoal com configuração completa
     *
     * @param mob A entidade que vai usar este goal
     * @param targetClass Classe do alvo (Player.class, AbstractVillager.class, etc)
     * @param followRangeMultiplier Multiplicador do FOLLOW_RANGE para follow (ex: 1.0)
     * @param ignoreLineOfSight Se true, vê através de paredes
     * @param targetSwitchCooldown Cooldown em ticks para trocar de alvo (ex: 5)
     */
    public DynamicTargetGoal(Mob mob, Class<T> targetClass,
                             double followRangeMultiplier,
                             boolean ignoreLineOfSight,
                             int targetSwitchCooldown) {
        super(mob, targetClass, 10, true, false, null);

        this.followRangeMultiplier = followRangeMultiplier;
        this.ignoreLineOfSight = ignoreLineOfSight;
        this.targetSwitchCooldown = targetSwitchCooldown;

        // ✅ Configura TargetingConditions baseado na configuração
        setupTargetingConditions();
    }

    /**
     * Versão simplificada com padrões sensatos
     *
     * @param mob A entidade que vai usar este goal
     * @param targetClass Classe do alvo
     * @param ignoreLineOfSight Se true, vê através de paredes
     */
    public DynamicTargetGoal(Mob mob, Class<T> targetClass, boolean ignoreLineOfSight) {
        this(mob, targetClass, 1.0, ignoreLineOfSight, 5);
    }

    // =============================
    // ⚙️ SETUP
    // =============================

    private void setupTargetingConditions() {

        double followRange = getFollowRange();

        TargetingConditions conditions = TargetingConditions.forCombat()
                .range(followRange);

        if (ignoreLineOfSight) {
            conditions = conditions.ignoreLineOfSight();
        }

        this.targetConditions = conditions;
    }

    // =============================
    // 📏 RANGE & DETECTION
    // =============================

    /**
     * Retorna o Follow Range baseado em FOLLOW_RANGE * multiplier
     */
    protected double getFollowRange() {
        double followRange = this.mob.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.FOLLOW_RANGE);
        return followRange * followRangeMultiplier;
    }

    // =============================
    // 🎯 TARGET FINDING
    // =============================

    @Override
    public boolean canUse() {
        // ✅ Atualiza o cooldown
        if (targetSwitchTicker > 0) {
            targetSwitchTicker--;
        }

        // Executa a lógica de busca de alvo
        this.target = findNearestTarget();

        return this.target != null;
    }

    @Override
    public void tick() {
        // ✅ A cada tick, verifica se há um alvo mais próximo
        LivingEntity currentTarget = this.mob.getTarget();

        if (currentTarget != null && targetSwitchTicker <= 0) {
            LivingEntity nearestTarget = findNearestTarget();

            // Se encontrou um alvo e ele é diferente do atual
            if (nearestTarget != null && nearestTarget != currentTarget) {
                double distToNearest = this.mob.distanceToSqr(nearestTarget);
                double distToCurrent = this.mob.distanceToSqr(currentTarget);

                // ✅ Se o novo alvo é significativamente mais perto, troca
                if (distToNearest < distToCurrent) {
                    this.mob.setTarget(nearestTarget);
                    this.target = nearestTarget;
                    activateTargetSwitchCooldown();
                }
            }
        }

        super.tick();
    }

    /**
     * Encontra o alvo mais próximo dentro do FOLLOW_RANGE
     */
    @Nullable
    protected LivingEntity findNearestTarget() {
        double followRange = getFollowRange();
        AABB searchBox = this.mob.getBoundingBox().inflate(followRange);

        // ✅ Busca todos os alvos potenciais
        List<T> possibleTargets = this.mob.level().getEntitiesOfClass(
                this.targetType,
                searchBox,
                (target) -> canTargetEntity(target)
        );

        // ✅ Encontra o mais próximo
        return possibleTargets.stream()
                .min(Comparator.comparingDouble(target -> this.mob.distanceToSqr(target)))
                .orElse(null);
    }

    /**
     * Valida se uma entidade pode ser alvo
     */
    protected boolean canTargetEntity(LivingEntity target) {
        // Checa distância usando FOLLOW_RANGE
        double followRange = getFollowRange();
        if (this.mob.distanceToSqr(target) > followRange * followRange) {
            return false;
        }

        // ✅ Se não ignora LOS, checa linha de visão
        if (!ignoreLineOfSight && !this.mob.hasLineOfSight(target)) {
            return false;
        }

        // Checa outras condições padrão
        return this.targetConditions.test(this.mob, target);
    }

    // =============================
    // ⏱️ COOLDOWN
    // =============================

    protected void activateTargetSwitchCooldown() {
        this.targetSwitchTicker = targetSwitchCooldown;
    }

    // =============================
    // 📊 GETTERS
    // =============================

    public double getFollowRangeMultiplier() {
        return followRangeMultiplier;
    }

    public boolean isIgnoringLineOfSight() {
        return ignoreLineOfSight;
    }

    public int getTargetSwitchCooldown() {
        return targetSwitchCooldown;
    }

    @Nullable
    public LivingEntity getLastNearestTarget() {
        return lastNearestTarget;
    }
}