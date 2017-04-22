package thecodewarrior.circuitry.internals

import com.teamwizardry.librarianlib.features.saving.Savable
import com.teamwizardry.librarianlib.features.saving.SavableConstructorOrder
import net.minecraft.util.math.BlockPos

/**
 * Created by TheCodeWarrior
 */
@Savable
data class CircuitPos @SavableConstructorOrder("block", "port") constructor(val block: BlockPos, val port: Int)
