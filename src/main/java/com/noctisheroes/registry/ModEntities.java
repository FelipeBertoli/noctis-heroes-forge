package com.noctisheroes.registry;

import com.noctisheroes.NoctisHeroes;
import com.noctisheroes.entity.ViltrumiteEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, NoctisHeroes.MODID);

    public static final RegistryObject<EntityType<ViltrumiteEntity>> VILTRUMITE =
            ENTITIES.register("viltrumite",
                    () -> EntityType.Builder.of(ViltrumiteEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.8F)
                            .build(new ResourceLocation(NoctisHeroes.MODID, "viltrumite").toString()));

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
    }
}