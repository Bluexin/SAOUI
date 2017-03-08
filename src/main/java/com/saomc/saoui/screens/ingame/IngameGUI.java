package com.saomc.saoui.screens.ingame;

import com.saomc.saoui.GLCore;
import com.saomc.saoui.config.OptionCore;
import com.saomc.saoui.effects.StatusEffects;
import com.saomc.saoui.neo.screens.IngameMenuGUI;
import com.saomc.saoui.resources.StringNames;
import com.saomc.saoui.screens.death.DeathScreen;
import com.saomc.saoui.social.StaticPlayerHelper;
import com.saomc.saoui.social.party.PartyHelper;
import com.saomc.saoui.themes.ThemeLoader;
import com.saomc.saoui.themes.elements.HudPartType;
import com.saomc.saoui.themes.util.HudDrawContext;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiOverlayDebug;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.*;

@SideOnly(Side.CLIENT)
public class IngameGUI extends GuiIngameForge {

    private FontRenderer fontRenderer;
    private RenderGameOverlayEvent eventParent;
    private int offsetUsername;
    private GuiOverlayDebugForge debugOverlay;

    @Deprecated // use getContext() instead of directly calling this
    private HudDrawContext ctx;

    public IngameGUI(Minecraft mc) {
        super(mc);
        this.debugOverlay = new GuiOverlayDebugForge(mc);
    }

    @Override
    public void renderGameOverlay(float partialTicks) {
        mc.mcProfiler.startSection("setup");
        fontRenderer = mc.fontRendererObj;
        String username = mc.player.getDisplayNameString();
        int maxNameWidth = fontRenderer.getStringWidth(username);
        int usernameBoxes = 1 + (maxNameWidth + 4) / 5;
        offsetUsername = 18 + usernameBoxes * 5;
        ScaledResolution res = new ScaledResolution(mc);
        eventParent = new RenderGameOverlayEvent(partialTicks, res);
        int width = res.getScaledWidth();
        int height = res.getScaledHeight();
        getContext().setTime(partialTicks);
        getContext().setScaledResolution(res);
        getContext().setZ(zLevel);
        getContext().setPlayer(mc.player);
        GLCore.glBlend(true);
        mc.mcProfiler.endSection();

        super.renderGameOverlay(partialTicks);

        if (OptionCore.FORCE_HUD.isEnabled() && !this.mc.playerController.shouldDrawHUD() && this.mc.getRenderViewEntity() instanceof EntityPlayer) {
            if (renderHealth) renderHealth(width, height);
            if (renderArmor) renderArmor(width, height);
            if (renderFood) renderFood(width, height);
            if (renderHealthMount) renderHealthMount(width, height);
            if (renderAir) renderAir(width, height);
            mc.entityRenderer.setupOverlayRendering();
        } // Basically adding what super doesn't render by default

    }

    @Override
    protected void renderCrosshairs(float partialTicks) {
        if (pre(CROSSHAIRS)) return;
        ThemeLoader.HUD.draw(HudPartType.CROSS_HAIR, getContext()); // TODO: rework
        if (OptionCore.CROSS_HAIR.isEnabled() && !(mc.currentScreen instanceof IngameMenuGUI || mc.currentScreen instanceof DeathScreen)) {
            super.renderCrosshairs(partialTicks);
        }
        post(CROSSHAIRS);
    }

    @Override
    protected void renderArmor(int width, int height) {
        if (OptionCore.VANILLA_UI.isEnabled()) super.renderArmor(width, height);
        else {
            if (replaceEvent(ARMOR)) return;
            ThemeLoader.HUD.draw(HudPartType.ARMOR, getContext());
            post(ARMOR);
        }
    }

    @Override
    protected void renderHotbar(ScaledResolution res, float partialTicks) {
        if (replaceEvent(HOTBAR)) return;
        if (mc.playerController.isSpectator()) this.spectatorGui.renderTooltip(res, partialTicks);
        else if (OptionCore.DEFAULT_HOTBAR.isEnabled()) super.renderHotbar(res, partialTicks);
        else {
            mc.mcProfiler.startSection("hotbar");
            ThemeLoader.HUD.draw(HudPartType.HOTBAR, getContext());
            mc.mcProfiler.endSection();
        }

        post(HOTBAR);
    }

