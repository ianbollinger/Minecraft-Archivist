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
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.commons.vfs2.*;
import org.celeria.minecraft.backup.BackUpWorldsTask.BackupFolder;
import org.joda.time.*;
import org.jukito.*;
import org.junit.*;
import org.junit.runner.RunWith;

@RunWith(JukitoRunner.class)
public class DeleteOldBackupsTaskTest {
    private static final Instant CURRENT_TIME = Instant.parse("T00:00:59");
    private static final long NEW_TIME = CURRENT_TIME.getMillis() * 2;
    private static final long OLD_TIME = 0;
    private static final Duration DURATION = Duration.parse("PT20S");

    public static class Module extends JukitoModule {
        @Override
        protected void configureTest() {
            bind(Instant.class).toInstance(CURRENT_TIME);
            bind(Duration.class).toInstance(DURATION);
        }
    }

    @Inject private DeleteOldBackupsTask task;
    @Inject @Named("old") private FileObject oldBackUp;
    @Inject @Named("new") private FileObject newBackUp;

    @Before
    public void setUpFolder(
            @BackupFolder final FileProvider<FileObject> backupFolderProvider,
            @BackupFolder final FileObject backupFolder) throws Exception {
        when(backupFolder.getChildren())
                .thenReturn(new FileObject[] {oldBackUp, newBackUp});
        when(backupFolderProvider.get()).thenReturn(backupFolder);
    }

    @Before
    public void setUpOldBackUp(
            @Named("old") final FileContent oldBackUpContent) throws Exception {
        when(oldBackUp.getContent()).thenReturn(oldBackUpContent);
        when(oldBackUpContent.getLastModifiedTime()).thenReturn(OLD_TIME);
    }

    @Before
    public void setUpNewBackUp(
            @Named("new") final FileContent newBackUpContent) throws Exception {
        when(newBackUp.getContent()).thenReturn(newBackUpContent);
        when(newBackUpContent.getLastModifiedTime()).thenReturn(NEW_TIME);
    }

    @Test
    public void shouldDeleteOldBackUp() throws Exception {
        task.run();
        verify(oldBackUp).delete();
    }

    @Test
    public void shouldNotDeleteNewBackUp() throws Exception {
        task.run();
        verify(newBackUp, never()).delete();
    }
}
