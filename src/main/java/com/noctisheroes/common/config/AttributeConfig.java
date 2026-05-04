package com.noctisheroes.common.config;

public class AttributeConfig {

    public double maxHealth;
    public double movementSpeed;
    public double flyingSpeed;
    public double attackDamage;
    public double attackKnockback;
    public double attackSpeed;
    public double followRange;
    public double armor;
    public double armorToughness;
    public double knockbackResistance;

    // 🔥 Combat (Damage System)
    public float fireResistance;
    public float fireWeakness;
    public float explosionResistance;
    public float explosionWeakness;
    public float projectileResistance;
    public float projectileWeakness;

    // =============================
    // 🌍 DEFAULT GLOBAL
    // =============================

    public static final AttributeConfig DEFAULT =
            new Builder()
                    .maxHealth(100)
                    .movementSpeed(0.3)
                    .flyingSpeed(0.6)
                    .attackDamage(5)
                    .attackKnockback(0.5)
                    .attackSpeed(0.3)
                    .followRange(20)
                    .armor(0)
                    .armorToughness(0)
                    .knockbackResistance(0)

                    .fireResistance(0)
                    .fireWeakness(0)
                    .explosionResistance(0)
                    .explosionWeakness(0)
                    .projectileResistance(0)
                    .projectileWeakness(0)

                    .build();

    // =============================
    // 🏗️ BUILDER
    // =============================

    public static class Builder {

        private double maxHealth = 100;
        private double movementSpeed = 0.3;
        private double flyingSpeed = 0.6;
        private double attackDamage = 5;
        private double attackKnockback = 0.5;
        private double attackSpeed = 0.3;
        private double followRange = 20;
        private double armor = 0;
        private double armorToughness = 0;
        private double knockbackResistance = 0;

        // 🔥 Combat defaults
        private float fireResistance = 0;
        private float fireWeakness = 0;
        private float explosionResistance = 0;
        private float explosionWeakness = 0;
        private float projectileResistance = 0;
        private float projectileWeakness = 0;

        public Builder() {}

        // =============================
        // ⚙️ ATTRIBUTES
        // =============================

        public Builder maxHealth(double v) { this.maxHealth = v; return this; }
        public Builder movementSpeed(double v) { this.movementSpeed = v; return this; }
        public Builder flyingSpeed(double v) { this.flyingSpeed = v; return this; }
        public Builder attackDamage(double v) { this.attackDamage = v; return this; }
        public Builder attackKnockback(double v) { this.attackKnockback = v; return this; }
        public Builder attackSpeed(double v) { this.attackSpeed = v; return this; }
        public Builder followRange(double v) { this.followRange = v; return this; }
        public Builder armor(double v) { this.armor = v; return this; }
        public Builder armorToughness(double v) { this.armorToughness = v; return this; }
        public Builder knockbackResistance(double v) { this.knockbackResistance = v; return this; }

        // =============================
        // 🔥 COMBAT CONFIG
        // =============================

        public Builder fireResistance(float v) { this.fireResistance = v; return this; }
        public Builder fireWeakness(float v) { this.fireWeakness = v; return this; }

        public Builder explosionResistance(float v) { this.explosionResistance = v; return this; }
        public Builder explosionWeakness(float v) { this.explosionWeakness = v; return this; }

        public Builder projectileResistance(float v) { this.projectileResistance = v; return this; }
        public Builder projectileWeakness(float v) { this.projectileWeakness = v; return this; }

        // =============================
        // 🧱 BUILD
        // =============================

        public AttributeConfig build() {
            AttributeConfig config = new AttributeConfig();

            config.maxHealth = this.maxHealth;
            config.movementSpeed = this.movementSpeed;
            config.flyingSpeed = this.flyingSpeed;
            config.attackDamage = this.attackDamage;
            config.attackKnockback = this.attackKnockback;
            config.attackSpeed = this.attackSpeed;
            config.followRange = this.followRange;
            config.armor = this.armor;
            config.armorToughness = this.armorToughness;
            config.knockbackResistance = this.knockbackResistance;

            config.fireResistance = this.fireResistance;
            config.fireWeakness = this.fireWeakness;
            config.explosionResistance = this.explosionResistance;
            config.explosionWeakness = this.explosionWeakness;
            config.projectileResistance = this.projectileResistance;
            config.projectileWeakness = this.projectileWeakness;

            return config;
        }
    }
}