    @Override
    protected void renderAir(int width, int height) {
        if (OptionCore.VANILLA_UI.isEnabled()) super.renderAir(width, height);
        else {
            if (replaceEvent(AIR)) return;
            mc.mcProfiler.startSection("air");
            EntityPlayer player = (EntityPlayer) this.mc.getRenderViewEntity();
            GLCore.glBlend(true);
            int left = width / 2 + 91;
            int top = height - right_height;

            if (player.isInsideOfMaterial(Material.WATER)) {
                int air = player.getAir();
                int full = MathHelper.ceil((double) (air - 2) * 10.0D / 300.0D);
                int partial = MathHelper.ceil((double) air * 10.0D / 300.0D) - full;

                for (int i = 0; i < full + partial; ++i) {
                    drawTexturedModalRect(left - i * 8 - 9, top, (i < full ? 16 : 25), 18, 9, 9);
                }
                right_height += 10;
            }

            GLCore.glBlend(false);
            mc.mcProfiler.endSection();
            // Linked to renderHealth
            post(AIR);
        }
    }

    @Override
    public void renderHealth(int width, int height) {
        if (OptionCore.VANILLA_UI.isEnabled()) super.renderHealth(width, height);
        else {
            if (replaceEvent(HEALTH)) return;
            mc.mcProfiler.startSection("health");
            ThemeLoader.HUD.draw(HudPartType.HEALTH_BOX, getContext());
            mc.mcProfiler.endSection();
            post(HEALTH);

            final int healthBarWidth = 234;
            final int healthWidth = 216;
            final int healthHeight = OptionCore.SAO_UI.isEnabled() ? 9 : 4;
            int stepOne = (int) (healthWidth / 3.0F - 3);
            int stepTwo = (int) (healthWidth / 3.0F * 2.0F - 3);
            int stepThree = healthWidth - 3;

            renderFood(healthWidth, healthHeight, offsetUsername, stepOne, stepTwo, stepThree);

            GLCore.glColor(1.0F, 1.0F, 1.0F, 1.0F);

            mc.mcProfiler.startSection("effects");

            final int offsetForEffects = offsetUsername + healthBarWidth - 4;
            final List<StatusEffects> effects = StatusEffects.getEffects(mc.player);

            GLCore.glBindTexture(OptionCore.SAO_UI.isEnabled() ? StringNames.gui : StringNames.guiCustom);

            for (int i = 0; i < effects.size(); i++) effects.get(i).glDraw(offsetForEffects + i * 11, 2, zLevel);

            mc.mcProfiler.endSection();

//            if (PartyHelper.instance().hasParty()) renderParty();
        }
    }

    private void renderParty() {
        mc.mcProfiler.startSection("party");

        GLCore.glAlphaTest(true);
        GLCore.glBlend(true);

        int index = 0;
        final int baseY = 35;
        final int h = 15;
        for (final EntityPlayer player : PartyHelper.instance().listMembers()) {
            if (player == mc.player) continue;

            GLCore.glBindTexture(OptionCore.SAO_UI.isEnabled() ? StringNames.gui : StringNames.guiCustom);

            GLCore.glTexturedRect(2, baseY + index * h, zLevel, 85, 15, 10, 13);
            GLCore.glTexturedRect(13, baseY + index * h, zLevel, 80, 15, 5, 13);

            String playerName = player.getDisplayNameString();
            if (playerName.length() > 5) playerName = playerName.substring(0, 5);

            final int nameBoxes = 29 / 5 + 1;

            GLCore.glTexturedRect(18, baseY + index * h, zLevel, nameBoxes * 5, 13, 65, 15, 5, 13);

            int offset = 18 + nameBoxes * 5;

            GLCore.glTexturedRect(offset, baseY + index * h, zLevel, 40, 28, 100, 13);

            final int hpWidth = 97;
            final int hpHeight = 3;

            final int hpValue = (int) (StaticPlayerHelper.getHealth(mc, player, ctx.getPartialTicks()) / StaticPlayerHelper.getMaxHealth(player) * hpWidth);
            HealthStep.getStep(mc, player, ctx.getPartialTicks()).glColor();

            int hp = hpHeight;
            for (int j = 0; j < hpValue; j++) {
                GLCore.glTexturedRect(offset + 1 + j, baseY + 5 + index * h, zLevel, (hpHeight - hp), 15, 1, hp);

                if (j >= hpValue - hp) {
                    hp--;
                    if (hp <= 0) break;
                }
            }

            offset += 100;

            GLCore.glColor(1.0F, 1.0F, 1.0F, 1.0F);
            GLCore.glTexturedRect(offset, baseY + index * h, zLevel, 70, 15, 5, 13);
            GLCore.glString(playerName, 18, baseY + 1 + index * h + (13 - fontRenderer.FONT_HEIGHT) / 2, 0xFFFFFFFF);

            index++;
        }

        mc.mcProfiler.endSection();
    }

