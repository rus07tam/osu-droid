package com.reco1l.osu.hud.elements

import com.edlplan.framework.easing.*
import com.reco1l.andengine.*
import com.reco1l.andengine.Anchor
import com.reco1l.andengine.shape.*
import com.reco1l.andengine.sprite.*
import com.reco1l.andengine.texture.*
import com.reco1l.framework.*
import com.reco1l.osu.hud.HUDElement
import org.anddev.andengine.opengl.texture.region.*
import ru.nsu.ccfit.zuev.osu.*
import ru.nsu.ccfit.zuev.osu.game.GameScene
import ru.nsu.ccfit.zuev.osu.scoring.*
import ru.nsu.ccfit.zuev.skins.*

class HUDHealthBar : HUDElement() {

    private val fill: AnimatedSprite
    private val fillClear: Box

    private val marker: ExtendedSprite
    private val explode: ExtendedSprite

    private val markerNormalTexture: TextureRegion?
    private val markerDangerTexture: TextureRegion?
    private val markerSuperDangerTexture: TextureRegion?

    private val isNewStyle: Boolean


    private var lastHP = 0f


    init {

        val backgroundTexture = ResourceManager.getInstance().getTexture("scorebar-bg")
        val markerTexture = ResourceManager.getInstance().getTextureIfLoaded("scorebar-marker")

        // the marker lookup to decide which display style must be performed on the source of the bg, which is the most common element.
        isNewStyle = backgroundTexture !is BlankTextureRegion && markerTexture != null

        // background implementation is the same for both versions.
        attachChild(ExtendedSprite().apply { textureRegion = backgroundTexture })

        fillClear = Box()
        fillClear.origin = Anchor.TopRight
        fillClear.depthInfo = DepthInfo.Clear
        fillClear.alpha = 0f
        attachChild(fillClear)

        fill = AnimatedSprite("scorebar-colour", true, OsuSkin.get().animationFramerate)
        fill.depthInfo = DepthInfo.Default
        fill.autoSizeAxes = Axes.None // Preserve the first frame width.
        attachChild(fill)

        marker = ExtendedSprite()
        marker.origin = Anchor.Center
        marker.blendInfo = BlendInfo.Additive
        attachChild(marker)

        explode = ExtendedSprite()
        explode.origin = Anchor.Center
        explode.blendInfo = BlendInfo.Additive
        explode.alpha = 0f
        attachChild(explode)

        if (isNewStyle) {
            fill.setPosition(7.5f * 1.6f, 7.8f * 1.6f)

            marker.textureRegion = markerTexture
            explode.textureRegion = markerTexture

            markerNormalTexture = null
            markerDangerTexture = null
            markerSuperDangerTexture = null
        } else {
            fill.setPosition(3f * 1.6f, 10f * 1.6f)

            markerNormalTexture = ResourceManager.getInstance().getTextureIfLoaded("scorebar-ki")
            markerDangerTexture = ResourceManager.getInstance().getTextureIfLoaded("scorebar-kidanger")
            markerSuperDangerTexture = ResourceManager.getInstance().getTextureIfLoaded("scorebar-kidanger2")

            marker.textureRegion = markerNormalTexture
            explode.textureRegion = markerNormalTexture
        }

        fillClear.width = 0f
        fillClear.height = fill.drawHeight
        fillClear.setPosition(fill.x + fill.drawWidth, fill.y)

        onMeasureContentSize()
    }


    override fun onGameplayUpdate(game: GameScene, secondsElapsed: Float) {
        val hp = game.stat.hp

        fillClear.width = Interpolation.floatAt(secondsElapsed.coerceIn(0f, 0.2f), fillClear.drawWidth, (1f - hp) * fill.drawWidth, 0f, 0.2f, Easing.OutQuint)

        marker.x = fill.x + fill.drawWidth - fillClear.drawWidth
        marker.y = fill.y + (if (isNewStyle) fill.drawHeight / 2 else 0f)

        explode.setPosition(marker)

        if (hp > lastHP) {
            bulge()
        }

        lastHP = hp

        if (isNewStyle) {

            val color = getFillColor(hp)

            fill.color = color
            marker.color = color
            marker.blendInfo = if (hp < EPIC_CUTOFF) BlendInfo.Inherit else BlendInfo.Additive

        } else {

            marker.textureRegion = when {
                hp < 0.2f -> markerSuperDangerTexture
                hp < EPIC_CUTOFF -> markerDangerTexture
                else -> markerNormalTexture
            }

        }
    }

    override fun onNoteHit(statistics: StatisticV2) {
        flash(statistics.hp)
    }

    override fun onBreakStateChange(isBreak: Boolean) {
        isChildrenVisible = !isBreak
    }


    private fun flash(hp: Float) {

        val isEpic = hp >= EPIC_CUTOFF

        bulge()

        explode.clearEntityModifiers()
        explode.blendInfo = if (isEpic) BlendInfo.Additive else BlendInfo.Inherit
        explode.alpha = 1f
        explode.setScale(1f)

        explode.scaleTo(if (isEpic) 2f else 1.6f, 0.12f)
        explode.fadeOut(0.12f)
    }

    private fun bulge() {
        marker.clearEntityModifiers()
        marker.setScale(1.4f)
        marker.scaleTo(1f, 0.2f, Easing.Out)
    }

    private fun getFillColor(hp: Float) = when {

        hp < 0.2f -> Colors.interpolateNonLinear(0.2f - hp, ColorARGB.Black, ColorARGB.Red, 0f, 0.2f)
        hp < EPIC_CUTOFF -> Colors.interpolateNonLinear(0.5f - hp, ColorARGB.White, ColorARGB.Black, 0f, 0.5f)

        else -> ColorARGB.White
    }

    companion object {

        const val EPIC_CUTOFF = 0.5f

    }

}