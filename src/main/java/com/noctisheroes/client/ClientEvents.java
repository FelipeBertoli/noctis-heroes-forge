package com.noctisheroes.client;

import com.noctisheroes.client.renderer.viltrumite.LucanBossRenderer;
import com.noctisheroes.client.renderer.viltrumite.ThraggBossRenderer;
import com.noctisheroes.client.renderer.viltrumite.ViltrumiteRenderer;
import com.noctisheroes.entity.entities.viltrumite.mob.LucanBoss;
import com.noctisheroes.entity.entities.viltrumite.mob.ThraggBoss;
import com.noctisheroes.entity.entities.viltrumite.mob.ViltrumiteEntity;
import com.noctisheroes.registry.EntitiesRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Com Geckolib não há mais registerLayerDefinitions.
 * Só registra o renderer — modelo e animações vêm dos JSONs.
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntitiesRegistry.VILTRUMITE.get(), ViltrumiteRenderer::new);
        event.registerEntityRenderer(EntitiesRegistry.THRAGG_BOSS.get(), ThraggBossRenderer::new);
        event.registerEntityRenderer(EntitiesRegistry.LUCAN_BOSS.get(), LucanBossRenderer::new);
        // Adicione novos mobs aqui — uma linha por mob
    }

    @SubscribeEvent
    public static void onAttributeCreate(EntityAttributeCreationEvent event) {
        event.put(EntitiesRegistry.VILTRUMITE.get(), ViltrumiteEntity.setAttributes());
        event.put(EntitiesRegistry.THRAGG_BOSS.get(), ThraggBoss.setAttributes());
        event.put(EntitiesRegistry.LUCAN_BOSS.get(), LucanBoss.setAttributes());
    }
}