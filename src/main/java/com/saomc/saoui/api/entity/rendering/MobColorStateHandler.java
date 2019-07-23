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

package com.saomc.saoui.api.entity.rendering;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityOwnable;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.passive.IAnimals;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.ref.WeakReference;
import java.util.Objects;

import static com.saomc.saoui.api.entity.rendering.ColorState.*;

/**
 * Part of saoui
 * <p>
 * Default implementation for mobs.
 * This will be the most common implementation for mobs and NPCs.
 *
 * @author Bluexin
 */
public class MobColorStateHandler implements IColorStateHandler {

    private final WeakReference<EntityLivingBase> theEnt;

    /**
     * Caches value when it can (ie the value will never change again)
     */
    private ColorState cached = null;

    MobColorStateHandler(EntityLivingBase entity) {
        this.theEnt = new WeakReference<>(entity);
    }

    @SideOnly(Side.CLIENT)
    private ColorState getColor() {
        if (this.cached != null) return cached;
        EntityLivingBase entity = theEnt.get();
        if (entity == null) return cached = INVALID;
        if (!entity.isNonBoss()) return cached = BOSS;
        if (entity instanceof EntityWolf && ((EntityWolf) entity).isAngry()) return KILLER;
        if (entity instanceof EntityTameable && ((EntityTameable) entity).isTamed())
            return Objects.equals(((EntityTameable) entity).getOwner(), Minecraft.getMinecraft().player) ? INNOCENT : VIOLENT;
        if (entity instanceof IMob)
            return entity.canEntityBeSeen(Minecraft.getMinecraft().player) ? KILLER : VIOLENT;
        if (entity instanceof IAnimals) return cached = INNOCENT;
        if (entity instanceof IEntityOwnable) return cached = VIOLENT;
        return cached = INVALID;
    }

    /**
     * @return the color state the entity should be showing.
     */
    @Override
    @SideOnly(Side.CLIENT)
    public ColorState getColorState() {
        return getColor();
    }
}
