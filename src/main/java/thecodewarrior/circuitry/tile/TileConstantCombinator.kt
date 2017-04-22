package thecodewarrior.circuitry.tile

import com.teamwizardry.librarianlib.features.autoregister.TileRegister
import com.teamwizardry.librarianlib.features.container.GuiHandler
import com.teamwizardry.librarianlib.features.helpers.vec
import com.teamwizardry.librarianlib.features.saving.Save
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import thecodewarrior.circuitry.gui.ContainerConstantCombinator
import thecodewarrior.circuitry.internals.NetworkColor
import thecodewarrior.circuitry.internals.SigSetBuilder

/**
 * Created by TheCodeWarrior
 */
@TileRegister("combinator_constant")
class TileConstantCombinator : TileCombinator() {
    override val portCount: Int = 1

    @Save val signals = Array<SignalConfig>(signalCount) {
        SignalConfig.Special(EnumSpecialSignal.NULL)
    }

    override fun getRenderPos(world: World, pos: BlockPos, color: NetworkColor, port: Int): Vec3d {
        var col = 2
        if(color == NetworkColor.GREEN)
            col *= -1
        return transform(vec(col, 8, 3))
    }

    override fun getPortTransformed(sideClicked: EnumFacing, vec: Vec3d): Int {
        return 0
    }

    override val bounds: AxisAlignedBB = AxisAlignedBB(-4.0, 0.0, -4.0, 4.0, 9.0, 4.0)

    override fun addOutput(world: World, pos: BlockPos, port: Int, color: NetworkColor, builder: SigSetBuilder) {
        signals.forEach {
            if(it is SignalConfig.Signal) {
                builder.addRaw(it.type, it.key, it.count)
            }
        }
    }

    override fun openGui(player: EntityPlayer) {
        GuiHandler.open(ContainerConstantCombinator.NAME, player, pos)
    }

    companion object {
        val signalCount = 6
    }
}