    @Override
    public void renderFood(int width, int height) {
        if (OptionCore.VANILLA_UI.isEnabled()) super.renderFood(width, height);
        // See below, called by renderHealth
    }

    private void renderFood(int healthWidth, int healthHeight, int offsetUsername, int stepOne, int stepTwo, int stepThree) {
        if (replaceEvent(FOOD)) return;
        mc.mcProfiler.startSection("food");
        HudDrawContext ctx = getContext();
        final int foodValue = (int) (StaticPlayerHelper.getHungerFract(mc, mc.player, ctx.getPartialTicks()) * healthWidth);
        int h = foodValue < 12 ? 12 - foodValue : 0;
        int o = healthHeight;
        GLCore.glAlphaTest(true);
        GLCore.glBlend(true);
        GLCore.glColorRGBA(0x8EE1E8);
        /*for (int i = 0; i < foodValue; i++) {
            GLCore.glTexturedRect(offsetUsername + i + 4, 9, zLevel, h, 240, 1, o);
            if (foodValue < healthWidth && i >= foodValue - 3) o--;

            if (foodValue <= 12) {
                h++;
                if (h > 12) break;
            } else if ((i >= stepOne && i <= stepOne + 3) || (i >= stepTwo && i <= stepTwo + 3) || (i >= stepThree)) {
                h++;

                if (h > 12) break;
            }
        }*/

        if (foodValue >= stepTwo && foodValue < stepThree)
            GLCore.glTexturedRect(offsetUsername + foodValue, 9, zLevel, 11, 249, 7, 4);
        if (foodValue >= stepOne && foodValue < stepTwo + 4)
            GLCore.glTexturedRect(offsetUsername + foodValue, 9, zLevel, 4, 249, 7, 4);
        if (foodValue < stepOne + 4 && foodValue > 0) {
            GLCore.glTexturedRect(offsetUsername + foodValue + 2, 9, zLevel, 0, 249, 4, 4);
            for (int i = 0; i < foodValue - 2; i++)
                GLCore.glTexturedRect(offsetUsername + i + 4, 9, zLevel, 0, 249, 4, 4);
        }

        mc.mcProfiler.endSection();
        post(FOOD);
    }

    @Override
    protected void renderExperience(int width, int height) {
        if (OptionCore.VANILLA_UI.isEnabled()) super.renderExperience(width, height);
        else {
            if (pre(EXPERIENCE)) return;
            if (!OptionCore.FORCE_HUD.isEnabled() && !this.mc.playerController.shouldDrawHUD()) return;
            mc.mcProfiler.startSection("expLevel");

            ThemeLoader.HUD.draw(HudPartType.EXPERIENCE, getContext());

            mc.mcProfiler.endSection();
            post(EXPERIENCE);
        }
    }

    @Override
    protected void renderJumpBar(int width, int height) {
        if (OptionCore.VANILLA_UI.isEnabled()) super.renderJumpBar(width, height);
        else {
            if (replaceEvent(JUMPBAR)) return;
            renderExperience(width, height);

            mc.mcProfiler.startSection("jumpBar");
            ThemeLoader.HUD.draw(HudPartType.JUMP_BAR, getContext());
            mc.mcProfiler.endSection();

            post(JUMPBAR);
        }
    }

    @Override
    protected void renderHealthMount(int width, int height) {
        if (OptionCore.VANILLA_UI.isEnabled()) super.renderHealthMount(width, height);
        else {
            EntityPlayer player = (EntityPlayer) mc.getRenderViewEntity();
            if (player == null) return;
            Entity tmp = player.getRidingEntity();
            if (!(tmp instanceof EntityLivingBase)) return;

            if (replaceEvent(HEALTHMOUNT)) return;
            // Not implemented yet
            post(HEALTHMOUNT);
        }
    }

