package com.noctisheroes.entity.ai;

import com.noctisheroes.entity.base.AbstractViltrumite;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.Vec3;

public class ViltrumiteMoveControl extends MoveControl {

    private final AbstractViltrumite viltrumite;

    public ViltrumiteMoveControl(AbstractViltrumite entity) {
        super(entity);
        this.viltrumite = entity;
    }

    @Override
    public void tick() {

        // ── Sem movimento → desaceleração suave ─────────────────────────────
        if (this.operation != Operation.MOVE_TO) {
            Vec3 current = viltrumite.getDeltaMovement();
            viltrumite.setDeltaMovement(current.scale(0.85D));
            return;
        }

        // ── Direção até o destino ───────────────────────────────────────────
        double dx = this.wantedX - viltrumite.getX();
        double dy = this.wantedY - viltrumite.getY();
        double dz = this.wantedZ - viltrumite.getZ();

        double distH = Math.sqrt(dx * dx + dz * dz);
        if (distH < 1e-4) distH = 1e-4;

        // ── Velocidade base ─────────────────────────────────────────────────
        double speed = viltrumite.getAttributeValue(
                net.minecraft.world.entity.ai.attributes.Attributes.FLYING_SPEED
        );

        Vec3 current = viltrumite.getDeltaMovement();

        double targetVX = (dx / distH) * speed * 3.5D;
        double targetVZ = (dz / distH) * speed * 3.5D;

        double targetVY;
        if (Math.abs(dy) > 0.5D) {
            targetVY = Math.signum(dy) * speed * 2.0D;
        } else {
            targetVY = -current.y * 0.3D;
        }

        // ── Suavização ───────────────────────────────────────────────────────
        double lerp = 0.25D;

        double newVX = Mth.lerp(lerp, current.x, targetVX);
        double newVY = Mth.lerp(lerp, current.y, targetVY);
        double newVZ = Mth.lerp(lerp, current.z, targetVZ);

        viltrumite.setDeltaMovement(newVX, newVY, newVZ);

        // ── ROTAÇÃO INTELIGENTE ──────────────────────────────────────────────
        applyRotation();

        // ── Chegada ao destino ──────────────────────────────────────────────
        if (distH < 0.5D && Math.abs(dy) < 0.5D) {
            this.operation = Operation.WAIT;
        }
    }

    private void applyRotation() {

        // 🔥 PRIORIDADE 1: olhar para o alvo
        if (viltrumite.getTarget() != null && viltrumite.getTarget().isAlive()) {

            double dx = viltrumite.getTarget().getX() - viltrumite.getX();
            double dz = viltrumite.getTarget().getZ() - viltrumite.getZ();

            float targetYaw = (float)(Mth.atan2(dz, dx) * (180D / Math.PI)) - 90F;

            float newYaw = rotateTowards(
                    viltrumite.getYRot(),
                    targetYaw,
                    30F
            );

            viltrumite.setYRot(newYaw);
            viltrumite.yBodyRot = newYaw;
            return;
        }

        // 🔄 PRIORIDADE 2: olhar para direção do movimento
        Vec3 movement = viltrumite.getDeltaMovement();

        if (movement.lengthSqr() > 0.001D) {

            float targetYaw = (float)(Mth.atan2(movement.z, movement.x) * (180D / Math.PI)) - 90F;

            float maxTurn = movement.length() > 0.2D ? 20F : 8F;

            float newYaw = rotateTowards(
                    viltrumite.getYRot(),
                    targetYaw,
                    maxTurn
            );

            viltrumite.setYRot(newYaw);
            viltrumite.yBodyRot = newYaw;
        }
    }

    // ── Rotação suave limitada ───────────────────────────────────────────────
    private float rotateTowards(float current, float target, float maxStep) {
        float diff = Mth.wrapDegrees(target - current);
        return current + Mth.clamp(diff, -maxStep, maxStep);
    }
}