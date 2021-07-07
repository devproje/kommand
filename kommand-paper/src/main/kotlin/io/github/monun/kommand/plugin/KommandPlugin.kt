package io.github.monun.kommand.plugin

import com.google.gson.JsonObject
import io.github.monun.kommand.Kommand
import io.github.monun.kommand.KommandArgument
import io.github.monun.kommand.StringType
import io.github.monun.kommand.getValue
import io.github.monun.kommand.util.BlockPosition
import io.github.monun.kommand.util.Position
import io.github.monun.kommand.util.Rotation
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.md_5.bungee.api.ChatColor
import org.bukkit.Axis
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.util.EnumSet

class KommandPlugin : JavaPlugin() {
    override fun onEnable() {
        val bool = KommandArgument.bool()
        val int = KommandArgument.int()
        val word = KommandArgument.string(StringType.SINGLE_WORD)
        val string = KommandArgument.string(StringType.QUOTABLE_PHRASE)
        val greedy = KommandArgument.string(StringType.GREEDY_PHRASE)

        Kommand.register("my") {
            then("age") {
                then("age" to int) {
                    executes {
                        val age: Int by it
                        Bukkit.broadcast(text("내 나이는 $age 살입니다."))
                    }
                }
            }
            then("flag") {
                then("flag" to bool) {
                    executes {
                        val flag: Boolean by it
                        Bukkit.broadcast(text("플래그 $flag"))
                    }
                }
            }
            then("word") {
                then("text" to word) {
                    executes {
                        val text: String by it
                        Bukkit.broadcast(text("word $text"))
                    }
                }
            }
            then("string") {
                then("text" to string) {
                    executes {
                        val text: String by it
                        Bukkit.broadcast(text("quote $text"))
                    }
                }
            }
            then("greedy") {
                then("text" to greedy) {
                    executes {
                        val text: String by it
                        Bukkit.broadcast(text("greedy $text"))
                    }
                }
            }
            then("color") {
                then("color" to KommandArgument.color()) {
                    executes {
                        val color: ChatColor by it
                        Bukkit.broadcast(text("$color color"))
                    }
                }
            }
            then("component") {
                then("component" to KommandArgument.component()) {
                    executes {
                        val component: Component by it
                        Bukkit.broadcast(component)
                    }
                }
            }
            then("compoundTag") {
                then("compoundTag" to KommandArgument.compoundTag()) {
                    executes {
                        val compoundTag: JsonObject by it
                        Bukkit.broadcast(text(compoundTag.toString()))
                    }
                }
            }
            then("dimension") {
                then("world" to KommandArgument.dimension()) {
                    executes {
                        val world: World by it
                        Bukkit.broadcast(text(world.toString()))
                    }
                }
            }
            //
            then("blockPredicate") {
                requires {
                    it.playerOrNull != null
                }

                then("predicate" to KommandArgument.blockPredicate()) {
                    executes {
                        val predicate: (Block) -> Boolean by it
                        val flag = predicate(it.source.player.location.add(0.0, -1.0, 0.0).block)
                        Bukkit.broadcast(text("$flag predicate"))
                    }
                }
            }
            then("blockState") {
                then("state" to KommandArgument.blockState()) {
                    executes {
                        val state: BlockData by it
                        val asString = state.getAsString(true)
                        Bukkit.broadcast(text("blockData: $asString"))
                    }
                }
            }
            then("blockPosition") {
                requires {
                    it.playerOrNull != null
                }

                then("position" to KommandArgument.blockPosition()) {
                    executes {
                        val position: BlockPosition by it
                        Bukkit.broadcast(text(position.toBlock(it.source.player.world).type.translationKey))
                    }
                }
            }
            then("position") {
                requires {
                    it.playerOrNull != null
                }

                then("position" to KommandArgument.position()) {
                    executes {
                        val position: Position by it
                        Bukkit.broadcast(text("${position.asVector.distance(it.source.player.location.toVector())} far"))
                    }
                }
            }
            then("rotation") {
                requires {
                    it.playerOrNull != null
                }

                then("rotation" to KommandArgument.rotation()) {
                    executes {
                        val rotation: Rotation by it
//                        it.source.player.setRotation(rotation.yaw, rotation.pitch)
                        Bukkit.broadcast(text("[${rotation.yaw}, ${rotation.pitch}]"))
                    }
                }
            }
            then("swizzle") {
                then("swizzle" to KommandArgument.swizzle()) {
                    executes {
                        val swizzle: EnumSet<Axis> by it
                        Bukkit.broadcast(text(swizzle.joinToString()))
                    }
                }
            }
            then("item") {
                requires {
                    it.playerOrNull != null
                }

                then("item" to KommandArgument.item()) {
                    executes {
                        val item: ItemStack by it
                        it.source.player.inventory.addItem(item)
                    }
                }
            }
            then("itemPredicate") {
                requires {
                    it.playerOrNull != null
                }

                then("predicate" to KommandArgument.itemPredicate()) {
                    executes {
                        val predicate: (ItemStack) -> Boolean by it
                        val flag = predicate(it.source.player.inventory.itemInMainHand)
                        Bukkit.broadcast(text("$flag predicate"))
                    }
                }
            }
        }
    }
}