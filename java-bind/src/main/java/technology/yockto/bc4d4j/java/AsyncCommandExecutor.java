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
import technology.yockto.bc4d4j.core.command.CommandContext;
import technology.yockto.bc4d4j.core.command.CommandExecutor;

import java.util.concurrent.CompletionStage;

@FunctionalInterface
public interface AsyncCommandExecutor extends CommandExecutor, AsyncFailable {
    @NotNull
    CompletionStage<Void> onExecuteAsync(@NotNull CommandContext context);

    @Nullable
    @Override
    @Deprecated
    default Object onExecute(@NotNull final CommandContext context,
                             @NotNull final Continuation<? super Unit> continuation) {
        return FutureKt.await(onExecuteAsync(context).thenApply(ignored -> Unit.INSTANCE), continuation);
    }
}
