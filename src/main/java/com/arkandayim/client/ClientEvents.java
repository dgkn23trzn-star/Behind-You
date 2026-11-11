package com.arkandayim.client;

import com.arkandayim.ArkandayimMod;
import com.arkandayim.entity.ArasEntity;
import com.arkandayim.network.FlashlightPacket;
import com.arkandayim.network.ModNetwork;
import com.arkandayim.network.VanishRequestPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ArkandayimMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientEvents {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            ClientState.locked = false;
            return;
        }

        while (ClientKeybinds.FLASHLIGHT.consumeClick()) {
            // Only talk to servers that actually run the mod, so a plain server never
            // kicks the player for an unknown packet.
            if (mc.getConnection() != null
                    && ModNetwork.CHANNEL.isRemotePresent(mc.getConnection().getConnection())) {
                ModNetwork.sendToServer(new FlashlightPacket());
            }
        }

        ClientState.displayAnger += (ClientState.anger - ClientState.displayAnger) * 0.18F;
        if (Math.abs(ClientState.anger - ClientState.displayAnger) < 0.25F) {
            ClientState.displayAnger = ClientState.anger;
        }
        if (ClientState.shatterAnim > 0) {
            ClientState.shatterAnim--;
        }
        if (ClientState.flash > 0.0F) {
            ClientState.flash = Math.max(0.0F, ClientState.flash - 0.045F);
        }
        if (ClientState.shakeTicks > 0) {
            ClientState.shakeTicks--;
        }
        if (ClientState.vanishCooldown > 0) {
            ClientState.vanishCooldown--;
        }

        if (ClientState.gazeTicks > 0) {
            gazeAtPoint(mc);
            ClientState.gazeTicks--;
        }

        ArasEntity target = findLockTarget(mc);
        ClientState.lastLocked = ClientState.locked;
        ClientState.locked = target != null;
        if (target != null) {
            lookAtTarget(mc, target);
            if (!ClientState.lastLocked) {
                ClientState.zoom = 70.0D;
            }
        } else {
            ClientState.zoom = 70.0D;
        }

        if (ClientState.vanishCooldown == 0) {
            crosshairCheck(mc);
        }
    }

    /** Smoothly turns the head towards the forming spot. */
    private static void gazeAtPoint(Minecraft mc) {
        Vec3 eye = mc.player.getEyePosition(1.0F);
        double dx = ClientState.gazeX - eye.x;
        double dy = ClientState.gazeY - eye.y;
        double dz = ClientState.gazeZ - eye.z;
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        float targetYaw = (float) (Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
        float targetPitch = Mth.clamp((float) -(Mth.atan2(dy, horizontal) * (180.0D / Math.PI)), -70.0F, 70.0F);
        float yaw = mc.player.getYRot() + Mth.wrapDegrees(targetYaw - mc.player.getYRot()) * 0.22F;
        float pitch = mc.player.getXRot() + (targetPitch - mc.player.getXRot()) * 0.22F;
        mc.player.setYRot(yaw);
        mc.player.setYHeadRot(yaw);
        mc.player.setXRot(pitch);
        mc.player.yRotO = yaw;
        mc.player.xRotO = pitch;
        ClientState.camYaw = yaw;
        ClientState.camPitch = pitch;
    }

    private static ArasEntity findLockTarget(Minecraft mc) {
        ArasEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof ArasEntity aras) || !aras.isLocking() || !aras.isAlive()) {
                continue;
            }
            double distance = mc.player.distanceTo(aras);
            if (distance < 220.0D && distance < bestDistance) {
                best = aras;
                bestDistance = distance;
            }
        }
        return best;
    }

    private static void lookAtTarget(Minecraft mc, ArasEntity target) {
        Vec3 eye = mc.player.getEyePosition(1.0F);
        Vec3 head = new Vec3(target.getX(), target.getY() + target.getBbHeight() * 0.85D, target.getZ());
        double dx = head.x - eye.x;
        double dy = head.y - eye.y;
        double dz = head.z - eye.z;
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
        float pitch = (float) -(Mth.atan2(dy, horizontal) * (180.0D / Math.PI));
        ClientState.camYaw = yaw;
        ClientState.camPitch = Mth.clamp(pitch, -80.0F, 80.0F);
        mc.player.setYRot(ClientState.camYaw);
        mc.player.setYHeadRot(ClientState.camYaw);
        mc.player.setXRot(ClientState.camPitch);
        mc.player.yRotO = ClientState.camYaw;
        mc.player.xRotO = ClientState.camPitch;
    }

    private static void crosshairCheck(Minecraft mc) {
        // While the camera is locked the crosshair always sits on the creature,
        // so asking it to vanish from there would fight the chase itself.
        if (ClientState.locked) {
            return;
        }
        Vec3 start = mc.player.getEyePosition(1.0F);
        Vec3 look = mc.player.getViewVector(1.0F);
        Vec3 end = start.add(look.scale(140.0D));
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(mc.player, start, end,
                mc.player.getBoundingBox().expandTowards(look.scale(140.0D)).inflate(1.0D),
                entity -> entity instanceof ArasEntity, 140.0D * 140.0D);
        if (hit == null || !(hit.getEntity() instanceof ArasEntity aras)) {
            return;
        }
        double distance = mc.player.distanceTo(aras);
        ModNetwork.sendToServer(new VanishRequestPacket(aras.getId(), distance > 25.0D));
        ClientState.vanishCooldown = 10;
    }

    @SubscribeEvent
    public static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (!ClientState.locked && ClientState.gazeTicks <= 0) {
            return;
        }
        event.setYaw(ClientState.camYaw);
        event.setPitch(ClientState.camPitch);
        if (ClientState.shakeTicks > 0) {
            event.setRoll((float) ((Math.random() - 0.5D) * 3.5D));
        } else {
            event.setRoll(0.0F);
        }
    }

    /** The flashlight only reaches about ten blocks, so the fog closes in with it. */
    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !NightDarkness.lampOn(mc)) {
            return;
        }
        if (!NightDarkness.isNight(mc) && !NightDarkness.isUnlit(mc)) {
            return;
        }
        event.setNearPlaneDistance(0.3F);
        event.setFarPlaneDistance(10.0F);
        event.setCanceled(true);
    }

    /**
     * With the flashlight on in the dark the fading distance is painted black, so the
     * world does not blur into a grey night sky but into pure nothing.
     */
    @SubscribeEvent
    public static void onRenderFogColor(ViewportEvent.ComputeFogColor event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        float fade = NightDarkness.lampFade();
        if (fade <= 0.01F) {
            return;
        }
        if (!NightDarkness.isNight(mc) && !NightDarkness.isUnlit(mc)) {
            return;
        }
        float keep = 1.0F - fade;
        event.setRed(event.getRed() * keep);
        event.setGreen(event.getGreen() * keep);
        event.setBlue(event.getBlue() * keep);
    }

    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        if (ClientState.locked) {
            event.setFOV(ClientState.zoom);
        }
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (!ClientState.locked) {
            return;
        }
        ClientState.zoom = Mth.clamp(ClientState.zoom - event.getScrollDelta() * 4.0D, 18.0D, 92.0D);
    }
}
