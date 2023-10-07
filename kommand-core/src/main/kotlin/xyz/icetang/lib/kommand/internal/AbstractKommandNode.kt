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

import xyz.icetang.lib.kommand.KommandArgument
import xyz.icetang.lib.kommand.KommandArgumentSupport
import xyz.icetang.lib.kommand.KommandContext
import xyz.icetang.lib.kommand.KommandSource
import xyz.icetang.lib.kommand.node.KommandNode
import kotlin.properties.ObservableProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class AbstractKommandNode : KommandNode, KommandArgumentSupport by KommandArgumentSupport.INSTANCE {
    protected fun <T> kommandField(initialValue: T): ReadWriteProperty<Any?, T> =
        object : ObservableProperty<T>(initialValue) {
            private var initialized = false

            override fun beforeChange(property: KProperty<*>, oldValue: T, newValue: T): Boolean {
                require(!kommand.immutable) { "Cannot redefine ${property.name} after registration" }
                require(!initialized) { "Cannot redefine ${property.name} after initialization" }

                return true;
            }

            override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) {
                initialized = true
            }
        }

    lateinit var kommand: KommandDispatcherImpl
    lateinit var name: String

    var parent: AbstractKommandNode? = null

    var requires: (KommandSource.() -> Boolean) by kommandField { true }
        private set

    var executes: (KommandSource.(context: KommandContext) -> Unit)? by kommandField(null)
        private set

    protected fun initialize0(kommand: KommandDispatcherImpl, name: String) {
        this.kommand = kommand
        this.name = name
    }

    val nodes = arrayListOf<AbstractKommandNode>()

    override fun requires(requires: KommandSource.() -> Boolean) {
        this.requires = requires
    }

    override fun executes(executes: KommandSource.(context: KommandContext) -> Unit) {
        this.executes = executes
    }

    override fun then(name: String, vararg arguments: Pair<String, KommandArgument<*>>, init: KommandNode.() -> Unit) {
        kommand.checkState()

        then(LiteralNodeImpl().apply {
            parent = this@AbstractKommandNode
            initialize(this@AbstractKommandNode.kommand, name)
        }.also {
            nodes += it
        }, arguments, init)
    }

    override fun then(
        argument: Pair<String, KommandArgument<*>>,
        vararg arguments: Pair<String, KommandArgument<*>>,
        init: KommandNode.() -> Unit
    ) {
        kommand.checkState()

        then(ArgumentNodeImpl().apply {
            parent = this@AbstractKommandNode
            initialize(this@AbstractKommandNode.kommand, argument.first, argument.second)
        }.also {
            nodes += it
        }, arguments, init)
    }

    private fun then(
        node: AbstractKommandNode,
        arguments: Array<out Pair<String, KommandArgument<*>>>,
        init: KommandNode.() -> Unit
    ) {
        var tail = node

        for ((subName, subArgument) in arguments) {
            val child = ArgumentNodeImpl().apply {
                parent = tail
                initialize(tail.kommand, subName, subArgument)
            }.also { tail.nodes += it }
            tail = child
        }

        tail.init()
    }
}