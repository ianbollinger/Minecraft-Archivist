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

import static org.mockito.Mockito.*;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import org.bukkit.*;
import org.celeria.minecraft.guice.TaskScheduler;
import org.jukito.JukitoRunner;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Matchers;

@RunWith(JukitoRunner.class)
public class BackUpWorldsTaskTest {
    @Inject private BackUpWorldsTask task;
    @Inject private Server server;
    @Inject private TaskScheduler scheduler;
    @Inject private Iterable<World> worlds;
    @Inject private World world;
    @Inject private WorldTaskFactory worldTaskFactory;
    @Inject private WorldTask worldTask;

    @Before
    public void setUp() {
        when(worldTaskFactory.create(Matchers.<World>any())).thenReturn(
                worldTask);
        when(worlds.iterator()).thenReturn(
                ImmutableList.<World>of(world).iterator());
    }

    @Test
    public void testRun() {
        task.run();
        verify(server).savePlayers();
        verify(scheduler).runAsynchronousTask(worldTask);
    }
}
