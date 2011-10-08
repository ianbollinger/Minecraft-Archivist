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
import org.apache.commons.vfs2.*;
import org.bukkit.World;
import org.jukito.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Matchers;

@RunWith(JukitoRunner.class)
public class ArchiveWorldTaskFactoryTest {
    public static class Module extends JukitoModule {
        @Override
        protected void configureTest() {
            bind(CompressionLevel.class).toInstance(
                    CompressionLevel.DEFAULT_COMPRESSION);
        }
    }

    @Inject private ArchiveWorldTaskFactory factory;
    @Inject private FileProvider<FileSystemManager> fileSystemManagerProvider;
    @Inject private FileSystemManager fileSystemManager;
    @Inject private FileObject file;
    @Inject private FileContent fileContent;

    @Before
    public void setUpMocks() throws Exception {
        when(fileSystemManagerProvider.get()).thenReturn(fileSystemManager);
        when(fileSystemManager.resolveFile(Matchers.<FileObject>any(),
                anyString())).thenReturn(file);
        when(file.getContent()).thenReturn(fileContent);
    }

    @Test
    public void testCreate(final World world) throws Exception {
        factory.create(world);
        // TODO: verify something!
    }
}
