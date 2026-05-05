package com.noctisheroes.entity.components;

import com.noctisheroes.entity.NoctisEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public class RageComponent {

    private float value = 0;
    private final float max = 100;

    private final NoctisEntity entity;

    public RageComponent(NoctisEntity entity) {
        this.entity = entity;
    }

    public void tick() {

        // ganho passivo
        add(0.03f);

        // 🔥 AURA VISUAL
        if (!entity.level().isClientSide && entity.level() instanceof ServerLevel level) {

            float percent = getPercent();

            if (percent > 0.1f) {
                spawnAura(level, percent);
            }
        }
    }

    private void spawnAura(ServerLevel level, float intensity) {

        int count = (int) (2 + intensity * 8);

        for (int i = 0; i < count; i++) {

            double angle = Math.random() * Math.PI * 2;
            double radius = 0.5 + intensity * 1.5;

            double x = Math.cos(angle) * radius;
            double z = Math.sin(angle) * radius;
            double y = Math.random() * 1.5;

            level.sendParticles(
                    net.minecraft.core.particles.ParticleTypes.PORTAL, // roxo estilo end
                    entity.getX() + x,
                    entity.getY() + y,
                    entity.getZ() + z,
                    1,
                    0, 0.05, 0,
                    0
            );
        }
    }

    public void add(float amount) {
        value = Math.min(max, value + amount);
    }

    public void consume(float amount) {
        value = Math.max(0, value - amount);
    }

    public void addFromDamage(float damage) {
        add(damage * 0.6f);
    }

    public float getValue() {
        return value;
    }

    public float getPercent() {
        return value / max;
    }
}