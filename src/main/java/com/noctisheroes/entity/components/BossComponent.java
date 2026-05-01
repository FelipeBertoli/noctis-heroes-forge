package com.noctisheroes.entity.components;

import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Mob;
import net.minecraft.network.chat.Component;
import java.util.HashSet;
import java.util.Set;

/**
 * Componente para gerenciar lógica de boss, incluindo barra de vida.
 *
 * ✅ Melhorias:
 * - Encapsulamento total da lógica de boss
 * - Separação clara do comportamento da entidade
 * - Padrão Composition over Inheritance
 * - Gerenciamento eficiente de players
 * - Barra de vida (Boss Bar) sincronizada
 * - Cores e estilos customizáveis
 */
public class BossComponent {

    private final Mob parentEntity;
    private final Set<ServerPlayer> trackedPlayers = new HashSet<>();
    private final ServerBossEvent bossBar;
    private final BossEventListener eventListener;

    // =============================
    // ⚙️ CONFIGURAÇÃO
    // =============================

    private static final BossEvent.BossBarColor DEFAULT_COLOR = BossEvent.BossBarColor.PURPLE;
    private static final BossEvent.BossBarOverlay DEFAULT_STYLE = BossEvent.BossBarOverlay.PROGRESS;

    // =============================
    // 🏗️ CONSTRUTOR
    // =============================

    public BossComponent(Mob parentEntity) {
        this(parentEntity, DEFAULT_COLOR, DEFAULT_STYLE);

    }

    /**
     * Construtor com cores e estilo customizáveis.
     */
    public BossComponent(Mob parentEntity, BossEvent.BossBarColor color, BossEvent.BossBarOverlay style) {
        this.parentEntity = parentEntity;
        this.eventListener = new BossEventListener(parentEntity);

        // Cria a barra de vida
        this.bossBar = new ServerBossEvent(
                Component.literal(parentEntity.getName().getString()),
                color,
                style
        );

        // Sincroniza saúde com a barra
        this.bossBar.setProgress(parentEntity.getHealth() / parentEntity.getMaxHealth());
    }

    // =============================
    // 📍 RASTREAMENTO DE PLAYERS
    // =============================

    /**
     * Registra um player que está vendo o boss.
     */
    public void addPlayer(ServerPlayer player) {
        this.trackedPlayers.add(player);
        this.bossBar.addPlayer(player);
        this.eventListener.playerStartsTracking(player);
    }

    /**
     * Remove um player do rastreamento do boss.
     */
    public void removePlayer(ServerPlayer player) {
        this.trackedPlayers.remove(player);
        this.bossBar.removePlayer(player);
        this.eventListener.playerStopsTracking(player);
    }

    // =============================
    // 🔄 TICK
    // =============================

    /**
     * Chamado a cada tick. Gerencia estado do boss.
     */
    public void tick() {
        if (this.parentEntity.level().isClientSide) return;

        // Atualiza barra de vida
        updateBossBar();

        // Lógica de boss por tick
        this.eventListener.tick(this.parentEntity);
    }

    /**
     * Atualiza a barra de vida do boss.
     */
    private void updateBossBar() {
        float healthPercentage = this.parentEntity.getHealth() / this.parentEntity.getMaxHealth();
        this.bossBar.setProgress(Math.max(0.0f, Math.min(1.0f, healthPercentage)));
    }

    // =============================
    // 🎨 CUSTOMIZAÇÃO
    // =============================

    /**
     * Define a cor da barra de vida.
     */
    public void setBossBarColor(BossEvent.BossBarColor color) {
        this.bossBar.setColor(color);
    }

    /**
     * Define o estilo da barra de vida.
     */
    public void setBossBarStyle(BossEvent.BossBarOverlay style) {
        this.bossBar.setOverlay(style);
    }

    /**
     * Define o nome da barra de vida.
     */
    public void setBossBarName(Component name) {
        this.bossBar.setName(name);
    }

    /**
     * Mostra ou esconde a barra de vida.
     */
    public void setBossBarVisible(boolean visible) {
        // ServerBossEvent não tem método direto, mas pode-se remover todos os players
        if (!visible && !this.trackedPlayers.isEmpty()) {
            this.trackedPlayers.forEach(this.bossBar::removePlayer);
        } else if (visible && !this.trackedPlayers.isEmpty()) {
            this.trackedPlayers.forEach(this.bossBar::addPlayer);
        }
    }

    // =============================
    // 📊 GETTERS
    // =============================

    /**
     * Retorna a barra de vida (para manipulação avançada).
     */
    public ServerBossEvent getBossBar() {
        return this.bossBar;
    }

    /**
     * Retorna os players rastreando este boss.
     */
    public Set<ServerPlayer> getTrackedPlayers() {
        return new HashSet<>(this.trackedPlayers);
    }

    /**
     * Retorna se há players rastreando este boss.
     */
    public boolean hasTrackedPlayers() {
        return !this.trackedPlayers.isEmpty();
    }

    /**
     * Retorna a entidade parent do boss.
     */
    public Mob getParentEntity() {
        return this.parentEntity;
    }

    // =============================
    // 🧹 CLEANUP
    // =============================

    /**
     * Limpa todos os players rastreados e remove a barra de vida.
     */
    public void cleanup() {
        this.trackedPlayers.forEach(player -> {
            this.bossBar.removePlayer(player);
            this.eventListener.playerStopsTracking(player);
        });
        this.trackedPlayers.clear();
    }

    // =============================
    // 📝 LISTENER INTERNO
    // =============================

    /**
     * Classe interna para gerenciar eventos de boss.
     */
    private static class BossEventListener {
        private final Mob boss;

        BossEventListener(Mob boss) {
            this.boss = boss;
        }

        /**
         * Chamado quando um player começa a rastrear o boss.
         */
        void playerStartsTracking(ServerPlayer player) {
            // Você pode adicionar lógica aqui
            // Exemplos: sons especiais, efeitos, mensagens, etc.
        }

        /**
         * Chamado quando um player para de rastrear o boss.
         */
        void playerStopsTracking(ServerPlayer player) {
            // Você pode adicionar lógica aqui
            // Exemplos: remover efeitos, salvar dados, etc.
        }

        /**
         * Chamado a cada tick enquanto o boss está vivo.
         */
        void tick(Mob boss) {
            // Você pode adicionar lógica aqui
            // Exemplos: fases do boss, ataques especiais, etc.
        }
    }
}