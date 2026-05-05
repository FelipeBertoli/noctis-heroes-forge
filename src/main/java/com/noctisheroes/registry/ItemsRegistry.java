package com.noctisheroes.registry;

import com.noctisheroes.NoctisHeroes;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ItemsRegistry {

    public static final DeferredRegister<net.minecraft.world.item.Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, NoctisHeroes.MODID);

    public static final RegistryObject<ForgeSpawnEggItem> VILTRUMITE_SPAWN_EGG =
            ITEMS.register("viltrumite_spawn_egg", () ->
                    new ForgeSpawnEggItem(
                            EntitiesRegistry.VILTRUMITE,
                            0x1a1a2e,  // azul escuro
                            0xc0392b,  // vermelho
                            new net.minecraft.world.item.Item.Properties()
                    )
            );

    public static final RegistryObject<ForgeSpawnEggItem> THRAGG_SPAWN_EGG =
            ITEMS.register("thragg_boss_spawn_egg", () ->
                    new ForgeSpawnEggItem(
                            EntitiesRegistry.THRAGG_BOSS,
                            0x8B0000,  // vermelho escuro
                            0xFFD700,  // dourado
                            new net.minecraft.world.item.Item.Properties()
                    )
            );

    public static final RegistryObject<ForgeSpawnEggItem> LUCAN_SPAWN_EGG =
            ITEMS.register("lucan_boss_spawn_egg", () ->
                    new ForgeSpawnEggItem(
                            EntitiesRegistry.LUCAN_BOSS,
                            0x8B0000,  // vermelho escuro
                            0xFFD700,  // dourado
                            new net.minecraft.world.item.Item.Properties()
                    )
            );

    public static final RegistryObject<ForgeSpawnEggItem> DRAKARI_SOLDIER_SPAWN_EGG =
            ITEMS.register("drakari_soldier_spawn_egg", () ->
                    new ForgeSpawnEggItem(
                            EntitiesRegistry.DRAKARI_SOLDIER,
                            0x161518,
                            0x8d00bd,  // vermelho
                            new net.minecraft.world.item.Item.Properties()
                    )
            );
}