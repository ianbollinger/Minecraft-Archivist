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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import com.google.inject.Inject;
import org.bukkit.entity.Player;
import org.celeria.minecraft.guice.TaskScheduler;
import org.jukito.*;
import org.junit.*;
import org.junit.runner.RunWith;

@RunWith(JukitoRunner.class)
public class ManualBackUpExecutorTest {
    @Inject private ManualBackUpExecutor executor;
    @Inject private TaskScheduler scheduler;
    @Inject private Player player;
    @Inject private BackUpWorldsTask task;

    @Before
    public void setUp() {
        // Do nothing for now.
    }

    @Test
    public void shouldDoNothingWhenSenderIsNotPlayer() {
        assertFalse(executor.onCommand(null, null, null, null));
        verifyZeroInteractions(scheduler);
    }

    @Test
    public void shouldDoNothingWhenSenderIsNotOp() {
        when(player.isOp()).thenReturn(false);
        assertFalse(executor.onCommand(player, null, null, null));
        verifyZeroInteractions(scheduler);
    }

    @Test
    public void shouldScheduleBackUpTask() {
        when(player.isOp()).thenReturn(true);
        assertTrue(executor.onCommand(player, null, null, null));
        verify(scheduler).runSynchronousTask(task);
        verifyNoMoreInteractions(scheduler);
    }
}
