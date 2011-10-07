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

import static org.mockito.Mockito.*;
import com.google.inject.Inject;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jukito.JukitoRunner;
import org.junit.*;
import org.junit.runner.RunWith;

@RunWith(JukitoRunner.class) public class TaskSchedulerTest {
    @Inject private TaskSchedulerImpl scheduler;
    @Inject private BukkitScheduler underlyingScheduler;
    @Inject private Plugin plugin;
    @Inject private Runnable task;

    @Test
    public void testCancelTasks() {
        scheduler.cancelTasks();
        verify(underlyingScheduler).cancelTasks(plugin);
    }

    @Test
    public void testRepeatAsynchronousTask() {
        scheduler.repeatAsynchronousTask(task, 1);
        verify(underlyingScheduler)
                .scheduleAsyncRepeatingTask(plugin, task, 0, 1);
    }

    @Test
    public void testRepeatSynchronousTask() {
        scheduler.repeatSynchronousTask(task, 0, 1);
        verify(underlyingScheduler).scheduleSyncRepeatingTask(plugin, task, 0,
                1);
    }

    @Test
    public void testRunAsynchronousTask() {
        scheduler.runAsynchronousTask(task);
        verify(underlyingScheduler).scheduleAsyncDelayedTask(plugin, task);
    }

    @Test
    public void testRunSynchronousTask() {
        scheduler.runSynchronousTask(task);
        verify(underlyingScheduler).scheduleSyncDelayedTask(plugin, task);
    }
}
