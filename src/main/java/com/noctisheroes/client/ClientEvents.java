package com.noctisheroes.client;

import com.noctisheroes.entity.entities.drakari.DrakariSoldier;
import com.noctisheroes.entity.entities.viltrumite.LucanBoss;
import com.noctisheroes.entity.entities.viltrumite.ThraggBoss;
import com.noctisheroes.entity.entities.viltrumite.ViltrumiteEntity;
import com.noctisheroes.registry.EntitiesRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {

    // =============================
    // 🎨 RENDERERS
    // =============================

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {

        event.registerEntityRenderer(EntitiesRegistry.VILTRUMITE.get(),
                ctx -> new NoctisEntityRenderer<>(ctx, "humanoid", "viltrumite", "viltrumite", 6));

        event.registerEntityRenderer(EntitiesRegistry.THRAGG_BOSS.get(),
                ctx -> new NoctisEntityRenderer<>(ctx, "viltrumite", "thragg_boss", "viltrumite", 1));

        event.registerEntityRenderer(EntitiesRegistry.LUCAN_BOSS.get(),
                ctx -> new NoctisEntityRenderer<>(ctx, "viltrumite", "lucan_boss", "viltrumite", 1));

        event.registerEntityRenderer(EntitiesRegistry.DRAKARI_SOLDIER.get(),
                ctx -> new NoctisEntityRenderer<>(ctx, "humanoid", "drakari", "drakari", 2));
    }

    // =============================
    // 📊 ATTRIBUTES
    // =============================

    @SubscribeEvent
    public static void onAttributeCreate(EntityAttributeCreationEvent event) {

        event.put(EntitiesRegistry.VILTRUMITE.get(), ViltrumiteEntity.createAttributes());
        event.put(EntitiesRegistry.THRAGG_BOSS.get(), ThraggBoss.createAttributes());
        event.put(EntitiesRegistry.LUCAN_BOSS.get(), LucanBoss.createAttributes());
        event.put(EntitiesRegistry.DRAKARI_SOLDIER.get(), DrakariSoldier.createAttributes());
    }
}