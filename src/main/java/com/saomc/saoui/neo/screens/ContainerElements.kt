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

package com.saomc.saoui.neo.screens

import com.saomc.saoui.GLCore
import com.saomc.saoui.SAOCore
import com.saomc.saoui.api.elements.neo.NeoCategoryButton
import com.saomc.saoui.api.elements.neo.NeoIconLabelElement
import com.saomc.saoui.api.screens.IIcon
import com.saomc.saoui.events.EventCore.mc
import com.saomc.saoui.screens.inventory.BaseFilters
import com.saomc.saoui.util.IconCore
import com.teamwizardry.librarianlib.features.kotlin.get
import com.teamwizardry.librarianlib.features.kotlin.isNotEmpty
import com.teamwizardry.librarianlib.features.kotlin.toolClasses
import com.teamwizardry.librarianlib.features.math.Vec2d
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.resources.I18n
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.inventory.*
import net.minecraft.item.*
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.registry.ForgeRegistries


@NeoGuiDsl
fun NeoCategoryButton.itemList(inventory: Container, filter: (iss: ItemStack) -> Boolean, vararg equippedRange: IntRange = arrayOf(-1..-1)) {
    inventory.inventorySlots.forEach { slot ->
        +ItemStackElement(slot, Vec2d.ZERO, equippedRange.any { r -> slot.slotNumber in r }, filter)
    }
    +object : NeoIconLabelElement(icon = IconCore.NONE, label = I18n.format("gui.empty")) {
        private var mark = false

        override val valid: Boolean
            get() {
                if (mark) return false
                mark = true
                val r = !this@itemList.validElementsSequence.any()
                mark = false
                return r
            }

        override var disabled: Boolean
            get() = true
            set(_) {}
    }
}

