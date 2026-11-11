package com.arkandayim.entity;

import com.arkandayim.ArkandayimMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ArkandayimMod.MODID);

    public static final RegistryObject<EntityType<ArasEntity>> ARAS = ENTITIES.register("aras",
            () -> EntityType.Builder.<ArasEntity>of(ArasEntity::new, MobCategory.MONSTER)
                    .sized(0.65F, 2.8F)
                    .clientTrackingRange(24)
                    .updateInterval(2)
                    .build(new ResourceLocation(ArkandayimMod.MODID, "aras").toString()));

    public static AttributeSupplier.Builder arasAttributes() {
        return ArasEntity.createAttributes();
    }
}
