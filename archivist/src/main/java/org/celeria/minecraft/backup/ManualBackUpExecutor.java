/*
 * Copyright 2011 Ian D. Bollinger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.celeria.minecraft.backup;

import com.google.inject.Inject;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.celeria.minecraft.guice.TaskScheduler;

class ManualBackUpExecutor implements CommandExecutor {
    private TaskScheduler scheduler;
    private BackUpWorldsTask task;

    @Inject
    ManualBackUpExecutor(final TaskScheduler scheduler,
            final BackUpWorldsTask task) {
        this.scheduler = scheduler;
        this.task = task;
    }

    @Override
    public boolean onCommand(final CommandSender sender,
            @SuppressWarnings("unused") final Command command,
            @SuppressWarnings("unused") final String label,
            @SuppressWarnings("unused") final String[] args) {
        if (invalidSender(sender)) {
            return false;
        }
        scheduler.runSynchronousTask(task);
        return true;
    }

    private boolean invalidSender(final CommandSender sender) {
        return !((sender instanceof Player && ((Player) sender).isOp())
                || sender instanceof ConsoleCommandSender);
    }
}
