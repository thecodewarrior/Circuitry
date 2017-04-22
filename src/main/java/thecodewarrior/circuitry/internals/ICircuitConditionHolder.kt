package thecodewarrior.circuitry.internals

import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

/**
 * Created by TheCodeWarrior
 */
interface ICircuitConditionHolder : ICircuitHandler {
    val conditionCount: Int

    fun shouldRefreshConditions(world: World, pos: BlockPos, oldState: IBlockState): Boolean {
        return world.getBlockState(pos).block != oldState.block
    }
}
