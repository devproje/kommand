/*
 * Kommand
 * Copyright (C) 2021 Monun
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package xyz.icetang.lib.kommand

import com.destroystokyo.paper.profile.PlayerProfile
import com.google.gson.JsonObject
import xyz.icetang.lib.kommand.loader.KommandLoader
import xyz.icetang.lib.kommand.wrapper.*
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Axis
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.World
import org.bukkit.advancement.Advancement
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import org.bukkit.potion.PotionEffectType
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Team
import java.util.*

// 인수
@xyz.icetang.lib.kommand.KommandDSL
interface KommandArgument<T> {
    companion object : KommandArgumentSupport by KommandArgumentSupport.INSTANCE

    fun suggests(provider: KommandSuggestion.(context: KommandContext) -> Unit)
}

interface KommandArgumentSupport {
    companion object {
        val INSTANCE = KommandLoader.loadCompat(KommandArgumentSupport::class.java)
    }

    // com.mojang.brigadier.arguments

    fun bool(): KommandArgument<Boolean>

    fun int(minimum: Int = Int.MIN_VALUE, maximum: Int = Int.MAX_VALUE): KommandArgument<Int>

    fun float(minimum: Float = -Float.MAX_VALUE, maximum: Float = Float.MAX_VALUE): KommandArgument<Float>

    fun double(minimum: Double = -Double.MAX_VALUE, maximum: Double = Double.MAX_VALUE): KommandArgument<Double>

    fun long(minimum: Long = Long.MIN_VALUE, maximum: Long = Long.MAX_VALUE): KommandArgument<Long>

    fun string(type: StringType = StringType.SINGLE_WORD): KommandArgument<String>

    // net.minecraft.commands.arguments

    fun angle(): KommandArgument<Float>

    fun color(): KommandArgument<TextColor>

    fun component(): KommandArgument<Component>

    fun compoundTag(): KommandArgument<JsonObject>

    fun dimension(): KommandArgument<World>

    fun entityAnchor(): KommandArgument<EntityAnchor>

    fun entity(): KommandArgument<Entity>

    fun entities(): KommandArgument<Collection<Entity>>

    fun player(): KommandArgument<Player>

    fun players(): KommandArgument<Collection<Player>>

    fun summonableEntity(): KommandArgument<NamespacedKey>

    fun profile(): KommandArgument<Collection<PlayerProfile>>

    fun enchantment(): KommandArgument<Enchantment>

    fun message(): KommandArgument<Component>

    fun mobEffect(): KommandArgument<PotionEffectType>

    //    fun nbtPath(): KommandArgument<*> [NbtTagArgument]

    fun objective(): KommandArgument<Objective>

    fun objectiveCriteria(): KommandArgument<String>

    //    fun operation(): KommandArgument<*> [OperationArgument]

    fun particle(): KommandArgument<Particle>

    fun intRange(): KommandArgument<IntRange>

    fun doubleRange(): KommandArgument<ClosedFloatingPointRange<Double>>

    fun advancement(): KommandArgument<Advancement>

    fun recipe(): KommandArgument<Recipe>

    //    ResourceLocationArgument#getPredicate()

    //    ResourceLocationArgument#getItemModifier()

    fun displaySlot(): KommandArgument<DisplaySlot>

    fun score(): KommandArgument<String>

    fun scores(): KommandArgument<Collection<String>>

    fun slot(): KommandArgument<Int>

    fun team(): KommandArgument<Team>

    fun time(): KommandArgument<Int>

    fun uuid(): KommandArgument<UUID>

    // net.minecraft.commands.arguments.blocks

    fun blockPredicate(): KommandArgument<(Block) -> Boolean>

    fun blockState(): KommandArgument<BlockData>

    // net.minecraft.commands.arguments.coordinates

    fun blockPosition(type: PositionLoadType = PositionLoadType.LOADED): KommandArgument<BlockPosition3D>

    fun blockPosition2D(): KommandArgument<BlockPosition2D>

    fun position(): KommandArgument<Position3D>

    fun position2D(): KommandArgument<Position2D>

    fun rotation(): KommandArgument<Rotation>

    fun swizzle(): KommandArgument<EnumSet<Axis>>

    // net.minecraft.commands.arguments.item

    fun function(): KommandArgument<() -> Unit>

    fun item(): KommandArgument<ItemStack>

    fun itemPredicate(): KommandArgument<(ItemStack) -> Boolean>

    // dynamic

    fun <T> dynamic(
        type: StringType = StringType.SINGLE_WORD,
        function: KommandSource.(context: KommandContext, input: String) -> T?
    ): KommandArgument<T>

    fun <T> dynamicByMap(
        map: Map<String, T>,
        type: StringType = StringType.SINGLE_WORD,
        tooltip: ((T) -> ComponentLike)? = null
    ): KommandArgument<T> {
        return dynamic(type) { _, input ->
            map[input]
        }.apply {
            suggests {
                if (tooltip == null) {
                    suggest(map.keys)
                } else {
                    suggest(map, tooltip)
                }
            }
        }
    }

    fun <T : Enum<T>> dynamicByEnum(
        set: EnumSet<T>,
        tooltip: ((T) -> ComponentLike)? = null
    ): KommandArgument<T> {
        return dynamic(StringType.SINGLE_WORD) { _, input ->
            set.find { it.name == input }
        }.apply {
            suggests {
                suggest(set, { it.name }, tooltip)
            }
        }
    }
}

enum class StringType {
    SINGLE_WORD,
    QUOTABLE_PHRASE,
    GREEDY_PHRASE
}

enum class PositionLoadType {
    LOADED,
    SPAWNABLE
}