    @Override
    protected void renderHUDText(int width, int height) {
        if (OptionCore.VANILLA_UI.isEnabled() || OptionCore.DEFAULT_DEBUG.isEnabled())
            super.renderHUDText(width, height);
        else {
            mc.mcProfiler.startSection("forgeHudText");
            OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
            ArrayList<String> listL = new ArrayList<>();
            ArrayList<String> listR = new ArrayList<>();

            if (mc.isDemo()) {
                long time = mc.world.getTotalWorldTime();
                if (time >= 120500L) listR.add(I18n.format("demo.demoExpired"));
                else
                    listR.add(I18n.format("demo.remainingTime", StringUtils.ticksToElapsedTime((int) (120500L - time))));
            }

            if (this.mc.gameSettings.showDebugInfo && !pre(DEBUG)) {
                listL.addAll(debugOverlay.getLeft());
                listR.addAll(debugOverlay.getRight());
                post(DEBUG);
            }

            RenderGameOverlayEvent.Text event = new RenderGameOverlayEvent.Text(eventParent, listL, listR);
            if (!MinecraftForge.EVENT_BUS.post(event)) {
                int top = 20;
                for (String msg : listL) {
                    if (msg == null) continue;
                    drawRect(1, top - 1, 2 + fontRenderer.getStringWidth(msg) + 1, top + fontRenderer.FONT_HEIGHT - 1, -1873784752);
                    fontRenderer.drawString(msg, 2, top, 14737632);
                    top += fontRenderer.FONT_HEIGHT;
                }

                top = 2;
                for (String msg : listR) {
                    if (msg == null) continue;
                    int w = fontRenderer.getStringWidth(msg);

                    final int slotsY = (height - 9 * 22) / 2;
//                        (res.getScaledHeight() - (slotCount * 22)) / 2;

                /*for (int i = 0; i < slotCount; i++) {
                    GLCore.glColorRGBA(i == inv.currentItem ? 0xFFBA66AA : 0xCDCDCDAA);
                    GLCore.glTexturedRect(res.getScaledWidth() - 24, slotsY + (22 * i), zLevel, 0, 25, 20, 20);
                }*/

                    int left = width - (OptionCore.HOR_HOTBAR.isEnabled() || top < slotsY - fontRenderer.FONT_HEIGHT - 2 ? 2 : 26) - w;
                    drawRect(left - 1, top - 1, left + w + 1, top + fontRenderer.FONT_HEIGHT - 1, -1873784752);
                    fontRenderer.drawString(msg, left, top, 14737632);
                    top += fontRenderer.FONT_HEIGHT;
                }
            }

            mc.mcProfiler.endSection();
            post(TEXT);
        }
    }

    private boolean replaceEvent(ElementType el) {
        if (eventParent.getType() == el && eventParent.isCanceled()) {
            eventParent.setCanceled(false);
            eventParent.setResult(Event.Result.ALLOW);
            pre(el);
            return true;
        }
        return false;
    }

    // c/p from GuiIngameForge
    private boolean pre(ElementType type) {
        return MinecraftForge.EVENT_BUS.post(new RenderGameOverlayEvent.Pre(eventParent, type));
    }

    private void post(ElementType type) {
        MinecraftForge.EVENT_BUS.post(new RenderGameOverlayEvent.Post(eventParent, type));
    }

    @SuppressWarnings("deprecation")
    private HudDrawContext getContext() {
        if (ctx == null) this.ctx = new HudDrawContext(mc.player, mc, itemRenderer);
        return ctx;
    }

    private class GuiOverlayDebugForge extends GuiOverlayDebug {
        private GuiOverlayDebugForge(Minecraft mc) {
            super(mc);
        }

        @Override
        protected void renderDebugInfoLeft() {
        }

        @Override
        protected void renderDebugInfoRight(@NotNull ScaledResolution res) {
        }

        private List<String> getLeft() {
            return this.call();
        }

        private List<String> getRight() {
            return this.getDebugInfoRight();
        }
    }

}
