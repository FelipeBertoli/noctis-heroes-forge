package com.noctisheroes.events;

import com.noctisheroes.entity.boss.viltrumite.LucanBoss;
import com.noctisheroes.entity.boss.viltrumite.ThraggBoss;
import com.noctisheroes.entity.mobs.ViltrumiteEntity;
import com.noctisheroes.registry.ModEntities;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonEvents {

    @SubscribeEvent
    public static void onAttributeCreate(EntityAttributeCreationEvent event) {
        event.put(ModEntities.VILTRUMITE.get(), ViltrumiteEntity.setAttributes());
        event.put(ModEntities.THRAGG_BOSS.get(), ThraggBoss.setAttributes());
        event.put(ModEntities.LUCAN_BOSS.get(), LucanBoss.setAttributes());
    }
}