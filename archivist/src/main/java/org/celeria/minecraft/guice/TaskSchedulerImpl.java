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

package org.celeria.minecraft.guice;

import static com.google.common.base.Preconditions.*;
import javax.annotation.concurrent.Immutable;
import com.google.inject.Inject;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

@Immutable
class TaskSchedulerImpl implements TaskScheduler {
    private final Plugin plugin;
    private final BukkitScheduler scheduler;

    @Inject
    TaskSchedulerImpl(final Plugin plugin, final BukkitScheduler scheduler) {
        this.plugin = plugin;
        this.scheduler = scheduler;
    }

    @Override
    public void cancelTasks() {
        scheduler.cancelTasks(plugin);
    }

    @Override
    public void repeatAsynchronousTask(final Runnable task,
            final long period) {
        checkNotNull(task);
        checkArgument(period >= 0);
        final int delay = 0;
        scheduler.scheduleAsyncRepeatingTask(plugin, task, delay, period);
    }

    @Override
    public void repeatSynchronousTask(final Runnable task,
            final long delay, final long period) {
        checkNotNull(task);
        checkArgument(delay >= 0);
        checkArgument(period >= 0);
        scheduler.scheduleSyncRepeatingTask(plugin, task, delay, period);
    }

    @Override
    public void runAsynchronousTask(final Runnable task) {
        checkNotNull(task);
        scheduler.scheduleAsyncDelayedTask(plugin, checkNotNull(task));
    }

    @Override
    public void runSynchronousTask(final Runnable task) {
        checkNotNull(task);
        scheduler.scheduleSyncDelayedTask(plugin, checkNotNull(task));
    }
}
