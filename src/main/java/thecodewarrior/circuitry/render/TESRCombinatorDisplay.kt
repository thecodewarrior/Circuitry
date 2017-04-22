package thecodewarrior.circuitry.render

import com.teamwizardry.librarianlib.core.client.ClientTickHandler
import com.teamwizardry.librarianlib.features.helpers.vec
import com.teamwizardry.librarianlib.features.kotlin.color
import com.teamwizardry.librarianlib.features.kotlin.plus
import com.teamwizardry.librarianlib.features.kotlin.pos
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.VertexBuffer
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraftforge.client.model.animation.FastTESR
import thecodewarrior.circuitry.tile.TileComputingCombinatorBase
import java.awt.Color

/**
 * Created by TheCodeWarrior
 */
object TESRCombinatorDisplay : FastTESR<TileComputingCombinatorBase>() {
    override fun renderTileEntityFast(te: TileComputingCombinatorBase, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, vb: VertexBuffer) {
        val sprite = Minecraft.getMinecraft().textureMapBlocks.getAtlasSprite(te.getIcon().toString())
        val a = te.transform(vec(-3.5, 6.51, -3.5))
        val b = te.transform(vec( 3.5, 6.51, -3.5))
        val c = te.transform(vec( 3.5, 6.51,  3.5))
        val d = te.transform(vec(-3.5, 6.51,  3.5))

        val p = vec(x, y, z)

        val brightness = (15 shl 20) or (15 shl 4) // should be full brightness
        val skyLight = brightness shr 16 and 65535
        val blockLight = brightness and 65535

        val time = ClientTickHandler.total + (MathHelper.getPositionRandom(te.pos) and 0xFF)


        fun vert(vertex: Vec3d, u: Number, v: Number) {
            var realU = u.toDouble()
            var realV = v.toDouble()
            realU = sprite.getInterpolatedU(realU).toDouble()
            realV = sprite.getInterpolatedV(realV).toDouble()
            vb.pos(p + vertex).color(Color.WHITE).tex(realU, realV).lightmap(skyLight, blockLight).endVertex()
        }

        vert(a, 0, 0)
        vert(b, 16, 0)
        vert(c, 16, 16)
        vert(d, 0, 16)
    }

}

object Noise {
    val primes = intArrayOf(
            165_479, 57_467, 88_609,
            9_929, 178_559, 26_959,
            15_217, 239_753, 19_819
    )

    fun getBasicNoise(x_: Int, primeIndex: Int): Float {
        val x = Math.pow((x_ shl 13).toDouble(), x_.toDouble()).toInt() // bitwise shift to the left by 13 places then raised to n

        //& performs a bitwise multiplication (i.e. 0*0 =0, 1*0=0, 1*1=1
        //it makes this multiplication with the largest possible int
        //i.e. +111111.....1111
        return (1.0 - (x * (x * x * primes[primeIndex+2] + primes[primeIndex+0]) + primes[primeIndex+1] and Integer.MAX_VALUE) / 1073741824f) as Float

    }

    fun perlinNoise1D(x: Float, persistence: Float, octaves: Int): Float {
        var total = 0f
        val p = persistence
        val n = octaves - 1

        for (i in 0..n) {

            val frequency = Math.pow(2.0, i.toDouble()).toFloat()
            val amplitude = Math.pow(p.toDouble(), i.toDouble())
            total += (interpolatedNoise(x * frequency) * amplitude).toFloat()
        }

        return total.toInt().toFloat()

    }

    private fun interpolatedNoise(x: Float): Float {
        val integer_X = x.toInt()
        val fractional_X = x - integer_X

        val v1 = smoothNoise1D(integer_X)
        val v2 = smoothNoise1D(integer_X + 1)

        return cosineInterpolate(v1, v2, fractional_X)

    }

    fun cosineInterpolate(a: Float, b: Float, x: Float): Float {
        val ft = (x * Math.PI).toFloat()
        val f = ((1 - Math.cos(ft.toDouble())) * 0.5).toFloat()

        return a * (1 - f) + b * f
    }

    fun smoothNoise1D(x: Int): Float {
        return getBasicNoise(x, 0) / 2 + getBasicNoise(x - 1, 3) / 4 + getBasicNoise(x + 1, 6) / 4
    }

}
