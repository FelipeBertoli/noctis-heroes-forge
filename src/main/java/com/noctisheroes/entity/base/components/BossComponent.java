package com.noctisheroes.entity.base.components;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import java.util.HashSet;
import java.util.Set;

/**
 * Componente para gerenciar lógica de boss.
 *
 * ✅ Melhorias:
 * - Encapsulamento total da lógica de boss
 * - Separação clara do comportamento da entidade
 * - Padrão Composition over Inheritance
 * - Gerenciamento eficiente de players
 */
public class BossComponent {

    private final Mob parentEntity;
    private final Set<ServerPlayer> trackedPlayers = new HashSet<>();
    private final BossEventListener eventListener;

    public BossComponent(Mob parentEntity) {
        this.parentEntity = parentEntity;
        this.eventListener = new BossEventListener(parentEntity);
    }

    /**
     * Registra um player que está vendo o boss.
     */
    public void addPlayer(ServerPlayer player) {
        this.trackedPlayers.add(player);
        this.eventListener.playerStartsTracking(player);
    }

    /**
     * Remove um player do rastreamento do boss.
     */
    public void removePlayer(ServerPlayer player) {
        this.trackedPlayers.remove(player);
        this.eventListener.playerStopsTracking(player);
    }

    /**
     * Chamado a cada tick. Gerencia estado do boss.
     */
    public void tick() {
        if (this.parentEntity.level().isClientSide) return;

        // Lógica de boss por tick (saúde, eventos especiais, etc)
        this.eventListener.tick(this.parentEntity);
    }

    /**
     * Retorna os players rastreando este boss.
     */
    public Set<ServerPlayer> getTrackedPlayers() {
        return new HashSet<>(this.trackedPlayers);
    }

    /**
     * Limpa todos os players rastreados.
     */
    public void cleanup() {
        this.trackedPlayers.forEach(this.eventListener::playerStopsTracking);
        this.trackedPlayers.clear();
    }

    /**
     * Retorna se há players rastreando este boss.
     */
    public boolean hasTrackedPlayers() {
        return !this.trackedPlayers.isEmpty();
    }

    /**
     * Classe interna para gerenciar eventos de boss.
     */
    private static class BossEventListener {
        private final Mob boss;

        BossEventListener(Mob boss) {
            this.boss = boss;
        }

        void playerStartsTracking(ServerPlayer player) {
            // Aqui você pode adicionar lógica de boss bar, sons, etc.
        }

        void playerStopsTracking(ServerPlayer player) {
            // Lógica de cleanup
        }

        void tick(Mob boss) {
            // Lógica de tick do boss
        }
    }

}