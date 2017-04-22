package thecodewarrior.circuitry.gui

import com.teamwizardry.librarianlib.features.gui.GuiComponent
import com.teamwizardry.librarianlib.features.gui.components.ComponentSprite
import com.teamwizardry.librarianlib.features.gui.components.ComponentVoid
import com.teamwizardry.librarianlib.features.guicontainer.ComponentSlot
import com.teamwizardry.librarianlib.features.guicontainer.GuiContainerBase
import com.teamwizardry.librarianlib.features.guicontainer.builtin.BaseLayouts
import com.teamwizardry.librarianlib.features.helpers.vec
import com.teamwizardry.librarianlib.features.sprite.Texture
import net.minecraft.util.ResourceLocation
import thecodewarrior.circuitry.CircuitryMod
import thecodewarrior.circuitry.gui.GuiContainerConfigureCombinator.Companion.in_glow

/**
 * Created by TheCodeWarrior
 */
class GuiContainerConstantCombinator(val container_: ContainerConstantCombinator) : GuiContainerBase(container_, 176, 170) {
    val tile = container_.combinator

    init {
        val compInv = ComponentSprite(inv, 0, 76)
        val compCard = ComponentSprite(card, 41, 0)
        mainComponents.add(compInv, compCard)

        val playerLayout = BaseLayouts.player(container_.playerWrapper)
        playerLayout.mainWrapper.pos = vec(8, 12)
        compInv.add(playerLayout.root)

        val gridRoot = ComponentVoid(12, 15)
        compCard.add(gridRoot)

        container_.configWrapper.all.forEachIndexed { i, slot ->
            val slot = ComponentSlot(slot, 0, 0)
            val render = ComponentSignalConfig(0, 0, {container_.signals[i]})

            val pos = vec((18 + 9) * (i % 3), (18 + 18) * (i / 3))
            slot.pos = pos
            render.pos = pos
            gridRoot.add(slot, render)
        }

        val selGrid = ComponentVoid(66, 34)
        compCard.add(selGrid)

        (0..5).forEach { i ->
            val x = 7 * (i % 3)
            val y = 7 * (i / 3)
            val button = ComponentVoid(x, y, 7, 7)
            val sprite = ComponentSprite(null, 1, 1, 5, 5)
            sprite.BUS.hook(GuiComponent.ComponentTickEvent::class.java) {
                if(container_.selected == i)
                    sprite.sprite = in_glow
                else
                    sprite.sprite = null
            }

            button.BUS.hook(GuiComponent.MouseClickEvent::class.java) {
                if(container_.selected != i)
                    container_.sendButton(-1, i)
            }
            button.add(sprite)
            selGrid.add(button)
        }

        val sideCard = SignalStrengthConfigTemplate(container_, -2, { container_.signals[container_.selected] }, { true })
        sideCard.root.pos = vec(94, 31)
        compCard.add(sideCard.root)

        mainComponents.BUS.hook(GuiComponent.ComponentTickEvent::class.java) {
            if(sideCard.buttonId != container_.selected) {
                sideCard.buttonId = container_.selected
                sideCard.update()
            }
        }
    }

    companion object {
        val positions = IntArray(60)

        val TEXTURE = Texture(ResourceLocation(CircuitryMod.MODID, "textures/gui/constant_combinator_config.png"))
        val card = TEXTURE.getSprite("card", 94, 76)
        val inv = TEXTURE.getSprite("inv", 176, 94)
        val selected_square = TEXTURE.getSprite("selected_square", 5, 5)
    }

}
