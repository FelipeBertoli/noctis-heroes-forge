package com.noctisheroes.entity.base.viltrumite;

import com.noctisheroes.entity.base.components.BossComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/**
 * Base para todos os bosses Viltrumita.
 * Encapsula lógica de boss e gerencia componentes.
 *
 * ✅ Melhorias:
 * - Encapsulamento completo de lógica de boss
 * - Separation of Concerns
 * - Fácil criação de novos bosses
 */
public abstract class ViltrumiteBoss extends ViltrumiteVariant {

    private final BossComponent boss;

    // =============================
    // 🏗️ CONSTRUTOR
    // =============================

    protected ViltrumiteBoss(EntityType<? extends Monster> type, Level level,
                             String tag, ViltrumiteConfig config) {
        super(type, level, tag, config);
        this.boss = new BossComponent(this);
    }

    // =============================
    // 🎮 LIFECYCLE
    // =============================

    @Override
    public void tick() {
        super.tick();
        this.boss.tick();
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.boss.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.boss.removePlayer(player);
    }

    // =============================
    // 🛡️ GETTERS PARA SUBCLASSES
    // =============================

    /**
     * Retorna o componente de boss para customização em subclasses.
     */
    protected final BossComponent getBossComponent() {
        return this.boss;
    }
}