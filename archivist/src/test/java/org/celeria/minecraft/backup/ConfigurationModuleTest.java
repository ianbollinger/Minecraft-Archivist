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
import org.apache.commons.vfs2.*;
import org.bukkit.Server;
import org.bukkit.util.config.Configuration;
import org.jukito.JukitoRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.google.inject.Inject;

@RunWith(JukitoRunner.class)
public class ConfigurationModuleTest {
    @Inject private ConfigurationModule module;
    @Inject private Configuration configuration;

    @Test
    public void testProvideBackupFolder(
            final FileProvider<FileSystemManager> fileSystemManagerProvider,
            final FileSystemManager fileSystemManager, final FileObject file)
            throws Exception {
        when(fileSystemManagerProvider.get()).thenReturn(fileSystemManager);
        when(fileSystemManager.resolveFile(anyString())).thenReturn(file);
        module.provideBackupFolder(configuration, fileSystemManagerProvider);
        // TODO: verify something!
    }

    @Test
    public void testProvideBackUpEndedMessage() {
        module.provideBackUpEndedMessage(configuration);
        // TODO: verify something!
    }

    @Test
    public void testProvideBackUpStartedMessage() {
        module.provideBackUpStartedMessage(configuration);
        // TODO: verify something!
    }

    @Test
    public void testProvideCompressionLevel() {
        when(configuration.getString(anyString(), anyString())).thenReturn(
                CompressionLevel.DEFAULT_COMPRESSION.toString());
        module.provideCompressionLevel(configuration);
        // TODO: verify something!
    }

    @Test
    public void testProvideDurationToKeepBackups() {
        module.provideDurationToKeepBackups(configuration);
        // TODO: verify something!
    }

    @Test
    public void testProvideWorlds(final Server server) {
        module.provideWorlds(configuration, server);
        // TODO: verify something!
    }

    @Test
    public void testProvideBackUpInterval() {
        module.provideBackUpInterval(configuration);
        // TODO: verify something!
    }
}
