package com.noctisheroes.entity.entities.base;

import com.noctisheroes.common.attribute.AttributeConfig;
import com.noctisheroes.common.config.EntityConfig;
import com.noctisheroes.entity.components.BossComponent;
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
public abstract class ViltrumiteBoss extends AbstractViltrumite{

    private final BossComponent boss;
    private static final float randomFlightToggleChance = 0.001f;
    private static final float combatFlightToggleChance = 0.02f;


    // =============================
    // 🏗️ CONSTRUTOR
    // =============================

    protected ViltrumiteBoss(EntityType<? extends Monster> type, Level level,
                             String tag, AttributeConfig attribute, EntityConfig config) {
        super(type, level, tag, attribute, config);
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

    @Override
    public float getRandomFlightToggleChance() {
        return randomFlightToggleChance;
    }

    @Override
    public float getCombatFlightChance() {
        return this.combatFlightToggleChance;
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