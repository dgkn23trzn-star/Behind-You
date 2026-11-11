package com.arkandayim.registry;

import com.arkandayim.ArkandayimMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ArkandayimMod.MODID);

    public static final RegistryObject<SoundEvent> WHISPER = register("whisper");
    public static final RegistryObject<SoundEvent> KNOCK = register("knock");
    public static final RegistryObject<SoundEvent> STEP = register("step");
    public static final RegistryObject<SoundEvent> HEARTBEAT = register("heartbeat");
    public static final RegistryObject<SoundEvent> FLASHLIGHT = register("flashlight");

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUNDS.register(name,
                () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ArkandayimMod.MODID, name)));
    }
}
