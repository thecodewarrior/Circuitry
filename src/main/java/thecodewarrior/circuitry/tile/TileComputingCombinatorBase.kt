package thecodewarrior.circuitry.tile

import com.teamwizardry.librarianlib.features.container.GuiHandler
import com.teamwizardry.librarianlib.features.helpers.vec
import com.teamwizardry.librarianlib.features.kotlin.withX
import com.teamwizardry.librarianlib.features.saving.Save
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.EnumFacing
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import thecodewarrior.circuitry.gui.ContainerConfigureCombinator
import thecodewarrior.circuitry.internals.NetworkColor

/**
 * Created by TheCodeWarrior
 */
abstract class TileComputingCombinatorBase : TileCombinator() {
    override val portCount: Int = 2

    @Save var right: SignalConfig = SignalConfig.Number(0)
    @Save var left: SignalConfig = SignalConfig.Special(EnumSpecialSignal.NULL)
    @Save var out: SignalConfig = SignalConfig.Special(EnumSpecialSignal.NULL)

    abstract fun getAllowedSpecialLeft(): Array<EnumSpecialSignal>
    abstract fun getAllowedSpecialOut(left: SignalConfig): Array<EnumSpecialSignal>
    abstract fun possibleOperators(): Array<out IOperator>
    abstract var operatorConfig: IOperator
    abstract fun getIcon(): ResourceLocation

    override val bounds: AxisAlignedBB = AxisAlignedBB(-4.0, 0.0, -7.5, 4.0, 6.0, 7.5)

    override fun getRenderPos(world: World, pos: BlockPos, color: NetworkColor, port: Int): Vec3d {
        var p: Vec3d
        if(port == 1) {
            p = vec(
                    3,
                    4,
                    7.5
            )
        } else {
            p = vec(
                    2.5,
                    4,
                    -7.5
            )
        }
        if(color == NetworkColor.GREEN) p = p.withX(-p.xCoord)
        return transform(p)
    }

    override fun getPortTransformed(sideClicked: EnumFacing, vec: Vec3d): Int {
        if(vec.zCoord > 0)
            return 1
        if(vec.zCoord < 0)
            return 0
        return 0
    }

    override fun openGui(player: EntityPlayer) {
        GuiHandler.open(ContainerConfigureCombinator.NAME, player, pos)
    }
}

interface IOperator {
    val inventoryIcon: ResourceLocation
}
