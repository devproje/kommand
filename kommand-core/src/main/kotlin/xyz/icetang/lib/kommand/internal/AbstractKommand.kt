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

package xyz.icetang.lib.kommand.internal

import xyz.icetang.lib.kommand.Kommand
import xyz.icetang.lib.kommand.KommandDispatcher
import xyz.icetang.lib.kommand.node.RootNode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.plugin.Plugin

abstract class AbstractKommand : xyz.icetang.lib.kommand.Kommand {
    private var registered = false

    override fun register(
        plugin: Plugin,
        name: String,
        vararg aliases: String,
        init: RootNode.() -> Unit
    ): KommandDispatcher {
        require(plugin.isEnabled) { "Plugin disabled!" }
        require(test(name, aliases))

        return KommandDispatcherImpl().apply {
            initialize(plugin, name)
            root.init()
            immutable = true
        }.also { dispatcher ->
            register(dispatcher, aliases.toList())

            plugin.server.pluginManager.registerEvents(
                object : Listener {
                    @EventHandler(priority = EventPriority.LOWEST)
                    fun onPluginDisable(event: PluginDisableEvent) {
                        if (event.plugin === plugin) {
                            unregister(name)
                            aliases.forEach { unregister(it) }
                        }
                    }
                },
                plugin
            )

            if (!registered) {
                plugin.server.pluginManager.registerEvents(PlayerListener(this), plugin)
                registered = true
            }
        }
    }

    /**
     * 이미 등록된 명령 이름인지 확인
     *
     * @return 등록이 가능하다면 true, 불가능하다면 false
     */
    protected abstract fun test(name: String, aliases: Array<out String>): Boolean

    protected abstract fun register(dispatcher: KommandDispatcherImpl, aliases: List<String>)

    /**
     * 외부 접근 금지
     *
     * 리로드시 사용
     */
    protected abstract fun unregister(name: String)

    abstract fun sendCommandsPacket(player: Player)
}

class PlayerListener(private val kommand: AbstractKommand) : Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        kommand.sendCommandsPacket(event.player)
    }
}