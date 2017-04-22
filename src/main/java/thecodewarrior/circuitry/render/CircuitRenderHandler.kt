package thecodewarrior.circuitry.render

import com.google.common.collect.HashMultimap
import com.teamwizardry.librarianlib.core.client.ClientTickHandler
import com.teamwizardry.librarianlib.core.client.ClientTickHandler.partialTicks
import com.teamwizardry.librarianlib.features.forgeevents.CustomWorldRenderEvent
import com.teamwizardry.librarianlib.features.helpers.vec
import com.teamwizardry.librarianlib.features.kotlin.*
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.VertexBuffer
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.EnumHand
import net.minecraft.util.EnumHandSide
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraftforge.client.model.pipeline.LightUtil
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.opengl.GL11
import thecodewarrior.circuitry.CircuitryMod
import thecodewarrior.circuitry.IDStore
import thecodewarrior.circuitry.autoFromNBT
import thecodewarrior.circuitry.internals.*
import thecodewarrior.circuitry.item.ModItems
import java.awt.Color



/**
 * Created by TheCodeWarrior
 */
object CircuitRenderHandler {
    init { MinecraftForge.EVENT_BUS.register(this) }

    val isRainbow = false

    private val networks = IDStore<CircuitNetwork>()
    private val networksByBlockPos = HashMultimap.create<BlockPos, CircuitNetwork>()

    var vbCache: IntArray? = null

    @SubscribeEvent
    fun render(e: CustomWorldRenderEvent) {
        val cap = e.world.getCapability(CapabilityCircuitWorld.cap, null)
        val tessellator = Tessellator.getInstance()
        val vb = tessellator.buffer

        if(vbCache == null) {
            beginVb(vb)
            populateVb(vb)
            vbCache = vb.createCacheArrayAndReset()
        }

        beginVb(vb)
        vb.addCacheArray(vbCache!!)
        getNetworksMouseOver()?.forEach { network ->
            render(network, vb, network.color.hlColor)
        }
        renderHand(vb, EnumHand.MAIN_HAND)
        renderHand(vb, EnumHand.OFF_HAND)
        tessellator.draw()
    }

    fun renderHand(vb: VertexBuffer, hand: EnumHand): Boolean {
        val player = Minecraft.getMinecraft().player
        val stack = player.getHeldItem(hand)
        if(stack.item != ModItems.wire) return false
        if(stack.nbt["pos"] == null) return false

        val color = ModItems.wire.netColor(stack)
        val other: CircuitPos = stack.nbt["pos"]!!.autoFromNBT()
        val otherVec = connectionPos(color, other)

        val handVec: Vec3d

        val isRightHand = (hand == EnumHand.OFF_HAND) xor (player.primaryHand == EnumHandSide.LEFT)

//        if(Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) {
        val look = player.getLook(ClientTickHandler.partialTicks).normalize()
        val horizontal = getVectorForRotation(0f, player.rotationYaw + 90)
        val vHand = if(isRightHand) -horizontal else horizontal
        val vertical = (look cross horizontal).normalize()

        val f = player.distanceWalkedModified - player.prevDistanceWalkedModified
        val f1 = -(player.distanceWalkedModified + f * partialTicks)
        val f2 = player.prevCameraYaw + (player.cameraYaw - player.prevCameraYaw) * partialTicks

        val bobbingX = MathHelper.sin(f1 * Math.PI.toFloat()) * f2 * 0.5F
        val bobbingY = Math.abs(MathHelper.cos(f1 * Math.PI.toFloat()) * f2)
        val cancelBobbing = if(Minecraft.getMinecraft().gameSettings.viewBobbing)
            vertical * -bobbingY + horizontal * -bobbingX
        else
            Vec3d.ZERO

        val itemUp = (vHand * -1 + vertical * -3 + look * 1).normalize()
        val itemForward = (vHand * -1 + look * 1).normalize()
        val itemBob = itemUp * bobbingY

        handVec = player.getPositionEyes(ClientTickHandler.partialTicks) + (
                (vHand * player.width/3) + look * 0.25 + vertical * 0.1 + cancelBobbing * (12/16.0)
                )
//        } else {
//            val model = (Minecraft.getMinecraft().renderManager.getEntityRenderObject(player) as RenderLivingBase<EntityPlayer>).mainModel as? ModelBiped
//            if(model == null)
//                handVec = Vec3d.ZERO
//            else {
//                model.bipedRightArm
//                handVec = Vec3d.ZERO
//            }
//        }

        renderEdge(vb, handVec, otherVec, color.color)
        return true
    }

