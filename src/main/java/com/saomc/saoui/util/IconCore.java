package com.saomc.saoui.util;

import com.saomc.saoui.GLCore;
import com.saomc.saoui.api.screens.IIcon;
import com.saomc.saoui.config.OptionCore;
import com.saomc.saoui.resources.StringNames;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public enum IconCore implements IIcon {

    NONE,
    OPTION,
    HELP,
    LOGOUT,
    CANCEL,
    CONFIRM,
    SETTINGS,
    NAVIGATION,
    MESSAGE,
    SOCIAL,
    PROFILE,
    EQUIPMENT,
    ITEMS,
    SKILLS,
    GUILD,
    PARTY,
    FRIEND,
    CREATE,
    INVITE,
    QUEST,
    FIELD_MAP,
    DUNGEON_MAP,
    ARMOR,
    ACCESSORY,
    MESSAGE_RECEIVED,
    CRAFTING,
    SPRINTING,
    SNEAKING;

    private static final int SRC_SIZE = 16;

    private int getSrcX() {
        return (index() % 16) * SRC_SIZE;
    }

    private int getSrcY() { return (index() / 16) * SRC_SIZE; }

    private int index() {
        return (ordinal() - 1);
    }

    @Override
    public final void glDraw(int x, int y) {
        if (index() >= 0) {
            GLCore.glColor(1f, 1f, 1f, 1f);
            GLCore.glBlend(true);
            GLCore.glBindTexture(OptionCore.SAO_UI.isEnabled() ? StringNames.icons : StringNames.iconsCustom);
            GLCore.glTexturedRect(x, y, getSrcX(), getSrcY(), SRC_SIZE, SRC_SIZE);
            GLCore.glBlend(false);
        }
    }

    @Override
    public void glDrawUnsafe(int x, int y) {
        GLCore.glTexturedRect(x, y, getSrcX(), getSrcY(), SRC_SIZE, SRC_SIZE);
    }

}
