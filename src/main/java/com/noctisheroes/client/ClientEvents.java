package com.noctisheroes.client;

import com.noctisheroes.client.renderer.viltrumite.LucanBossRenderer;
import com.noctisheroes.client.renderer.viltrumite.ThraggBossRenderer;
import com.noctisheroes.client.renderer.viltrumite.ViltrumiteRenderer;
import com.noctisheroes.entity.mobs.viltrumite.LucanBoss;
import com.noctisheroes.entity.mobs.viltrumite.ThraggBoss;
import com.noctisheroes.entity.mobs.viltrumite.ViltrumiteEntity;
import com.noctisheroes.registry.ModEntities;
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
        event.registerEntityRenderer(ModEntities.VILTRUMITE.get(), ViltrumiteRenderer::new);
        event.registerEntityRenderer(ModEntities.THRAGG_BOSS.get(), ThraggBossRenderer::new);
        event.registerEntityRenderer(ModEntities.LUCAN_BOSS.get(), LucanBossRenderer::new);
        // Adicione novos mobs aqui — uma linha por mob
    }

    @SubscribeEvent
    public static void onAttributeCreate(EntityAttributeCreationEvent event) {
        event.put(ModEntities.VILTRUMITE.get(), ViltrumiteEntity.setAttributes());
        event.put(ModEntities.THRAGG_BOSS.get(), ThraggBoss.setAttributes());
        event.put(ModEntities.LUCAN_BOSS.get(), LucanBoss.setAttributes());
    }
}