    fun getVectorForRotation(pitch: Float, yaw: Float): Vec3d {
        val f = MathHelper.cos(-yaw * 0.017453292f - Math.PI.toFloat())
        val f1 = MathHelper.sin(-yaw * 0.017453292f - Math.PI.toFloat())
        val f2 = -MathHelper.cos(-pitch * 0.017453292f)
        val f3 = MathHelper.sin(-pitch * 0.017453292f)
        return Vec3d((f1 * f2).toDouble(), f3.toDouble(), (f * f2).toDouble())
    }

    fun beginVb(vb: VertexBuffer) {
        GlStateManager.enableTexture2D()
        GlStateManager.disableLighting()
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
        Minecraft.getMinecraft().renderEngine.bindTexture(tex)
        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP)
    }

    fun populateVb(vb: VertexBuffer) {
        networks.id2obj.values.forEach { network ->
            render(network, vb)
        }
    }

    fun render(network: CircuitNetwork, vb: VertexBuffer) {
        render(network, vb, network.color.color)
    }

    fun render(network: CircuitNetwork, vb: VertexBuffer, color: Color) {
        network.edges.forEach { (a, b) ->
            val posA = connectionPos(network.color, a)
            val posB = connectionPos(network.color, b)

            renderEdge(vb, posA, posB, color)
        }
    }

    fun renderEdge(vb: VertexBuffer, posA: Vec3d, posB: Vec3d, color: Color) {
        val delta = posB-posA
        val length = delta.lengthVector().toInt()
        val deltaNorm = delta.normalize()
        val stepCount = Math.max(length * 4, 2)
        val step = delta / stepCount
        val positions = (0..stepCount).map {
            val p = posA + (step * it)
            val parabola = getParabola(Math.abs(delta.yCoord), it/stepCount.toDouble())
            vec(p.xCoord, p.yCoord + parabola, p.zCoord)
        }

        val perpendicular = if(delta.xCoord == 0.0 && delta.zCoord == 0.0) vec(1, 0, 0) else (vec(0, 1, 0) cross deltaNorm).normalize()

        val lightMultiplierLeft = LightUtil.diffuseLight(perpendicular.xCoord.toFloat(), perpendicular.yCoord.toFloat(), perpendicular.zCoord.toFloat())
        val lightMultiplierRight = LightUtil.diffuseLight(-perpendicular.xCoord.toFloat(), -perpendicular.yCoord.toFloat(), -perpendicular.zCoord.toFloat())

        val r = 0.01
        val brightnessBias = 0.1f

        (0..positions.size-2).forEach {
            val a = positions[it]
            val b = positions[it+1]
            val up = ((b-a) cross perpendicular).normalize()
            val upS = (if(it == 0) up else ((b - positions[it-1]) cross perpendicular).normalize()) * r
            val upE = (if(it == positions.size-2) up else ((positions[it+2] - a) cross perpendicular).normalize()) * r
            val per = perpendicular*r

            val blockpos = BlockPos( (a+b)/2 )
            val brightness = if (Minecraft.getMinecraft().world.isBlockLoaded(blockpos)) Minecraft.getMinecraft().world.getCombinedLight(blockpos, 0) else 0
            val skyLight = brightness shr 16 and 65535
            val blockLight = brightness and 65535

            val lightMultiplierUp = LightUtil.diffuseLight(up.xCoord.toFloat(), up.yCoord.toFloat(), up.zCoord.toFloat())
            val lightMultiplierDown = LightUtil.diffuseLight(-up.xCoord.toFloat(), -up.yCoord.toFloat(), -up.zCoord.toFloat())

            val colorA = if(!isRainbow) { color } else {
                hueShift(color, Math.PI)
            }

            val colorB = if(!isRainbow) { color } else {
                hueShift(color, 0)
            }


            var mul = (lightMultiplierRight + brightnessBias).clamp(0f, 1f)

            vb.pos(a + upS).tex(0.0, 0.0).color(colorB * mul).lightmap(skyLight, blockLight).endVertex()
            vb.pos(b + upE).tex(1.0, 0.0).color(colorB * mul).lightmap(skyLight, blockLight).endVertex()
            vb.pos(b - upE).tex(1.0, 1.0).color(colorB * mul).lightmap(skyLight, blockLight).endVertex()
            vb.pos(a - upS).tex(0.0, 1.0).color(colorB * mul).lightmap(skyLight, blockLight).endVertex()

            mul = (lightMultiplierLeft + brightnessBias).clamp(0f, 1f)

            vb.pos(a - upS).tex(0.0, 1.0).color(colorA * mul).lightmap(skyLight, blockLight).endVertex()
            vb.pos(b - upE).tex(1.0, 1.0).color(colorB * mul).lightmap(skyLight, blockLight).endVertex()
            vb.pos(b + upE).tex(1.0, 0.0).color(colorB * mul).lightmap(skyLight, blockLight).endVertex()
            vb.pos(a + upS).tex(0.0, 0.0).color(colorA * mul).lightmap(skyLight, blockLight).endVertex()

            mul = (lightMultiplierUp + brightnessBias).clamp(0f, 1f)

            vb.pos(a - per).tex(0.0, 1.0).color(colorA * mul).lightmap(skyLight, blockLight).endVertex()
            vb.pos(b - per).tex(1.0, 1.0).color(colorB * mul).lightmap(skyLight, blockLight).endVertex()
            vb.pos(b + per).tex(1.0, 0.0).color(colorB * mul).lightmap(skyLight, blockLight).endVertex()
            vb.pos(a + per).tex(0.0, 0.0).color(colorA * mul).lightmap(skyLight, blockLight).endVertex()

            mul = (lightMultiplierDown + brightnessBias).clamp(0f, 1f)

            vb.pos(a + per).tex(0.0, 0.0).color(colorA * mul).lightmap(skyLight, blockLight).endVertex()
            vb.pos(b + per).tex(1.0, 0.0).color(colorB * mul).lightmap(skyLight, blockLight).endVertex()
            vb.pos(b - per).tex(1.0, 1.0).color(colorB * mul).lightmap(skyLight, blockLight).endVertex()
            vb.pos(a - per).tex(0.0, 1.0).color(colorA * mul).lightmap(skyLight, blockLight).endVertex()
        }
    }

    fun hueShift(color: Color, angle: Number): Color {
        val arr = FloatArray(3)
        Color.RGBtoHSB(color.red, color.green, color.blue, arr)
        arr[0] = arr[0] + angle.toFloat()
        return Color(Color.HSBtoRGB(arr[0], arr[1], arr[2]))
    }

    fun getParabola(length: Double, x: Double): Double {
        val droop = (length + 1)/4.0
        return droop*(4 * (x - 0.5) * (x - 0.5) - 1)
    }

    fun connectionPos(color: NetworkColor, pos: CircuitPos): Vec3d {
        return Vec3d(pos.block) + (
                CircuitryAPI.getCircuitHandler(Minecraft.getMinecraft().world, pos.block)
                        ?.getRenderPos(Minecraft.getMinecraft().world, pos.block, color, pos.port) ?: Vec3d.ZERO
                )
    }

    fun getNetwork(id: Int): CircuitNetwork? {
        return networks.get(id)
    }

    fun getNetworksMouseOver(): Set<CircuitNetwork>? {
        return Minecraft.getMinecraft().objectMouseOver?.blockPos?.let { blockMouseOver ->
            networksByBlockPos.get(blockMouseOver)
        }
    }

    fun setNetwork(id: Int, network: CircuitNetwork?) {
        vbCache = null
        if(network == null) {
            val existing = networks.get(id)
            existing?.nodes?.forEach {
                networksByBlockPos.remove(it.block, existing)
            }
            networks.remove(id)
        } else {
            val existing = networks.get(id)
            existing?.nodes?.forEach {
                networksByBlockPos.remove(it.block, existing)
            }

            network.id = id // just to make sure
            networks.put(id, network)
            network.rebuildChunks()

            network.nodes.forEach {
                networksByBlockPos.put(it.block, network)
            }
        }
    }

    fun unwatch(chunk: ChunkPos) {
        val toRemove = mutableListOf<Int>()
//        networks.id2obj.forEach {
//            if()
//        }
    }

    fun clear() {
        networks.clear()
    }

    val tex = ResourceLocation(CircuitryMod.MODID, "textures/wire.png")
}

operator fun Color.times(other: Float) = Color((this.red * other).toInt().clamp(0, 255), (this.green * other).toInt().clamp(0, 255), (this.blue * other).toInt().clamp(0, 255), this.alpha)