class ItemStackElement(private val slot: Slot, pos: Vec2d, override var selected: Boolean, private val filter: (iss: ItemStack) -> Boolean) :
        NeoIconLabelElement(icon = ItemIcon { slot.stack }, pos = pos) {

    init {
        onClick { _, button ->
            if (button == MouseButton.LEFT)
                if (tlParent is NeoGui<*>)
                    (tlParent as NeoGui<*>).openGui(PopupItem(label, itemStack.itemDesc(), if (mc.gameSettings.advancedItemTooltips) ForgeRegistries.ITEMS.getKey(itemStack.item).toString() else "")) += {
                        when (it){
                            PopupItem.Result.EQUIP -> handleEquip()
                            PopupItem.Result.TRADE -> handleTrade()
                            PopupItem.Result.DROP -> handleDrop()
                            PopupItem.Result.USE -> handleUse()
                            else -> {}
                        }
                        selected = false
                    }
            true
        }
        //itemStack.getTooltip(mc.player, if (mc.gameSettings.advancedItemTooltips) ITooltipFlag.TooltipFlags.ADVANCED else ITooltipFlag.TooltipFlags.NORMAL)
    }

    private fun handleEquip(){
        if (BaseFilters.EQUIPMENT(itemStack)){
            val equipSlot = (itemStack.item as ItemArmor).getEquipmentSlot(itemStack)?: (itemStack.item as ItemArmor).equipmentSlot
            val otherSlot =
                    when (equipSlot){
                        EntityEquipmentSlot.HEAD -> 5
                        EntityEquipmentSlot.CHEST -> 6
                        EntityEquipmentSlot.LEGS -> 7
                        EntityEquipmentSlot.FEET -> 8
                        //Sanity Check
                        else -> checkArmorSlots()
                    }
            val other = mc.player.inventoryContainer.getSlot(otherSlot).stack
            SAOCore.LOGGER.info("$slotID Item Slot -> $otherSlot Other Slot")
            mc.player.inventoryContainer.inventorySlots.forEach {
                if (it.hasStack)
                    SAOCore.LOGGER.info("${it.slotNumber} - ${it.stack}")
            }
            if (other.isNotEmpty) {
                val otherLabel = I18n.format("saoui.formatItem", other.getTooltip(mc.player, ITooltipFlag.TooltipFlags.NORMAL)[0])
                (tlParent as NeoGui<*>).openGui(PopupYesNo("$otherLabel -> $label", compare(other, 0), "Are you sure you want to replace this?")) += {
                    if (it == PopupYesNo.Result.YES) {
                        swapItems(otherSlot)
                    }
                }
            }
            else
                swapItems(otherSlot)
        }
        else if (BaseFilters.SHIELDS(itemStack)){
            val other = mc.player.inventoryContainer.getSlot(45).stack
            if (other.isNotEmpty) {
                val otherLabel = I18n.format("saoui.formatItem", other.getTooltip(mc.player, ITooltipFlag.TooltipFlags.NORMAL)[0])
                (tlParent as NeoGui<*>).openGui(PopupYesNo("$otherLabel -> $label", compare(other, 0), "Are you sure you want to replace this?")) += {
                    if (it == PopupYesNo.Result.YES) {
                        swapItems(45)
                    }
                }
            }
            else
                swapItems(45)

        }
        else
            (tlParent as NeoGui<*>).openGui(PopupHotbarSelection(label, "Select a slot", "")) += {
                if (it != PopupHotbarSelection.Result.CANCEL)
                    if (it.slot != slotID)
                        swapItems(it.slot)
            }
    }

    private fun handleTrade(){

    }

    private fun handleDrop(){
        (tlParent as NeoGui<*>).openGui(PopupYesNo(label, "Are you sure you want to discard this item?", "")) += {
            if (it == PopupYesNo.Result.YES) {
                throwItem()
            }
        }
    }

    private fun handleUse(){
    }

    fun swapItems(otherSlot: Int){
        if (mc.player.inventory.isItemValidForSlot(otherSlot, itemStack)) {
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, otherSlot, 0, ClickType.PICKUP, mc.player)
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slotID, 0, ClickType.PICKUP, mc.player)
            mc.playerController.windowClick(mc.player.inventoryContainer.windowId, otherSlot, 0, ClickType.PICKUP, mc.player)
        }
    }

    fun throwItem(){
        mc.playerController.windowClick(mc.player.inventoryContainer.windowId, slotID, 0, ClickType.THROW, mc.player)
    }

    /**
     * TODO Finish this
     * Compares two items together
     * Types:   0 - Armor
     *          1 - Weapons
     *          2 - Tools
     *          3 - Food
     *          4 - Misc
     */
    fun compare(other: ItemStack, type: Int): List<String>{
        val stringBuilder = mutableListOf<String>()
        when (type){
            0 -> {
                val desc = itemStack.itemDesc()
                other.itemDesc().forEachIndexed { index, s ->
                    if (index <= 3)
                        stringBuilder.add("$s -> ${desc[index]}")
                }

            }
            else -> {

            }
        }
        return stringBuilder
    }

    fun checkArmorSlots(): Int {
        if (mc.player.inventory.isItemValidForSlot(37, itemStack)) {
            return 37
        }
        else if (mc.player.inventory.isItemValidForSlot(38, itemStack)) {
            return 38
        }
        else if (mc.player.inventory.isItemValidForSlot(39, itemStack)) {
            return 39
        }
        else if (mc.player.inventory.isItemValidForSlot(40, itemStack)) {
            return 40
        }

        else return findFreeHotbarSlot()
    }

    fun findFreeHotbarSlot(): Int {
        for (i in 4..8){
            if (mc.player.inventory.get(i).isEmpty)
                return i
        }
        return 8
    }

    private val itemStack
        get() = with(slot.stack) {
            return@with if (filter(this)) this
            else ItemStack.EMPTY
        }

    private val slotID
        get() = slot.slotNumber

    override val valid: Boolean
        get() = itemStack.isNotEmpty

    override val label: String
        get() = if (itemStack.isNotEmpty) {
            if (itemStack.count > 1) I18n.format("saoui.formatItems", itemStack.getTooltip(mc.player, ITooltipFlag.TooltipFlags.NORMAL)[0], itemStack.count)
            else I18n.format("saoui.formatItem", itemStack.getTooltip(mc.player, ITooltipFlag.TooltipFlags.NORMAL)[0])
        } else I18n.format("gui.empty")

}

class ItemIcon(private val itemStack: () -> ItemStack) : IIcon {
    private val itemRenderer by lazy { Minecraft.getMinecraft().renderItem }

