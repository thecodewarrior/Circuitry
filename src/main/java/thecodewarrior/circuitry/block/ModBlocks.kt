package thecodewarrior.circuitry.block

import net.minecraft.block.state.IBlockState
import net.minecraft.world.World
import thecodewarrior.circuitry.tile.TileArithmeticCombinator
import thecodewarrior.circuitry.tile.TileConstantCombinator
import thecodewarrior.circuitry.tile.TileDeciderCombinator

/**
 * Created by TheCodeWarrior
 */
object ModBlocks {
    val constant_combinator = object : BlockCombinator("constant") {
        override fun createTileEntity(world: World, state: IBlockState) = TileConstantCombinator()
    }
    val arithmetic_combinator = object : BlockCombinator("arithmetic") {
        override fun createTileEntity(world: World, state: IBlockState) = TileArithmeticCombinator()
    }
    val decider_combinator = object : BlockCombinator("decider") {
        override fun createTileEntity(world: World, state: IBlockState) = TileDeciderCombinator()
    }
    val wire_tower = BlockWireTower()
    val redstone_output_off = BlockRedstoneOutput(false)
    val redstone_output_on = BlockRedstoneOutput(true)
}
