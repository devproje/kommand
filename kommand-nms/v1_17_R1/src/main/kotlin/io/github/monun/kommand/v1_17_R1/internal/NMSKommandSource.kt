package io.github.monun.kommand.v1_17_R1.internal

import io.github.monun.kommand.KommandSource
import io.github.monun.kommand.util.Position
import io.github.monun.kommand.util.Rotation
import net.minecraft.commands.CommandSourceStack
import org.bukkit.command.CommandSender
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

class NMSKommandSource(
    private val nms: CommandSourceStack
) : KommandSource {
    override val sender: CommandSender
        get() = nms.bukkitSender

    override val entity: Entity
        get() = nms.entityOrException.bukkitEntity

    override val entityOrNull: Entity?
        get() = nms.entity?.bukkitEntity

    override val player: Player
        get() = nms.playerOrException.bukkitEntity

    override val playerOrNull: Player?
        get() = nms.entity?.bukkitEntity?.takeIf { it is Player } as Player?

    override val position: Position
        get() = nms.position.run { Position(x, y, z) }

    override val rotation: Rotation
        get() = nms.rotation.run { Rotation(x, y) }
}