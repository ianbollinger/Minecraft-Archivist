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
import org.jukito.JukitoRunner;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Matchers;

@RunWith(JukitoRunner.class)
public class TemporaryFolderProviderTest {
    @Inject private TemporaryFolderProvider provider;
    @Inject private FileProvider<FileSystemManager> fileSystemManagerProvider;
    @Inject private FileSystemManager fileSystemManager;
    @Inject private FileObject file;

    @Before
    public void setUp() throws Exception {
        when(fileSystemManagerProvider.get()).thenReturn(fileSystemManager);
        when(fileSystemManager.resolveFile(Matchers.<FileObject>any(),
                anyString())).thenReturn(file);
    }

    @Test
    public void testGet() throws Exception {
        provider.get();
        // TODO: verify something.
    }
}
