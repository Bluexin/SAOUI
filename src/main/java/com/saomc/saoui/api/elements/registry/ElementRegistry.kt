package com.saomc.saoui.api.elements.registry

import be.bluexin.saomclib.message
import com.saomc.saoui.SAOCore
import com.saomc.saoui.api.elements.CategoryButton
import com.saomc.saoui.api.elements.IconElement
import com.saomc.saoui.api.elements.NeoElement
import com.saomc.saoui.api.elements.optionCategory
import com.saomc.saoui.api.events.MenuBuildingEvent
import com.saomc.saoui.api.items.IItemFilter
import com.saomc.saoui.api.items.ItemFilterRegister
import com.saomc.saoui.api.screens.IIcon
import com.saomc.saoui.config.OptionCore
import com.saomc.saoui.events.EventCore
import com.saomc.saoui.screens.menus.IngameMenu
import com.saomc.saoui.screens.util.PopupYesNo
import com.saomc.saoui.screens.util.itemList
import com.saomc.saoui.util.AdvancementUtil
import com.saomc.saoui.util.IconCore
import com.teamwizardry.librarianlib.features.helpers.vec
import com.teamwizardry.librarianlib.features.kotlin.Minecraft
import net.minecraft.client.gui.GuiIngameMenu
import net.minecraft.client.gui.GuiOptions
import net.minecraft.client.resources.I18n
import net.minecraftforge.common.MinecraftForge

object ElementRegistry {

    /**
     * This is a fixed list of elements for the menu
     * that will be pulled everytime the menu is opened
     */
    val registeredElements = hashMapOf<Type, ArrayList<NeoElement>>()

    fun initRegistry(){
        registeredElements.clear()
        val event = MenuBuildingEvent(getDefaultElements(), Type.INGAMEMENU)
        MinecraftForge.EVENT_BUS.post(event)
        registeredElements[Type.INGAMEMENU] = event.elements
    }

    fun getDefaultElements() = {
        var index = 0
        arrayListOf<NeoElement>(
                tlCategory(IconCore.PROFILE, index++) {
                    ItemFilterRegister.tlFilters.forEach { baseFilter ->
                        addItemCategories(this, baseFilter)
                    }
                    category(IconCore.SKILLS, I18n.format("sao.element.skills")) {
                        category(IconCore.SKILLS, "Test 1") {
                            category(IconCore.SKILLS, "1.1") {
                                for (i in 1..3) category(IconCore.SKILLS, "1.1.$i")
                            }
                            category(IconCore.SKILLS, "1.2") {
                                for (i in 1..3) category(IconCore.SKILLS, "1.2.$i")
                            }
                            category(IconCore.SKILLS, "1.3") {
                                for (i in 1..3) category(IconCore.SKILLS, "1.3.$i")
                            }
                        }
                        category(IconCore.SKILLS, "解散") {
                            onClick { _, _ ->
                                highlighted = true
                                controllingGUI?.openGui(PopupYesNo("Disolve", "パーチイを解散しますか？", ""))?.plusAssign {
                                    IngameMenu.mc.player.message("Result: $it")
                                    highlighted = false
                                }
                                true
                            }
                        }
                        category(IconCore.SKILLS, "3") {
                            category(IconCore.SKILLS, "3.1") {
                                for (i in 1..6) category(IconCore.SKILLS, "3.1.$i")
                            }
                            category(IconCore.SKILLS, "3.2") {
                                for (i in 1..7) category(IconCore.SKILLS, "3.2.$i")
                            }
                            category(IconCore.SKILLS, "3.3") {
                                for (i in 1..10) category(IconCore.SKILLS, "3.3.$i")
                            }
                        }
                    }
                    crafting()
                    profile(IngameMenu.mc.player)
                },
                tlCategory(IconCore.SOCIAL, index++) {
                    category(IconCore.GUILD, I18n.format("sao.element.guild")) {
                        this.delegate.disabled = !SAOCore.isSAOMCLibServerSide
                    }
                    partyMenu()
                    friendMenu()
                },
                tlCategory(IconCore.MESSAGE, index++),
                tlCategory(IconCore.NAVIGATION, index++) {
                    category(IconCore.QUEST, I18n.format("sao.element.quest")) {
                        AdvancementUtil.getCategories().forEach {
                            advancementCategory(it)
                        }
                    }
                    recipes()
                },
                tlCategory(IconCore.SETTINGS, index) {
                    category(IconCore.OPTION, I18n.format("sao.element.options")) {
                        category(IconCore.OPTION, I18n.format("guiOptions")) {
                            onClick { _, _ ->
                                IngameMenu.mc.displayGuiScreen(GuiOptions(controllingGUI, EventCore.mc.gameSettings))
                                true
                            }
                        }
                        OptionCore.tlOptions.forEach {
                            +optionCategory(it)
                        }
                    }
                    category(IconCore.HELP, I18n.format("sao.element.menu")) {
                        onClick { _, _ ->
                            IngameMenu.mc.displayGuiScreen(GuiIngameMenu())
                            true
                        }
                    }
                    category(IconCore.LOGOUT, if (OptionCore.LOGOUT()) I18n.format("sao.element.logout") else "") {
                        onClick { _, _ ->
                            if (OptionCore.LOGOUT()) {
                                (controllingGUI as? IngameMenu)?.loggingOut = true
                                true
                            } else false
                        }
                    }
                }
        )
    }.invoke()

    fun addItemCategories(button: CategoryButton, filter: IItemFilter){
        button.category(filter.icon, filter.displayName) {
            if (filter.isCategory) {
                if (filter.subFilters.isNotEmpty())
                    filter.subFilters.forEach { subFilter -> addItemCategories(this, subFilter) }
            }
            else itemList(Minecraft().player.inventoryContainer, filter)
        }
    }

    fun tlCategory(icon: IIcon, index: Int, body: (CategoryButton.() -> Unit)? = null): CategoryButton {
        return CategoryButton(IconElement(icon, vec(0, 25 * index)), null, body)
    }

    enum class Type {
        MAINMENU,
        INGAMEMENU;
    }
}