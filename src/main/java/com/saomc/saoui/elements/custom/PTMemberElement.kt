package com.saomc.saoui.elements.custom

import be.bluexin.saomclib.capabilities.getPartyCapability
import be.bluexin.saomclib.party.PlayerInfo
import com.saomc.saoui.elements.IElement
import com.saomc.saoui.elements.IconLabelElement
import com.saomc.saoui.util.PlayerIcon
import com.teamwizardry.librarianlib.features.kotlin.Minecraft
import net.minecraft.client.resources.I18n

class PTMemberElement(val player: PlayerInfo, override var parent: IElement? = null, override val init: (IElement.() -> Unit)? = null): IconLabelElement(PlayerIcon(player), if (party?.isInvited(player) == true) I18n.format("sao.party.player_invited", player.username) else player.username) {

    val invited = party?.isInvited(player)?: false

    override fun update() {
        super.update()
        if ((invited && party?.isInvited(player) != true) || party?.isMember(player) != true)
            parent?.elements?.remove(this)
    }

    companion object{
        val party = Minecraft().player.getPartyCapability().partyData
        val invitedParty = Minecraft().player.getPartyCapability().inviteData
    }
}