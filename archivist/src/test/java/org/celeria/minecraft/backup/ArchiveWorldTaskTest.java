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
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.commons.vfs2.*;
import org.bukkit.World;
import org.celeria.minecraft.backup.ArchiveWorldTask.*;
import org.jukito.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Matchers;

@RunWith(JukitoRunner.class)
public class ArchiveWorldTaskTest {
    public static class Module extends JukitoModule {
        @Override
        protected void configureTest() {
            bind(World.class).toInstance(mock(World.class));
        }
    }

    @Inject private ArchiveWorldTask task;
    @Inject @WorldFolder private FileObject worldFolder;
    @Inject @TemporaryWorldFolder private FileObject temporaryWorldFolder;
    @Inject private ZipOutputStream archive;
    @Inject @Named("file") private FileObject file;
    @Inject @Named("folder") private FileObject folder;
    @Inject @Named("file") private FileName fileName;

    @Before
    public void setUpFile(final FileContent fileContent,
            final InputStream inputStream) throws Exception {
        when(file.getType()).thenReturn(FileType.FILE);
        when(file.getContent()).thenReturn(fileContent);
        when(file.getName()).thenReturn(fileName);
        when(fileContent.getInputStream()).thenReturn(inputStream);
        when(inputStream.read(Matchers.<byte[]>any())).thenReturn(-1);
    }

    @Before
    public void setUpFolder() throws Exception {
        when(folder.getName()).thenReturn(fileName);
        when(folder.getType()).thenReturn(FileType.FOLDER);
        when(folder.getChildren()).thenReturn(new FileObject[] {});
    }

    @Before
    public void setUpTemporaryWorldFolder(
            @TemporaryWorldFolder final FileName temporaryWorldFolderName)
            throws Exception {
        when(temporaryWorldFolder.getChildren())
                .thenReturn(new FileObject[] {folder, file});
        when(temporaryWorldFolder.getName())
                .thenReturn(temporaryWorldFolderName);
        when(temporaryWorldFolderName.getRelativeName(fileName))
                .thenReturn("foobar");
    }

    @Before
    public void setTemporaryWorldFolderProvider(
            @TemporaryWorldFolder
            final FileProvider<FileObject> temporaryWorldFolderProvider)
            throws Exception {
        when(temporaryWorldFolderProvider.get())
                .thenReturn(temporaryWorldFolder);
    }

    @Before
    public void setUpWorldFolderProvider(
            @WorldFolder final FileProvider<FileObject> worldFolderProvider)
            throws Exception {
        when(worldFolderProvider.get()).thenReturn(worldFolder);
    }

    @Before
    public void setUpArchiveProvider(
            final FileProvider<ZipOutputStream> archiveProvider)
            throws Exception {
        when(archiveProvider.get()).thenReturn(archive);
    }

    // TODO: figure out what's up with Jukito so I can split this into
    // multiple tests!
    @Test
    public void testRun(final World world) throws Exception {
        task.run();
        shouldSaveWorld(world);
        verify(temporaryWorldFolder)
                .copyFrom(worldFolder, Selectors.SELECT_ALL);
        verify(archive).putNextEntry(Matchers.<ZipEntry>any());
        verify(archive).close();
        shouldDeleteTemporaryFolder();
    }

    private void shouldSaveWorld(final World world) {
        verify(world).setAutoSave(false);
        verify(world).save();
        verify(world).setAutoSave(true);
    }

    private void shouldDeleteTemporaryFolder() throws Exception {
        verify(temporaryWorldFolder).delete(Selectors.SELECT_ALL);
        verify(temporaryWorldFolder).delete();
    }
}
