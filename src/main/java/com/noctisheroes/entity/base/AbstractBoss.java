package com.noctisheroes.entity.base;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * Classe base para todos os Bosses.
 * Gerencia barra de vida e fases baseadas em porcentagem de HP.
 *
 * Subclasses devem implementar:
 *  - getPhaseThresholds() — array de porcentagens de HP que trocam de fase (ex: {0.7f, 0.4f})
 *  - onPhaseChange(phase) — chamado quando a fase muda
 *  - getBossBarColor()    — cor da barra de boss
 *  - getBossBarName()     — nome exibido na barra
 */
public abstract class AbstractBoss extends AbstractViltrumite {

    private final ServerBossEvent bossBar;
    private int currentPhase = 0;

    protected AbstractBoss(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        bossBar = new ServerBossEvent(
                Component.translatable(getBossBarName()),
                getBossBarColor(),
                BossEvent.BossBarOverlay.PROGRESS
        );
    }

    // ── Configuração — sobrescreva nas subclasses ─────────────────────────────

    /** Ex: new float[]{0.7f, 0.4f} = fase 1 em 70% HP, fase 2 em 40% HP */
    protected abstract float[] getPhaseThresholds();

    /** Chamado quando o boss entra em uma nova fase. phase começa em 1. */
    protected abstract void onPhaseChange(int phase);

    protected abstract BossEvent.BossBarColor getBossBarColor();

    protected abstract String getBossBarName();

    // ── Fase atual ────────────────────────────────────────────────────────────

    public int getCurrentPhase() {
        return currentPhase;
    }

    // ── Tick ──────────────────────────────────────────────────────────────────

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide) {
            updateBossBar();
            checkPhaseTransition();
        }
    }

    private void updateBossBar() {
        bossBar.setProgress(getHealth() / getMaxHealth());
    }

    private void checkPhaseTransition() {
        float[] thresholds = getPhaseThresholds();
        float hpPercent = getHealth() / getMaxHealth();

        for (int i = currentPhase; i < thresholds.length; i++) {
            if (hpPercent <= thresholds[i]) {
                currentPhase = i + 1;
                onPhaseChange(currentPhase);
                break;
            }
        }
    }

    // ── Barra de boss — players próximos ──────────────────────────────────────

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossBar.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossBar.removePlayer(player);
    }

    // ── NBT ──────────────────────────────────────────────────────────────────

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("BossPhase", currentPhase);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        currentPhase = tag.getInt("BossPhase");
    }
}