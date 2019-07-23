/*
 * Copyright (C) 2016-2019 Arnaud 'Bluexin' Solé
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saomc.saoui.events

import com.saomc.saoui.SoundCore
import com.saomc.saoui.events.EventCore.mc
import net.minecraft.client.multiplayer.GuiConnecting
import net.minecraft.client.settings.KeyBinding
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
object EventHandler {

    var IS_SPRINTING = false
    var IS_SNEAKING = false

    internal fun nameNotification(e: ClientChatReceivedEvent) {
        if (mc.currentScreen !is GuiConnecting && e.message.unformattedText.contains(mc.player.displayNameString))
            SoundCore.play(mc, SoundCore.MESSAGE)
    }

    internal fun abilityCheck() {
        if (mc.player == null) {
            IS_SPRINTING = false
            IS_SNEAKING = false
        } else if (mc.inGameHasFocus) {
            if (IS_SPRINTING) KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.keyCode, true)
            if (IS_SNEAKING) KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.keyCode, true)
        }
    }
}

