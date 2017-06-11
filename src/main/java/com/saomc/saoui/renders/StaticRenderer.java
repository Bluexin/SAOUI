package com.saomc.saoui.renders;

import com.saomc.saoui.GLCore;
import com.saomc.saoui.SAOCore;
import com.saomc.saoui.api.entity.rendering.RenderCapability;
import com.saomc.saoui.effects.DeathParticles;
import com.saomc.saoui.resources.StringNames;
import com.saomc.saoui.screens.ingame.HealthStep;
import com.saomc.saoui.social.StaticPlayerHelper;
import com.saomc.saoui.config.OptionCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.IMob;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class StaticRenderer { // TODO: add usage of scale, offset etc from capability

    private static final int HEALTH_COUNT = 32;
    private static final double HEALTH_ANGLE = 0.35F;
    private static final double HEALTH_RANGE = 0.975F;
    private static final float HEALTH_OFFSET = 0.75F;
    private static final double HEALTH_HEIGHT = 0.21F;

    @SuppressWarnings("ConstantConditions")
    public static void render(RenderManager renderManager, EntityLivingBase living, double x, double y, double z) {
        final Minecraft mc = Minecraft.getMinecraft();

        boolean dead = StaticPlayerHelper.INSTANCE.getHealth(mc, living, SAOCore.UNKNOWN_TIME_DELAY) <= 0;

        if (living.deathTime == 1) living.deathTime++;

        if (!dead && !living.isInvisibleToPlayer(mc.player)) {
            if (OptionCore.COLOR_CURSOR.isEnabled() && living.hasCapability(RenderCapability.RENDER_CAPABILITY, null))
                doRenderColorCursor(renderManager, mc, living, x, y, z, 64);

            if (OptionCore.HEALTH_BARS.isEnabled() && !living.equals(mc.player) && living.hasCapability(RenderCapability.RENDER_CAPABILITY, null))
                doRenderHealthBar(renderManager, mc, living, x, y, z);
        }
    }

    private static void doRenderColorCursor(RenderManager renderManager, Minecraft mc, EntityLivingBase entity, double x, double y, double z, int distance) {
        if (entity.getRidingEntity() != null) return;
        if (OptionCore.LESS_VISUALS.isEnabled() && !(entity instanceof IMob || StaticPlayerHelper.INSTANCE.getHealth(mc, entity, SAOCore.UNKNOWN_TIME_DELAY) != StaticPlayerHelper.INSTANCE.getMaxHealth(entity)))
            return;

        if (entity.world.isRemote) {
            double d3 = entity.getDistanceSqToEntity(renderManager.renderViewEntity);

            if (d3 <= (double) (distance * distance)) {
                final float sizeMult = entity.isChild() && entity instanceof EntityMob ? 0.5F : 1.0F;

                float f = 1.6F;
                float f1 = 0.016666668F * f;

                GLCore.start();
                GLCore.glTranslatef((float) x + 0.0F, (float) y + sizeMult * entity.height + sizeMult * 1.1F, (float) z);
                GLCore.glNormal3f(0.0F, 1.0F, 0.0F);
                GLCore.glRotatef(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
                GLCore.glScalef(-(f1 * sizeMult), -(f1 * sizeMult), (f1 * sizeMult));
                GLCore.lighting(false);

                GLCore.glDepthTest(true);

                GLCore.glAlphaTest(true);
                GLCore.glBlend(true);
                GLCore.tryBlendFuncSeparate(770, 771, 1, 0);

                GLCore.glBindTexture(OptionCore.SAO_UI.isEnabled() ? StringNames.entities : StringNames.entitiesCustom);
                GLCore.glColorRGBA(RenderCapability.get(entity).colorStateHandler.getColorState().rgba());
                GLCore.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

                if (OptionCore.SPINNING_CRYSTALS.isEnabled()) {
                    double a = (entity.world.getTotalWorldTime() % 40) / 20.0D * Math.PI;
                    double cos = Math.cos(a);//Math.PI / 3 * 2);
                    double sin = Math.sin(a);//Math.PI / 3 * 2);

                    if (a > Math.PI / 2 && a <= Math.PI * 3 / 2) {
                        GLCore.addVertex(9.0D * cos, -1, 9.0D * sin, 0.125F, 0.25F);
                        GLCore.addVertex(9.0D * cos, 17, 9.0D * sin, 0.125F, 0.375F);
                        GLCore.addVertex(-9.0D * cos, 17, -9.0D * sin, 0F, 0.375F);
                        GLCore.addVertex(-9.0D * cos, -1, -9.0D * sin, 0F, 0.25F);
                    } else {
                        GLCore.addVertex(-9.0D * cos, -1, -9.0D * sin, 0F, 0.25F);
                        GLCore.addVertex(-9.0D * cos, 17, -9.0D * sin, 0F, 0.375F);
                        GLCore.addVertex(9.0D * cos, 17, 9.0D * sin, 0.125F, 0.375F);
                        GLCore.addVertex(9.0D * cos, -1, 9.0D * sin, 0.125F, 0.25F);
                    }

                    if (a < Math.PI) {
                        GLCore.addVertex(-9.0D * sin, -1, 9.0D * cos, 0.125F, 0.25F);
                        GLCore.addVertex(-9.0D * sin, 17, 9.0D * cos, 0.125F, 0.375F);
                        GLCore.addVertex(9.0D * sin, 17, -9.0D * cos, 0F, 0.375F);
                        GLCore.addVertex(9.0D * sin, -1, -9.0D * cos, 0F, 0.25F);
                    } else {
                        GLCore.addVertex(9.0D * sin, -1, -9.0D * cos, 0F, 0.25F);
                        GLCore.addVertex(9.0D * sin, 17, -9.0D * cos, 0F, 0.375F);
                        GLCore.addVertex(-9.0D * sin, 17, 9.0D * cos, 0.125F, 0.375F);
                        GLCore.addVertex(-9.0D * sin, -1, 9.0D * cos, 0.125F, 0.25F);
                    }
                    GLCore.draw();
                } else {
                    GLCore.addVertex(-9, -1, 0.0D, 0F, 0.25F);
                    GLCore.addVertex(-9, 17, 0.0D, 0F, 0.375F);
                    GLCore.addVertex(9, 17, 0.0D, 0.125F, 0.375F);
                    GLCore.addVertex(9, -1, 0.0D, 0.125F, 0.25F);
                    GLCore.draw();
                }

                GLCore.lighting(true);
                GLCore.end();
            }
        }
    }

    private static void doRenderHealthBar(RenderManager renderManager, Minecraft mc, EntityLivingBase living, double x, double y, double z) {
        if (living.getRidingEntity() != null && living.getRidingEntity() == mc.player) return;
        if (OptionCore.LESS_VISUALS.isEnabled() && !(living instanceof IMob || StaticPlayerHelper.INSTANCE.getHealth(mc, living, SAOCore.UNKNOWN_TIME_DELAY) != StaticPlayerHelper.INSTANCE.getMaxHealth(living)))
            return;

        GLCore.glBindTexture(OptionCore.SAO_UI.isEnabled() ? StringNames.entities : StringNames.entitiesCustom);
        GLCore.start();
        GLCore.glDepthTest(true);
        GLCore.glCullFace(false);
        GLCore.glBlend(true);

        GLCore.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        final int hitPoints = (int) (getHealthFactor(mc, living, SAOCore.UNKNOWN_TIME_DELAY) * HEALTH_COUNT);
        useColor(mc, living, SAOCore.UNKNOWN_TIME_DELAY);

        final float sizeMult = living.isChild() && living instanceof EntityMob ? 0.5F : 1.0F;

        GLCore.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX);
        for (int i = 0; i <= hitPoints; i++) {
            final double value = (double) (i + HEALTH_COUNT - hitPoints) / HEALTH_COUNT;
            final double rad = Math.toRadians(renderManager.playerViewY - 135) + (value - 0.5) * Math.PI * HEALTH_ANGLE;

            final double x0 = x + sizeMult * living.width * HEALTH_RANGE * Math.cos(rad);
            final double y0 = y + sizeMult * living.height * HEALTH_OFFSET;
            final double z0 = z + sizeMult * living.width * HEALTH_RANGE * Math.sin(rad);

            final double uv_value = value - (double) (HEALTH_COUNT - hitPoints) / HEALTH_COUNT;

            GLCore.addVertex(x0, y0 + HEALTH_HEIGHT, z0, (1.0 - uv_value), 0);
            GLCore.addVertex(x0, y0, z0, (1.0 - uv_value), 0.125);
        }

        GLCore.draw();

        GLCore.glColor(1, 1, 1, 1);
        GLCore.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX);

        for (int i = 0; i <= HEALTH_COUNT; i++) {
            final double value = (double) i / HEALTH_COUNT;
            final double rad = Math.toRadians(renderManager.playerViewY - 135) + (value - 0.5) * Math.PI * HEALTH_ANGLE;

            final double x0 = x + sizeMult * living.width * HEALTH_RANGE * Math.cos(rad);
            final double y0 = y + sizeMult * living.height * HEALTH_OFFSET;
            final double z0 = z + sizeMult * living.width * HEALTH_RANGE * Math.sin(rad);

            GLCore.addVertex(x0, y0 + HEALTH_HEIGHT, z0, (1.0 - value), 0.125);
            GLCore.addVertex(x0, y0, z0, (1.0 - value), 0.25);
        }

        GLCore.draw();

        GLCore.glCullFace(true);
        GLCore.end();
    }

    public static void doSpawnDeathParticles(Minecraft mc, Entity living) {
        final World world = living.world;

        if (living.world.isRemote) {
            final float[][] colors = {
                    {1F / 0xFF * 0x9A, 1F / 0xFF * 0xFE, 1F / 0xFF * 0x2E},
                    {1F / 0xFF * 0x01, 1F / 0xFF * 0xFF, 1F / 0xFF * 0xFF},
                    {1F / 0xFF * 0x08, 1F / 0xFF * 0x08, 1F / 0xFF * 0x8A}
            };

            final float size = living.width * living.height;
            final int pieces = (int) Math.max(Math.min((size * 64), 128), 8);

            for (int i = 0; i < pieces; i++) {
                final float[] color = colors[i % 3];

                final double x0 = living.width * (Math.random() * 2 - 1) * 0.75;
                final double y0 = living.height * (Math.random());
                final double z0 = living.width * (Math.random() * 2 - 1) * 0.75;

                mc.effectRenderer.addEffect(new DeathParticles(
                        world,
                        living.posX + x0, living.posY + y0, living.posZ + z0,
                        color[0], color[1], color[2]
                ));
            }
        }
    }

    private static void useColor(Minecraft mc, Entity living, float time) {
        if (living instanceof EntityLivingBase) {
            HealthStep.getStep(mc, (EntityLivingBase) living, time).glColor();
        } else {
            HealthStep.GOOD.glColor();
        }
    }

    private static float getHealthFactor(Minecraft mc, Entity living, float time) {
        final float normalFactor = StaticPlayerHelper.INSTANCE.getHealth(mc, living, time) / StaticPlayerHelper.INSTANCE.getMaxHealth(living);
        final float delta = 1.0F - normalFactor;

        return normalFactor + (delta * delta / 2) * normalFactor;
    }

}
