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

import javax.annotation.concurrent.Immutable;
import com.google.inject.*;
import org.bukkit.command.*;
import org.celeria.minecraft.guice.*;
import org.joda.time.Period;
import org.slf4j.cal10n.LocLogger;

@Immutable
class Archivist implements BukkitPlugin {
    private final LocLogger log;
    private final TaskScheduler scheduler;
    private final PluginCommand pluginCommand;
    private final CommandExecutor manualBackUpExecutor;
    private final Runnable cleanBackupsTask;
    private final Runnable backUpTask;
    private final Period backUpPeriod;

    @Inject
    Archivist(final LocLogger log, final TaskScheduler scheduler,
            final PluginCommand pluginCommand,
            final DeleteOldBackupsTask deleteOldBackupsTask,
            final CommandExecutor manualBackUpExecutor,
            final BackUpWorldsTask backUpTask,
            final Period backUpPeriod) {
        this.log = log;
        this.scheduler = scheduler;
        this.pluginCommand = pluginCommand;
        this.cleanBackupsTask = deleteOldBackupsTask;
        this.manualBackUpExecutor = manualBackUpExecutor;
        this.backUpTask = backUpTask;
        this.backUpPeriod = backUpPeriod;
    }

    @Override
    public void run() {
        log.info(LogMessage.PLUGIN_ENABLED);
        resetTasks();
        enableManualBackupCommand();
        scheduleTasks();
    }

    private void resetTasks() {
        scheduler.cancelTasks();
    }

    private void enableManualBackupCommand() {
        pluginCommand.setExecutor(manualBackUpExecutor);
    }

    private void scheduleTasks() {
        scheduleBackUpCleaner();
        scheduleBackUpTask();
    }

    // TODO: create generic task (value) object.
    // TODO: augment scheduler interface to take period objects.
    private void scheduleBackUpCleaner() {
        scheduler.repeatAsynchronousTask(cleanBackupsTask,
                2 * backUpPeriod.getMillis());
    }

    private void scheduleBackUpTask() {
        scheduler.repeatSynchronousTask(backUpTask, backUpPeriod.getMillis(),
                backUpPeriod.getMillis());
    }

    @Override
    public void stop() {
        resetTasks();
        log.info(LogMessage.PLUGIN_DISABLED);
    }
}
