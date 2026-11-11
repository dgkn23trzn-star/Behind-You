package com.arkandayim.client;

import com.arkandayim.ArkandayimMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ArkandayimMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MainMenuThreat {

    private static boolean dismissed = false;
    private static boolean scheduled = false;

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (dismissed || scheduled) {
            return;
        }
        if (!(event.getScreen() instanceof TitleScreen)) {
            return;
        }
        scheduled = true;
        Minecraft.getInstance().execute(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.screen instanceof TitleScreen) {
                mc.setScreen(new ThreatScreen());
            }
        });
    }

    public static void dismiss() {
        dismissed = true;
    }
}
