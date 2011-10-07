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

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;
import java.lang.annotation.*;
import com.google.inject.*;
import org.bukkit.command.*;
import org.celeria.minecraft.guice.BukkitPlugin;
import org.celeria.minecraft.guice.TaskScheduler;
import org.slf4j.cal10n.LocLogger;

class Archivist implements BukkitPlugin {
    @BindingAnnotation @Target({FIELD, PARAMETER, METHOD}) @Retention(RUNTIME)
    public @interface BackUpInterval {}

    private final LocLogger log;
    private final TaskScheduler scheduler;
    private final PluginCommand pluginCommand;
    private final CommandExecutor manualBackUpExecutor;
    private final Runnable cleanBackupsTask;
    private final Runnable backUpTask;
    private final String version;
    private final long backUpInterval;

    @Inject
    Archivist(final LocLogger log, final TaskScheduler scheduler,
            final PluginCommand pluginCommand,
            final DeleteOldBackupsTask deleteOldBackupsTask,
            final CommandExecutor manualBackUpExecutor,
            final BackUpWorldsTask backUpTask,
            @PluginVersion final String version,
            @BackUpInterval final long interval) {
        this.log = log;
        this.scheduler = scheduler;
        this.pluginCommand = pluginCommand;
        this.cleanBackupsTask = deleteOldBackupsTask;
        this.manualBackUpExecutor = manualBackUpExecutor;
        this.backUpTask = backUpTask;
        this.version = version;
        this.backUpInterval = interval;
    }

    @Override
    public void run() {
        log.info(LogMessage.PLUGIN_ENABLED, version);
        scheduler.cancelTasks();
        scheduleBackUpCleaner();
        scheduleBackUpTask();
        pluginCommand.setExecutor(manualBackUpExecutor);
    }

    private void scheduleBackUpCleaner() {
        scheduler.repeatAsynchronousTask(cleanBackupsTask, 2 * backUpInterval);
    }

    private void scheduleBackUpTask() {
        scheduler.repeatSynchronousTask(backUpTask, backUpInterval,
                backUpInterval);
    }

    @Override
    public void stop() {
        scheduler.cancelTasks();
        log.info(LogMessage.PLUGIN_DISABLED, version);
    }
}
