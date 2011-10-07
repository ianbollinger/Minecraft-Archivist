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
import org.celeria.minecraft.backup.ConfigurationModule.DurationToKeepBackups;
import org.celeria.minecraft.backup.DeleteOldBackupsTask.CurrentTime;
import org.jukito.*;
import org.junit.*;
import org.junit.runner.RunWith;

@RunWith(JukitoRunner.class)
public class DeleteOldBackupsTaskTest {
    private static final long NEW_TIME = 100;
    private static final long OLD_TIME = -100;
    private static final long CURRENT_TIME = 101;
    private static final long DURATION = 20;

    public static class Module extends JukitoModule {
        @Override
        protected void configureTest() {
            bindConstant().annotatedWith(CurrentTime.class).to(CURRENT_TIME);
            bindConstant().annotatedWith(DurationToKeepBackups.class).to(
                    DURATION);
        }
    }

    @Inject private DeleteOldBackupsTask task;
    @Inject @BackupFolder private FileProvider<FileObject> backupFolderProvider;
    @Inject private FileObject backupFolder;
    @Inject @Named("old") private FileObject oldBackUp;
    @Inject @Named("new") private FileObject newBackUp;
    @Inject @Named("old") private FileContent oldBackUpContent;
    @Inject @Named("new") private FileContent newBackUpContent;

    @Before
    public void setUp() throws FileSystemException {
        when(oldBackUp.getContent()).thenReturn(oldBackUpContent);
        when(oldBackUpContent.getLastModifiedTime()).thenReturn(OLD_TIME);
        when(newBackUp.getContent()).thenReturn(newBackUpContent);
        when(newBackUpContent.getLastModifiedTime()).thenReturn(NEW_TIME);
        when(backupFolder.getChildren()).thenReturn(
                new FileObject[] {oldBackUp, newBackUp});
        when(backupFolderProvider.get()).thenReturn(backupFolder);
    }

    @Test
    public void shouldDeleteOldBackUp() throws FileSystemException {
        task.run();
        verify(oldBackUp).delete();
    }

    @Test
    public void shouldNotDeleteNewBackUp() throws FileSystemException {
        task.run();
        verify(newBackUp, never()).delete();
    }
}
