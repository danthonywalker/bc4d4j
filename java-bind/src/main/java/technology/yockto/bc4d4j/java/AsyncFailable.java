/*
 * This file is part of bc4d4j.
 *
 * bc4d4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * bc4d4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with bc4d4j.  If not, see <http://www.gnu.org/licenses/>.
 */
package technology.yockto.bc4d4j.java;

import kotlin.Unit;
import kotlin.coroutines.experimental.Continuation;
import kotlinx.coroutines.experimental.future.FutureKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import technology.yockto.bc4d4j.core.command.CommandContext;
import technology.yockto.bc4d4j.core.command.Failable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public interface AsyncFailable extends Failable {
    Logger LOGGER = LoggerFactory.getLogger(AsyncFailable.class);

    @NotNull
    default CompletionStage<Void> onFailAsync(@NotNull final CommandContext context,
                                              @Nullable final Exception exception) {

        return CompletableFuture.supplyAsync(() -> { // Mimics the behavior of what would be the super call of onFail
            LOGGER.trace("Command, {}, failed for {} with Exception: {}", context.getCommandName(), this, exception);
            return null;
        });
    }

    @Nullable
    @Override
    @Deprecated
    default Object onFail(@NotNull final CommandContext context,
                          @Nullable final Exception exception,
                          @NotNull final Continuation<? super Unit> continuation) {
        return FutureKt.await(onFailAsync(context, exception).thenApply(ignored -> Unit.INSTANCE), continuation);
    }
}
