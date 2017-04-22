package thecodewarrior.circuitry.gui

import com.teamwizardry.librarianlib.features.gui.GuiComponent
import com.teamwizardry.librarianlib.features.gui.components.ComponentSprite
import com.teamwizardry.librarianlib.features.gui.components.ComponentVoid
import com.teamwizardry.librarianlib.features.gui.mixin.DragMixin
import com.teamwizardry.librarianlib.features.guicontainer.ComponentSlot
import com.teamwizardry.librarianlib.features.guicontainer.GuiContainerBase
import com.teamwizardry.librarianlib.features.guicontainer.builtin.BaseLayouts
import com.teamwizardry.librarianlib.features.helpers.vec
import com.teamwizardry.librarianlib.features.kotlin.clamp
import com.teamwizardry.librarianlib.features.kotlin.withRealDefault
import com.teamwizardry.librarianlib.features.kotlin.withX
import com.teamwizardry.librarianlib.features.math.Vec2d
import com.teamwizardry.librarianlib.features.sprite.Sprite
import com.teamwizardry.librarianlib.features.sprite.Texture
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.MathHelper
import org.lwjgl.opengl.GL11
import thecodewarrior.circuitry.CircuitryMod
import thecodewarrior.circuitry.gui.GuiContainerConfigureCombinator.Companion.numcard
import thecodewarrior.circuitry.gui.GuiContainerConfigureCombinator.Companion.slider
import thecodewarrior.circuitry.internals.SignalType
import thecodewarrior.circuitry.tile.SignalConfig
import thecodewarrior.circuitry.tile.TileDeciderCombinator

/**
 * Created by TheCodeWarrior
 */
class GuiContainerConfigureCombinator(val container_: ContainerConfigureCombinator) : GuiContainerBase(container_, 176, 170) {
    val tile = container_.combinator

    init {
        val compInv = ComponentSprite(inv, 0, 76)
        val compCard = ComponentSprite(card, 41, 0)
        mainComponents.add(compInv, compCard)

        val playerLayout = BaseLayouts.player(container_.playerWrapper)
        playerLayout.mainWrapper.pos = vec(8, 12)
        compInv.add(playerLayout.root)

        val leftSlot = ComponentSlot(container_.configWrapper.left, 12, 24)
        val rightSlot = ComponentSlot(container_.configWrapper.right, 66, 24)
        val outSlot = ComponentSlot(container_.configWrapper.out, 39, 33)
        compCard.add(leftSlot, rightSlot, outSlot)

        val leftRender = ComponentSignalConfig(12, 24, {container_.left})
        val rightRender = ComponentSignalConfig(66, 24, {container_.right})
        val outRender = ComponentSignalConfig(39, 33, {container_.out})
        compCard.add(leftRender, rightRender, outRender)

        val operator = createOperator()
        operator.pos = vec(43.5, 19.5)
        compCard.add(operator)

        val sideCard = SignalStrengthConfigTemplate(container_, 1, {container_.right}, {container_.right is SignalConfig.Number})
        sideCard.root.pos = vec(94, 22)
        compCard.add(sideCard.root)

        if(container_.combinator is TileDeciderCombinator) {
            val in_1Component = ComponentSprite(in_1, 20, 32)
            val inSprite = ComponentSprite(null, 13, 7, 3, 3)
            val oneSprite = ComponentSprite(null, 38, 7, 3, 3)

            inSprite.BUS.hook(GuiComponent.ComponentTickEvent::class.java) {
                if(!container_.outModeOne)
                    inSprite.sprite = in_glow
                else
                    inSprite.sprite = null
            }
            oneSprite.BUS.hook(GuiComponent.ComponentTickEvent::class.java) {
                if(container_.outModeOne)
                    oneSprite.sprite = in_glow
                else
                    oneSprite.sprite = null
            }

            val inButton = ComponentVoid(9, 0, 9, 18)
            val oneButton = ComponentVoid(36, 0, 9, 18)
            inButton.BUS.hook(GuiComponent.MouseClickEvent::class.java) {
                if(container_.outModeOne)
                    container_.sendButton(2, 0)
            }

            oneButton.BUS.hook(GuiComponent.MouseClickEvent::class.java) {
                if(!container_.outModeOne)
                    container_.sendButton(2, 1)
            }

            in_1Component.add(inSprite, oneSprite, inButton, oneButton)
            compCard.add(in_1Component)
        }
    }

    val sprites = mutableMapOf<ResourceLocation, Sprite>().withRealDefault { Sprite(ResourceLocation(it.resourceDomain, "textures/" + it.resourcePath + ".png")) }

    fun createOperator(): ComponentSprite {
        val comp = ComponentSprite(null, 0, 0, 7, 7)
        comp.BUS.hook(GuiComponent.ComponentTickEvent::class.java) {
            comp.sprite = sprites[container_.operation.inventoryIcon]
        }
        comp.BUS.hook(GuiComponent.MouseClickEvent::class.java) {
            container_.sendButton(0)
        }
        return comp
    }


