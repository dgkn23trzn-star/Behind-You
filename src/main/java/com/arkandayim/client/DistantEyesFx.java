package com.arkandayim.client;

import com.arkandayim.ArkandayimMod;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * At night, one or two pairs of pale eyes sometimes stare at the player from the dark.
 */
@Mod.EventBusSubscriber(modid = ArkandayimMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DistantEyesFx {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(ArkandayimMod.MODID, "textures/entity/distant_eyes.png");
    private static final RenderType TYPE = RenderType.eyes(TEXTURE);
    private static final List<Eyes> ACTIVE = new ArrayList<>();
    private static final RandomSource RANDOM = RandomSource.create();

    private static final class Eyes {
        private final Vec3 pos;
        private final int total;
        private final float size;
        private int life;

        private Eyes(Vec3 pos, int total, float size) {
            this.pos = pos;
            this.total = total;
            this.life = total;
            this.size = size;
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            ACTIVE.clear();
            return;
        }
        long dayTime = mc.level.getDayTime() % 24000L;
        boolean night = dayTime >= 13000L && dayTime <= 23300L;
        if (!night) {
            ACTIVE.clear();
            return;
        }
        for (Iterator<Eyes> iterator = ACTIVE.iterator(); iterator.hasNext(); ) {
            Eyes eyes = iterator.next();
            if (--eyes.life <= 0) {
                iterator.remove();
            }
        }
        if (ACTIVE.isEmpty() && RANDOM.nextInt(360) == 0) {
            spawnPair(mc);
            if (RANDOM.nextBoolean()) {
                spawnPair(mc);
            }
        }
    }

    private static void spawnPair(Minecraft mc) {
        double angle = RANDOM.nextDouble() * Math.PI * 2.0D;
        double distance = 28.0D + RANDOM.nextDouble() * 30.0D;
        double x = mc.player.getX() + Math.cos(angle) * distance;
        double z = mc.player.getZ() + Math.sin(angle) * distance;
        double y = mc.player.getEyeY() + (RANDOM.nextDouble() - 0.5D) * 6.0D;
        ACTIVE.add(new Eyes(new Vec3(x, y, z), 140 + RANDOM.nextInt(180), 1.3F + RANDOM.nextFloat() * 0.9F));
    }

    /** Positions of the eyes currently staring, used by the night blackout pass. */
    public static List<Vec3> activePositions() {
        if (ACTIVE.isEmpty()) {
            return List.of();
        }
        List<Vec3> positions = new ArrayList<>(ACTIVE.size());
        for (Eyes eyes : ACTIVE) {
            positions.add(eyes.pos);
        }
        return positions;
    }

    @SubscribeEvent
    public static void onRenderStage(RenderLevelStageEvent event) {
        if (ACTIVE.isEmpty() || event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        Vec3 camera = event.getCamera().getPosition();
        float camYaw = event.getCamera().getYRot();
        float camPitch = event.getCamera().getXRot();
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(-camera.x, -camera.y, -camera.z);

        MultiBufferSource.BufferSource buffers = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        VertexConsumer consumer = buffers.getBuffer(TYPE);

        for (Eyes eyes : ACTIVE) {
            float fade = Math.min(1.0F, Math.min(eyes.life / 20.0F, (eyes.total - eyes.life) / 20.0F));
            if (eyes.life % 130 < 6) {
                fade *= 0.12F;
            }
            poseStack.pushPose();
            poseStack.translate(eyes.pos.x, eyes.pos.y, eyes.pos.z);
            poseStack.mulPose(Axis.YP.rotationDegrees(-camYaw));
            poseStack.mulPose(Axis.XP.rotationDegrees(camPitch));
            Matrix4f matrix = poseStack.last().pose();
            float s = eyes.size;
            float h = s * 0.32F;
            int alpha = (int) (255 * fade);
            vertex(consumer, matrix, -s, -h, alpha);
            vertex(consumer, matrix, -s, h, alpha);
            vertex(consumer, matrix, s, h, alpha);
            vertex(consumer, matrix, s, -h, alpha);
            poseStack.popPose();
        }

        buffers.endBatch();
        poseStack.popPose();
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, float x, float y, int alpha) {
        consumer.vertex(matrix, x, y, 0.0F)
                .color(255, 250, 235, alpha)
                .uv(0.0F, 1.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(0.0F, 0.0F, 1.0F)
                .endVertex();
    }
}
