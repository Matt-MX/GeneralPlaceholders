package com.mattmx.general

import com.mattmx.ktgui.GuiManager
import com.mattmx.ktgui.commands.declarative.arg.impl.greedyStringArgument
import com.mattmx.ktgui.commands.declarative.arg.impl.longArgument
import com.mattmx.ktgui.commands.declarative.arg.impl.stringArgument
import com.mattmx.ktgui.commands.declarative.div
import com.mattmx.ktgui.papi.placeholder
import com.mattmx.ktgui.papi.placeholderExpansion
import com.mattmx.ktgui.scheduling.sync
import com.mattmx.ktgui.utils.pretty
import org.bukkit.plugin.java.JavaPlugin
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeFormatter
import kotlin.math.abs


class GeneralPlaceholdersPlugin : JavaPlugin() {

    override fun onEnable() {
        instance = this
        GuiManager.init(this)

        sync {
            val epochMillis by longArgument()
            val javaInstant by stringArgument()

            val textIfNegative by greedyStringArgument()
            textIfNegative optionalWithDefault "soon"

            placeholderExpansion {
                id("countdown")

                placeholder("millis" / epochMillis / textIfNegative) {
                    val timeUntil = Duration.ofMillis(epochMillis() - System.currentTimeMillis())
                    if (timeUntil.isNegative) textIfNegative() else timeUntil.pretty()
                }

                placeholder("instant" / javaInstant / textIfNegative) {
                    val result = runCatching {
                        val event = DateTimeFormatter.ISO_ZONED_DATE_TIME.parse(javaInstant(), Instant::from)
                        val now: Instant = Instant.now()
                        val diff = Duration.between(now, event)

                        if (diff.isNegative) textIfNegative() else diff.pretty()
                    }

                    if (result.isSuccess) result.getOrNull() else result.exceptionOrNull()?.message
                }
            }

            placeholderExpansion {
                id("since")

                placeholder("millis" / epochMillis) {
                    val timeUntil = Duration.ofMillis(epochMillis() - System.currentTimeMillis())
                    if (timeUntil.isNegative) textIfNegative() else timeUntil.pretty()
                }

                placeholder("instant" / javaInstant) {
                    val result = runCatching {
                        val event = DateTimeFormatter.ISO_ZONED_DATE_TIME.parse(javaInstant(), Instant::from)
                        val now: Instant = Instant.now()
                        val diff = Duration.between(now, event)

                        Duration.ofMillis(abs(diff.toMillis())).pretty()
                    }

                    if (result.isSuccess) result.getOrNull() else result.exceptionOrNull()?.message
                }
            }
        }
    }

    companion object {
        private lateinit var instance: GeneralPlaceholdersPlugin
        fun get() = instance
    }

}