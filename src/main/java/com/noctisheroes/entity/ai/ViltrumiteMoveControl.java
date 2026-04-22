package com.noctisheroes.entity.ai;

import com.noctisheroes.entity.ViltrumiteEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.Vec3;

public class ViltrumiteMoveControl extends MoveControl {

    private final ViltrumiteEntity viltrumite;

    public ViltrumiteMoveControl(ViltrumiteEntity entity) {
        super(entity);
        this.viltrumite = entity;
    }

    @Override
    public void tick() {
        if (this.operation != Operation.MOVE_TO) {
            // Sem destino — desacelera gradualmente (não trava de repente)
            Vec3 current = viltrumite.getDeltaMovement();
            viltrumite.setDeltaMovement(current.scale(0.85D));
            return;
        }

        // ── Calcula direção até o destino ────────────────────────────────────
        double dx = this.wantedX - viltrumite.getX();
        double dy = this.wantedY - viltrumite.getY();
        double dz = this.wantedZ - viltrumite.getZ();
        double distH = Math.sqrt(dx * dx + dz * dz); // distância horizontal

        // ── Velocidade atual do atributo (controlada pelos estados) ──────────
        double speed = viltrumite.getAttributeValue(
                net.minecraft.world.entity.ai.attributes.Attributes.FLYING_SPEED
        );

        // Acelera em direção ao destino com interpolação suave
        Vec3 current = viltrumite.getDeltaMovement();

        double targetVX = (dx / distH) * speed * 3.5D;
        double targetVZ = (dz / distH) * speed * 3.5D;
        double targetVY;

        // Controle vertical — sobe/desce suavemente, sem saltos
        if (Math.abs(dy) > 0.5D) {
            targetVY = Math.signum(dy) * speed * 2.0D;
        } else {
            // Perto da altura certa — estabiliza no eixo Y
            targetVY = -current.y * 0.3D;
        }

        // Lerp (interpolação linear) para suavizar aceleração — evita teleporte
        double lerpFactor = 0.25D;
        double newVX = Mth.lerp(lerpFactor, current.x, targetVX);
        double newVY = Mth.lerp(lerpFactor, current.y, targetVY);
        double newVZ = Mth.lerp(lerpFactor, current.z, targetVZ);

        viltrumite.setDeltaMovement(newVX, newVY, newVZ);

        // ── Rotação — vira em direção ao movimento ───────────────────────────
        if (distH > 0.1D) {
            float targetYaw = (float)(Mth.atan2(dz, dx) * (180D / Math.PI)) - 90F;
            viltrumite.setYRot(rotateTowards(viltrumite.getYRot(), targetYaw, 20F));
            viltrumite.yBodyRot = viltrumite.getYRot();
        }

        // ── Se chegou perto do destino, limpa o operation ────────────────────
        if (distH < 0.5D && Math.abs(dy) < 0.5D) {
            this.operation = Operation.WAIT;
        }
    }

    /** Rotaciona `current` em direção a `target` no máximo `maxStep` graus. */
    private float rotateTowards(float current, float target, float maxStep) {
        float diff = Mth.wrapDegrees(target - current);
        return current + Mth.clamp(diff, -maxStep, maxStep);
    }
}