    override fun glDraw(x: Int, y: Int, z: Float) {
        val f = itemStack().animationsToGo.toFloat()/* - partialTicks*/
        RenderHelper.disableStandardItemLighting()
        RenderHelper.enableGUIStandardItemLighting()

        itemRenderer.zLevel += z

        if (f > 0.0f) {
            GLCore.pushMatrix()
            val f1 = 1.0f + f / 5.0f
            GLCore.translate((x + 8).toFloat(), (y + 12).toFloat(), z)
            GLCore.scale(1.0f / f1, (f1 + 1.0f) / 2.0f, z.plus(1))
            GLCore.translate((-(x + 8)).toFloat(), (-(y + 12)).toFloat(), z)
        }

        RenderHelper.enableGUIStandardItemLighting()
        itemRenderer.renderItemAndEffectIntoGUI(itemStack(), x, y)
        GLCore.depth(false)

        itemRenderer.zLevel -= z

        if (f > 0.0f) GLCore.popMatrix()

//        itemRenderer.renderItemOverlays(fontRenderer, itemStack, x, y)
    }
}

fun ItemStack.itemDesc(): List<String> {
    val stringBuilder = mutableListOf<String>()
    val desc = getTooltip(mc.player, ITooltipFlag.TooltipFlags.NORMAL)
    desc.removeAt(0)
    if (item is ItemTool){
        if (toolClasses.isNotEmpty()) {
            toolClasses.forEachIndexed { index, s ->
                stringBuilder.add(I18n.format("itemDesc.type", I18n.format("itemDesc.tool")))
                if (toolClasses.size > 1) {
                    if (index == 0)
                        stringBuilder.add(I18n.format("itemDesc.toolClasses", s.capitalize(), item.getHarvestLevel(this, s, mc.player, null)))
                    else
                        stringBuilder.add(I18n.format("itemDesc.toolClassSpace", s.capitalize(), item.getHarvestLevel(this, s, mc.player, null)))
                } else
                    stringBuilder.add(I18n.format("itemDesc.toolClass", s.capitalize(), item.getHarvestLevel(this, s, mc.player, null)))
            }
        }

        if (isItemEnchantable)
            stringBuilder.add(I18n.format("itemDesc.enchantability", (item as ItemTool).itemEnchantability))
        else
            stringBuilder.add(I18n.format("itemDesc.enchantable", isItemEnchantable.toString().capitalize()))
        stringBuilder.add(I18n.format("itemDesc.repairable",  (item as ItemTool).isRepairable.toString().capitalize()))
    }

    else if (toolClasses.isNotEmpty()) {
        toolClasses.forEachIndexed { index, s ->
            stringBuilder.add(I18n.format("itemDesc.type", I18n.format("itemDesc.tool")))
            if (toolClasses.size > 1) {
                if (index == 0)
                    stringBuilder.add(I18n.format("itemDesc.toolClasses", s.capitalize(), item.getHarvestLevel(this, s, mc.player, null)))
                else
                    stringBuilder.add(I18n.format("itemDesc.toolClassSpace", s.capitalize(), item.getHarvestLevel(this, s, mc.player, null)))
            } else
                stringBuilder.add(I18n.format("itemDesc.toolClass", s.capitalize(), item.getHarvestLevel(this, s, mc.player, null)))
            stringBuilder.add(I18n.format("itemDesc.enchantable", isItemEnchantable.toString().capitalize()))
        }

        //TODO Item tool
    }

    else if (item is ItemHoe){
        stringBuilder.add(I18n.format("itemDesc.type", I18n.format("itemDesc.tool")))
        stringBuilder.add(I18n.format("itemDesc.toolClass", I18n.format("itemDesc.hoe"), 0))
    }

    else if (item is ItemSword) {
        stringBuilder.add(I18n.format("itemDesc.type", I18n.format("itemDesc.sword")))
        if (isItemEnchantable)
            stringBuilder.add(I18n.format("itemDesc.enchantability", (item as ItemSword).itemEnchantability))
        else
            stringBuilder.add(I18n.format("itemDesc.enchantable", isItemEnchantable.toString().capitalize()))
    }

    else if (item is ItemBlock) {
        val block = (item as ItemBlock).block
        val state = block.getStateFromMeta(metadata)
        if (block.hasTileEntity(state))
            stringBuilder.add(I18n.format("itemDesc.type", I18n.format("itemDesc.tileEntity")))
        else
            stringBuilder.add(I18n.format("itemDesc.type", I18n.format("itemDesc.block")))
        stringBuilder.add(I18n.format("itemDesc.hardness", block.getBlockHardness(state, mc.player.world, mc.player.position)))
        stringBuilder.add(I18n.format("itemDesc.solid", state.material.isSolid.toString().capitalize()))
        if (state.material.isToolNotRequired)
            stringBuilder.add(I18n.format("itemDesc.toolRequired.false"))
        else {
            stringBuilder.add(I18n.format("itemDesc.toolRequired.true"))
            stringBuilder.add(I18n.format("itemDesc.mostEffective",
                    mc.player.inventory.asSequence().filter { it.canHarvestBlock(state) }.maxBy { it.getDestroySpeed(state) }?.displayName
                    ?: I18n.format("itemDesc.none")))

        }


        //TODO Block Desc
    }

    else if (item is ItemFood){
        stringBuilder.add(I18n.format("itemDesc.type", I18n.format("itemDesc.food")))
        stringBuilder.add(I18n.format("itemDesc.foodValue", (item as ItemFood).getHealAmount(this)))
        stringBuilder.add(I18n.format("itemDesc.saturationValue", (item as ItemFood).getSaturationModifier(this)))
        //TODO Food Desc
    }

    else if (item is ItemEnchantedBook){

        //TODO Enchant Desc
    }

    else if (item is ItemWrittenBook){

        //TODO Book Desk

    }
    else if (item is ItemArmor){
        stringBuilder.add(I18n.format("itemDesc.type", I18n.format("itemDesc.armor")))
        val equipSlot = (item as ItemArmor).getEquipmentSlot(this)?: (item as ItemArmor).equipmentSlot
        stringBuilder.add(I18n.format("itemDesc.slot", equipSlot.getName().capitalize()))
        if (isItemEnchantable)
            stringBuilder.add(I18n.format("itemDesc.enchantability", (item as ItemArmor).itemEnchantability))
        else
            stringBuilder.add(I18n.format("itemDesc.enchantable", isItemEnchantable.toString().capitalize()))
        if ((item as ItemArmor).isRepairable)
            stringBuilder.add(I18n.format("itemDesc.repairItem",  (item as ItemArmor).armorMaterial.repairItemStack.displayName))
        else
            stringBuilder.add(I18n.format("itemDesc.repairable",  (item as ItemArmor).isRepairable.toString().capitalize()))
        stringBuilder.add(I18n.format("itemDesc.toughness",  (item as ItemArmor).armorMaterial.toughness))
    }

    else if (hasTagCompound()){
        //TODO Potion Desc
        tagCompound?.get("Potion")?.let {
            val potion = ForgeRegistries.POTION_TYPES.getValue(ResourceLocation(it.toString()))
        }
    }
    else{
        stringBuilder.add(I18n.format("itemDesc.type", I18n.format("itemDesc.material")))
    }

    stringBuilder.addAll(desc)

    return stringBuilder
}

fun IInventory.asSequence(): Sequence<ItemStack> {
    return Sequence {
        object : Iterator<ItemStack> {
            private var index = 0
            private val size get() = this@asSequence.sizeInventory - 1

            override fun hasNext() = index < size

            override fun next(): ItemStack {
                if (!hasNext()) throw IndexOutOfBoundsException("index: $index, size: $size")
                return this@asSequence[index++]
            }
        }
    }
}

fun IInventory.asNumberedSequence(): Sequence<Pair<ItemStack, Int>> {
    return Sequence {
        object : Iterator<Pair<ItemStack, Int>> {
            private var index = 0
            private val size get() = this@asNumberedSequence.sizeInventory - 1

            override fun hasNext() = index < size

            override fun next(): Pair<ItemStack, Int> {
                if (!hasNext()) throw IndexOutOfBoundsException("index: $index, size: $size")
                return this@asNumberedSequence[index] to index++
            }
        }
    }
}

inline fun IInventory.forEach(body: (ItemStack) -> Unit) {
    (0 until sizeInventory).forEach { body(this[it]) }
}

operator fun IInventory.get(index: Int): ItemStack = getStackInSlot(index)