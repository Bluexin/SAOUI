package com.saomc.saoui.events

import be.bluexin.saomclib.player
import be.bluexin.saomclib.world
import com.saomc.saoui.effects.RenderDispatcher
import cpw.mods.fml.common.eventhandler.SubscribeEvent
import cpw.mods.fml.common.gameevent.TickEvent
import cpw.mods.fml.common.network.FMLNetworkEvent
import net.minecraft.client.Minecraft
import net.minecraft.client.settings.GameSettings
import net.minecraftforge.client.event.*
import net.minecraftforge.event.entity.living.LivingDeathEvent

/**
 * This is the core for all event handlers, listening to events then passing on to the other events that need it.
 */
object EventCore {

    /*@SubscribeEvent
    public void chatListener(ClientChatReceivedEvent e) {
        EventHandler.nameNotification(e);
        EventHandler.chatCommand(e);
    }*/

    @SubscribeEvent
    fun clientTickListener(e: TickEvent.ClientTickEvent) {
        EventHandler.abilityCheck()
    }

    @SubscribeEvent
    fun renderTickListener(e: TickEvent.RenderTickEvent) {
        RenderHandler.deathHandlers()
        RenderHandler.deathCheck()
    }

    @SubscribeEvent
    fun onDeath(e: LivingDeathEvent) {
        if (e.entityLiving != null && e.entityLiving.world.isRemote)
            RenderHandler.addDeadMob(e.entityLiving)
    }

    @SubscribeEvent
    fun onDisconnect(e: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
        //        EventHandler.cleanTempElements();
        //        PartyHelper.instance().clean();
    }

    @SubscribeEvent
    fun renderPlayerListener(e: RenderPlayerEvent.Post) {
        RenderHandler.renderPlayer(e)
    }

    @SubscribeEvent
    fun renderEntityListener(e: RenderLivingEvent.Post) {
        RenderHandler.renderEntity(e)
    }

    @SubscribeEvent
    fun renderEntityListener(e: RenderLivingEvent.Pre) {
        if (e.entity.getDistanceSqToEntity(mc.player) > Math.pow((mc.gameSettings.getOptionFloatValue(GameSettings.Options.RENDER_DISTANCE) * 16).toDouble(), 2.0))
            e.isCanceled = true
    }

    @SubscribeEvent
    fun renderWorldListener(event: RenderWorldLastEvent) {
        RenderDispatcher.dispatch()
    }

    @SubscribeEvent
    fun guiListener(e: GuiScreenEvent) {
        RenderHandler.checkingameGUI()
    } // FIXME: perf !

    @SubscribeEvent
    fun guiOpenListener(e: GuiOpenEvent) {
        RenderHandler.guiInstance(e)
        RenderHandler.mainMenuGUI(e)
    }

    internal val mc = Minecraft.getMinecraft()
}
