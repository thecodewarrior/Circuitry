package thecodewarrior.circuitry

import net.minecraft.block.state.IBlockState
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.SoundCategory
import net.minecraft.util.SoundEvent
import net.minecraft.util.math.BlockPos
import net.minecraft.world.IWorldEventListener
import net.minecraft.world.World

/**
 * Created by TheCodeWarrior
 */
interface IDefaultedWorldEventListener : IWorldEventListener {
    override fun spawnParticle(p_190570_1_: Int, p_190570_2_: Boolean, p_190570_3_: Boolean, p_190570_4_: Double, p_190570_6_: Double, p_190570_8_: Double, p_190570_10_: Double, p_190570_12_: Double, p_190570_14_: Double, vararg p_190570_16_: Int) { }
    override fun playSoundToAllNearExcept(player: EntityPlayer?, soundIn: SoundEvent?, category: SoundCategory?, x: Double, y: Double, z: Double, volume: Float, pitch: Float) { }
    override fun onEntityAdded(entityIn: Entity?) { }
    override fun broadcastSound(soundID: Int, pos: BlockPos?, data: Int) { }
    override fun playEvent(player: EntityPlayer?, type: Int, blockPosIn: BlockPos?, data: Int) { }
    override fun markBlockRangeForRenderUpdate(x1: Int, y1: Int, z1: Int, x2: Int, y2: Int, z2: Int) { }
    override fun onEntityRemoved(entityIn: Entity?) { }
    override fun notifyLightSet(pos: BlockPos?) { }
    override fun spawnParticle(particleID: Int, ignoreRange: Boolean, xCoord: Double, yCoord: Double, zCoord: Double, xSpeed: Double, ySpeed: Double, zSpeed: Double, vararg parameters: Int) { }
    override fun notifyBlockUpdate(worldIn: World, pos: BlockPos, oldState: IBlockState, newState: IBlockState, flags: Int) { }
    override fun playRecord(soundIn: SoundEvent?, pos: BlockPos?) { }
    override fun sendBlockBreakProgress(breakerId: Int, pos: BlockPos?, progress: Int) { }
}
