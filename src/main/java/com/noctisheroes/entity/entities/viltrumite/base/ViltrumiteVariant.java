package com.noctisheroes.entity.entities.viltrumite.base;

import com.noctisheroes.common.tags.DamageTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Classe base para todas as variações de Viltrumita.
 * Implementa padrão builder para configuração de atributos.
 */
public abstract class ViltrumiteVariant extends AbstractViltrumite {

    private static final int DEFAULT_SKIN_COUNT = 1;
    private final ViltrumiteConfig config;

    // =============================
    // 🏗️ CONSTRUTOR
    // =============================

    protected ViltrumiteVariant(EntityType<? extends Monster> type, Level level,
                                String tag, ViltrumiteConfig config) {
        super(type, level, tag);
        this.config = config;
        this.xpReward = config.xpReward;
        this.getDamageProfile()
                .ignoreFallDamage()
                .immuneTo(DamageTags.VILTRUMITE)
                .resist(DamageTags.FIRE, 0.5f)
                .resist(DamageTags.EXPLOSION, 0.5f)
                .weakTo(DamageTags.SONIC, 1.0f)
                .immuneTo("viltrumite");
    }

    // =============================
    // ⚙️ CONFIGURAÇÃO
    // =============================

    @Override
    protected int getSkinCount() {
        return this.config.skinCount;
    }

    @Override
    protected float getRandomFlightToggleChance() {
        return this.config.randomFlightToggleChance;
    }

    @Override
    protected float getCombatFlightChance() {
        return this.config.combatFlightChance;
    }

    /**
     * Retorna a configuração deste Viltrumita.
     */
    protected final ViltrumiteConfig getConfig() {
        return this.config;
    }

    // =============================
    // 📊 CONFIGURAÇÃO DE ATRIBUTOS
    // =============================

    /**
     * Cria um supplier de atributos baseado na configuração.
     * Use esta factory em suas classes concretas.
     */
    protected static AttributeSupplier createAttributeSupplier(ViltrumiteConfig config) {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, config.maxHealth)
                .add(Attributes.MOVEMENT_SPEED, config.movementSpeed)
                .add(Attributes.FLYING_SPEED, config.flyingSpeed)
                .add(Attributes.ATTACK_DAMAGE, config.attackDamage)
                .add(Attributes.ATTACK_KNOCKBACK, config.attackKnockback)
                .add(Attributes.ATTACK_SPEED, config.attackSpeed)
                .add(Attributes.FOLLOW_RANGE, config.followRange)
                .add(Attributes.ARMOR, config.armor)
                .add(Attributes.ARMOR_TOUGHNESS, config.armorToughness)
                .add(Attributes.KNOCKBACK_RESISTANCE, config.knockbackResistance)
                .build();
    }

    // =============================
    // 🎯 CONFIGURAÇÃO DE VILTRUMITA
    // =============================

    /**
     * Configuração imutável para uma variante de Viltrumita.
     */
    public static class ViltrumiteConfig {
        // Dados de jogo
        public final int xpReward;
        public final int skinCount;

        // Atributos
        public final double maxHealth;
        public final double movementSpeed;
        public final double flyingSpeed;
        public final double attackDamage;
        public final double attackKnockback;
        public final double attackSpeed;
        public final double followRange;
        public final double armor;
        public final double armorToughness;
        public final double knockbackResistance;

        // Comportamento
        public final float randomFlightToggleChance;
        public final float combatFlightChance;

        private ViltrumiteConfig(Builder builder) {
            this.xpReward = builder.xpReward;
            this.skinCount = builder.skinCount;

            this.maxHealth = builder.maxHealth;
            this.movementSpeed = builder.movementSpeed;
            this.flyingSpeed = builder.flyingSpeed;
            this.attackDamage = builder.attackDamage;
            this.attackKnockback = builder.attackKnockback;
            this.attackSpeed = builder.attackSpeed;
            this.followRange = builder.followRange;
            this.armor = builder.armor;
            this.armorToughness = builder.armorToughness;
            this.knockbackResistance = builder.knockbackResistance;

            this.randomFlightToggleChance = builder.randomFlightToggleChance;
            this.combatFlightChance = builder.combatFlightChance;
        }

        // =============================
        // 🏗️ BUILDER PATTERN
        // =============================

        public static class Builder {
            // Defaults
            public int xpReward = 0;
            public int skinCount = DEFAULT_SKIN_COUNT;

            public double maxHealth = 100.0;
            public double movementSpeed = 0.3;
            public double flyingSpeed = 0.6;
            public double attackDamage = 5.0;
            public double attackKnockback = 0.5;
            public double attackSpeed = 0.3;
            public double followRange = 20.0;
            public double armor = 0.0;
            public double armorToughness = 0.0;
            public double knockbackResistance = 0.0;

            public float randomFlightToggleChance = 0.002f;
            public float combatFlightChance = 0.01f;

            public Builder xpReward(int xp) { this.xpReward = xp; return this; }
            public Builder skinCount(int count) { this.skinCount = count; return this; }

            public Builder maxHealth(double hp) { this.maxHealth = hp; return this; }
            public Builder movementSpeed(double speed) { this.movementSpeed = speed; return this; }
            public Builder flyingSpeed(double speed) { this.flyingSpeed = speed; return this; }
            public Builder attackDamage(double damage) { this.attackDamage = damage; return this; }
            public Builder attackKnockback(double kb) { this.attackKnockback = kb; return this; }
            public Builder attackSpeed(double speed) { this.attackSpeed = speed; return this; }
            public Builder followRange(double range) { this.followRange = range; return this; }
            public Builder armor(double armor) { this.armor = armor; return this; }
            public Builder armorToughness(double tough) { this.armorToughness = tough; return this; }
            public Builder knockbackResistance(double resist) { this.knockbackResistance = resist; return this; }

            public Builder randomFlightToggleChance(float chance) {
                this.randomFlightToggleChance = chance;
                return this;
            }
            public Builder combatFlightChance(float chance) {
                this.combatFlightChance = chance;
                return this;
            }

            public ViltrumiteConfig build() {
                return new ViltrumiteConfig(this);
            }
        }
    }
}