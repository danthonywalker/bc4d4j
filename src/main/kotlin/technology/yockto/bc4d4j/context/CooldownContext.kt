/*
 * Better Commands 4 Discord4J
 * Copyright (C) 2017  danthonywalker
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package technology.yockto.bc4d4j.context

import java.time.Instant
import java.time.temporal.ChronoUnit.SECONDS
import java.time.temporal.TemporalUnit

data class CooldownContext(
    val timeUnit: TemporalUnit = SECONDS,
    val start: Instant = Instant.now(),
    val duration: Long = 0) {

    val end: Instant = start.plus(duration, timeUnit)
}