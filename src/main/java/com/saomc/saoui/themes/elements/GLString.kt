package com.saomc.saoui.themes.elements

import com.saomc.saoui.GLCore
import com.saomc.saoui.api.themes.IHudDrawContext
import com.saomc.saoui.themes.util.StringExpressionWrapper
import javax.xml.bind.annotation.XmlRootElement

/**
 * Part of saoui by Bluexin.

 * @author Bluexin
 */
@XmlRootElement
open class GLString : GLRectangle() {

    protected lateinit var text: StringExpressionWrapper
    private val shadow = true

    override fun draw(ctx: IHudDrawContext) {
        if (!(enabled?.execute(ctx) ?: true)) return
        val p = this.parent.get()
        val x = (this.x?.execute(ctx) ?: 0.0) + (p?.getX(ctx) ?: 0.0)
        val y = (this.y?.execute(ctx) ?: 0.0) + (p?.getY(ctx) ?: 0.0) + ((this.h?.execute(ctx) ?: 0.0) - ctx.fontRenderer.FONT_HEIGHT) / 2.0

        GLCore.glString(ctx.fontRenderer, this.text.execute(ctx), x.toInt(), y.toInt(), rgba?.execute(ctx) ?: 0xFFFFFFFF.toInt(), shadow)
    }
}
