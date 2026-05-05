package com.noctisheroes.entity.entities.base;

import com.noctisheroes.common.ability.abilities.DestructiveDashAbility;
import com.noctisheroes.common.ability.abilities.KineticPunchAbility;
import com.noctisheroes.common.attribute.AttributeConfig;
import com.noctisheroes.common.combat.damage.DamageConfig;
import com.noctisheroes.common.combat.rage.IRageUser;
import com.noctisheroes.common.config.EntityConfig;
import com.noctisheroes.common.combat.damage.DamageTags;
import com.noctisheroes.common.effect.EffectConfig;
import com.noctisheroes.common.effect.EffectType;
import com.noctisheroes.entity.components.RageComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

public abstract class AbstractDrakari extends AbstractFlightWarrior implements IRageUser {

    private final RageComponent rage;
    private final AttributeConfig attributes;

    protected AbstractDrakari(
            EntityType<? extends Monster> type,
            Level level,
            String tag,
            AttributeConfig attributes,
            EntityConfig config) {

        super(type, level, tag, attributes, config);
        this.attributes = attributes;
        this.rage = new RageComponent(this);

        this.getAbilityManager().register(new KineticPunchAbility());
        this.getAbilityManager().register(new DestructiveDashAbility());

        this.getDamageProfile()
                .ignoreFallDamage()
                .resist(DamageTags.EXPLOSION, attributes.explosionResistance)
                .resist(DamageTags.PROJECTILE, attributes.explosionWeakness)
                .weakTo(DamageTags.SONIC, 0.75f)
                .immuneTo("drakari");

//        this.getEffectManager().addEffect(
//                new EffectConfig.Builder()
//                        .type(EffectType.REGEN)
//                        .duration(Integer.MAX_VALUE)
//                        .amplifier(1)
//                        .tickInterval(20)
//                        .build()
//        );
    }

    // =============================
    // 🔄 TICK
    // =============================

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide) {
            rage.tick();
            float ragePercent = getRage().getPercent();
            applyRageScaling();
            spawnRageAura(ragePercent);
            applyRageRegen(ragePercent);
        }
    }

    // =============================
    // 💥 DANO → RAGE
    // =============================

    @Override
    public boolean hurt(DamageSource source, float amount) {

        DamageConfig ctx = new DamageConfig(source);

        // validação do sistema de dano
        if (!getDamageProfile().canBeDamaged(ctx)) {
            return false;
        }

        // aplica resistências / fraquezas
        amount = getDamageProfile().applyModifiers(ctx, amount);

        // 🔥 GERA RAGE USANDO SEU SISTEMA
        if (getRage() != null) {

            if (ctx.isPhysical() || ctx.isExplosion() || ctx.isProjectile()) {
                getRage().addFromDamage(amount);
            }
        }

        return super.hurt(source, amount);
    }

    // =============================
    // 📈 SCALING
    // =============================

    private void applyRageScaling() {

        float r = rage.getPercent();

        // escala progressiva
        double physicBonus = 1.0 + r * 1.5; // até +150%
        double speedBonus = 1.0 + r * 0.5;

        AttributeConfig base = this.getAttributeConfig();

        this.getAttribute(Attributes.ATTACK_DAMAGE)
                .setBaseValue(base.attackDamage * physicBonus);

        this.getAttribute(Attributes.MOVEMENT_SPEED)
                .setBaseValue(base.movementSpeed * speedBonus);

        this.getAttribute(Attributes.FLYING_SPEED)
                .setBaseValue(base.flyingSpeed * speedBonus);

        this.getAttribute(Attributes.ARMOR)
                .setBaseValue(base.armor * physicBonus);

        this.getAttribute(Attributes.ATTACK_KNOCKBACK)
                .setBaseValue(base.attackKnockback * physicBonus);

        this.getAttribute(Attributes.ATTACK_SPEED)
                .setBaseValue(base.attackSpeed * speedBonus);
    }

    private void applyRageRegen(float ragePercent) {

        // intervalo base (mais rage = mais frequência)
        int interval = (int) (20 - (ragePercent * 15)); // 20 → 5 ticks

        if (interval < 5) interval = 5;

        if (this.tickCount % interval != 0) return;

        // quantidade de cura
        float healAmount = 0.5f + (ragePercent * 2.5f);
        // 0 rage → 0.5
        // full rage → 3.0

        this.heal(healAmount);

    }

    // =============================
    // 🎨 COR ROXA (RENDER HOOK)
    // =============================

    public float getRageTint() {
        return rage.getPercent(); // 0 → 1
    }

    // =============================
    // 🔗 GETTER
    // =============================

    @Override
    public RageComponent getRage() {
        return rage;
    }

    private void spawnRageAura(float intensity) {

        if (!(this.level() instanceof ServerLevel server)) return;

        if (this.tickCount % 4 != 0) return; // controle de spam

        int count = (int) (2 + intensity * 8);

        for (int i = 0; i < count; i++) {

            double offsetX = (random.nextDouble() - 0.5) * 1.2;
            double offsetY = random.nextDouble() * 1.5;
            double offsetZ = (random.nextDouble() - 0.5) * 1.2;

            server.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.PORTAL,
                    this.getX() + offsetX,
                    this.getY() + offsetY,
                    this.getZ() + offsetZ,
                    1,
                    0, 0, 0,
                    0.1 + intensity * 0.2
            );
        }
    }
}