package com.saomc.saoui.api.screens;

/**
 * Part of saoui
 * Icons are used to display any form on screen.
 * Implement this to add your own custom icons.
 *
 * @author Bluexin
 */
@FunctionalInterface
public interface IIcon {

    /**
     * Called when this icon needs to be drawn on screen, at given x and y coordinates.
     * The given coordinates should be the upper-left corner of this icon.
     *
     * @param x x-coordinate to start drawing from
     * @param y y-coordinate to start drawing from
     */
    void glDraw(int x, int y);

    default void glDrawUnsafe(int x, int y) {
        this.glDraw(x, y);
    }

}
