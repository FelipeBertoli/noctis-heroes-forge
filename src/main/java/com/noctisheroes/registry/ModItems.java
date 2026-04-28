package com.noctisheroes.registry;

import com.noctisheroes.NoctisHeroes;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<net.minecraft.world.item.Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, NoctisHeroes.MODID);

    public static final RegistryObject<ForgeSpawnEggItem> VILTRUMITE_SPAWN_EGG =
            ITEMS.register("viltrumite_spawn_egg", () ->
                    new ForgeSpawnEggItem(
                            ModEntities.VILTRUMITE,
                            0x1a1a2e,  // azul escuro
                            0xc0392b,  // vermelho
                            new net.minecraft.world.item.Item.Properties()
                    )
            );

    public static final RegistryObject<ForgeSpawnEggItem> THRAGG_SPAWN_EGG =
            ITEMS.register("thragg_boss_spawn_egg", () ->
                    new ForgeSpawnEggItem(
                            ModEntities.THRAGG_BOSS,
                            0x8B0000,  // vermelho escuro
                            0xFFD700,  // dourado
                            new net.minecraft.world.item.Item.Properties()
                    )
            );

    public static final RegistryObject<ForgeSpawnEggItem> LUCAN_SPAWN_EGG =
            ITEMS.register("lucan_boss_spawn_egg", () ->
                    new ForgeSpawnEggItem(
                            ModEntities.LUCAN_BOSS,
                            0x8B0000,  // vermelho escuro
                            0xFFD700,  // dourado
                            new net.minecraft.world.item.Item.Properties()
                    )
            );
}