    companion object {
        val positions = IntArray(60)

        val TEXTURE = Texture(ResourceLocation(CircuitryMod.MODID, "textures/gui/combinator_config.png"))
        val card = TEXTURE.getSprite("card", 94, 76)
        val inv = TEXTURE.getSprite("inv", 176, 94)
        val numcard = TEXTURE.getSprite("numcard", 72, 20)
        val slider = TEXTURE.getSprite("slider", 5, 8)
        val in_1 = TEXTURE.getSprite("in_1", 54, 44)
        val in_glow = TEXTURE.getSprite("in_glow", 3, 3)
    }

}

class SignalStrengthConfigTemplate(val container: IContainerButtonHandler, var buttonId: Int, var signal: () -> SignalConfig, enabled: () -> Boolean) {
    val root = ComponentVoid(0, 0)
    val slideTrack = ComponentVoid(5, -7, 32, 0)
    val slide = ComponentSprite(slider, 0, 0)
    val textInput = ComponentTextInput(7, 6, 61, 10)
    val drag: DragMixin<ComponentSprite>

    private var updating = false

    init {
        root.add(ComponentSprite(numcard, 0, 0))

        root.add(slideTrack)
        slideTrack.add(slide)

        drag = DragMixin(slide) {
            vec(it.x.clamp(0.0, slideTrack.size.x), 0)
        }

        root.add(textInput)

        textInput.textField.setValidator(this::validate)

        root.BUS.hook(GuiComponent.ComponentTickEvent::class.java) {
            val wasVisible = root.isVisible
            root.isVisible = enabled()
            if(wasVisible && !root.isVisible) {
                drag.mouseDown = null
            }
        }

        slide.BUS.hook(DragMixin.DragMoveEvent::class.java) {
            val x = it.newPos.x-1
            val num = if(x < 0) 0 else Math.pow(2.0, x).toInt()
            updateNum(num)
        }

        update()
    }

    fun update() {
        val sig = signal()
        if(sig is SignalConfig.Number) {
            updateField(sig.num)
            updateSlide(sig.num)
        } else if(sig is SignalConfig.Signal) {
            updateField(sig.count)
            updateSlide(sig.count)
        } else {
            updateField(0)
            updateSlide(0)
        }
    }

    private fun validate(it: String?): Boolean {
            if(updating) return true
            if(it == null)
                return true
            if(it == "") {
                updateNum(0)
                return true
            }
            val t = if(it.endsWith('k', true) && it.endsWith('m', true) && it.endsWith('g', true)) {
                it.substring(0, it.length-1)
            } else it
            try {
                val i = Integer.parseInt(t)
                val transformed =
                        if(it.endsWith('k', true)) i * 1_000
                        else if(it.endsWith('m', true)) i * 1_000_000
                        else if(it.endsWith('g', true)) i * 1_000_000_000
                        else i
                updateNum(transformed)
                return true
            } catch (e: NumberFormatException) {
                return false
            }
        }

    private fun updateSlide(num: Int) {
        val x = if(num < 2) 0 else MathHelper.log2(num).clamp(0, 31)+1
        slide.pos = slide.pos.withX(x)
    }

    private fun updateField(num: Int) {
        var s = num.toString()
        var postFix = 0
        (0..2).forEach {
            if(s.endsWith("000")) {
                s = s.substring(0, s.length - 3)
                postFix++
            }
        }
        val text = s + arrayOf("", "k", "m", "b")[postFix]
        updating = true
        textInput.textField.text = text
        updating = false
    }

    private fun updateNum(num: Int) {
        container.sendButton(buttonId, num)
        if(drag.mouseDown == null) {
            updateSlide(num)
        }
        if(!textInput.textField.isFocused) {
            updateField(num)
        }
    }
}

class ComponentSignalConfig(posX: Int, posY: Int, val config: () -> SignalConfig) : GuiComponent<ComponentSignalConfig>(posX, posY, 16, 16) {
    override fun drawComponent(mousePos: Vec2d, partialTicks: Float) {
        val conf = config()
        when(conf) {
            is SignalConfig.Special -> {
                drawIcon(conf.type.icon, this.pos.xi, this.pos.yi)
            }
            is SignalConfig.Number -> {
                drawIcon(numberIcon, this.pos.xi, this.pos.yi)
            }
            is SignalConfig.Signal -> {
                val type = conf.type as SignalType<Any>
                type.render(type.fromInt(conf.key), this.pos.xi, this.pos.yi)
            }
        }
    }

    protected fun drawIcon(texture: ResourceLocation, x: Int, y: Int) {
        val tess = Tessellator.getInstance()
        val vb = tess.buffer

        val xMin = x.toDouble()
        val yMin = y.toDouble()
        val xMax = xMin + 16
        val yMax = yMin + 16

        Minecraft.getMinecraft().renderEngine.bindTexture(texture)

        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX)
        vb.pos(xMin, yMax, 0.0).tex(0.0, 1.0).endVertex()
        vb.pos(xMax, yMax, 0.0).tex(1.0, 1.0).endVertex()
        vb.pos(xMax, yMin, 0.0).tex(1.0, 0.0).endVertex()
        vb.pos(xMin, yMin, 0.0).tex(0.0, 0.0).endVertex()
        tess.draw()
    }

    companion object {
        val numberIcon = ResourceLocation(CircuitryMod.MODID, "textures/signal/number.png")
    }
}
