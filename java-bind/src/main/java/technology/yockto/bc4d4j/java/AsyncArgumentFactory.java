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

import kotlin.coroutines.experimental.Continuation;
import kotlinx.coroutines.experimental.future.FutureKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import technology.yockto.bc4d4j.command.ArgumentFactory;
import technology.yockto.bc4d4j.command.CommandContext;

import java.util.List;
import java.util.concurrent.CompletionStage;

@FunctionalInterface
public interface AsyncArgumentFactory extends ArgumentFactory, AsyncFailable {
    @NotNull
    CompletionStage<List<String>> getArgumentsAsync(@NotNull CommandContext context);

    @Nullable
    @Override
    @Deprecated
    default Object getArguments(@NotNull final CommandContext context,
                                @NotNull final Continuation<? super List<String>> continuation) {
        return FutureKt.await(getArgumentsAsync(context), continuation);
    }
}
