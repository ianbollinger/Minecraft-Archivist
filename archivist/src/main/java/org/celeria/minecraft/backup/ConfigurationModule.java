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

import static org.celeria.minecraft.backup.ConfigurationKey.*;
import javax.annotation.concurrent.Immutable;
import com.google.inject.*;
import com.google.inject.throwingproviders.*;
import org.apache.commons.vfs2.*;
import org.bukkit.*;
import org.bukkit.util.config.Configuration;
import org.celeria.minecraft.backup.BackUpWorldsTask.*;
import org.joda.time.*;

@Immutable
class ConfigurationModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(new TypeLiteral<Iterable<World>>() {})
                .toProvider(WorldListProvider.class).in(Singleton.class);
        install(ThrowingProviderBinder.forModule(this));
    }

    @CheckedProvides(FileProvider.class) @BackupFolder @Singleton
    public FileObject provideBackupFolder(final Configuration configuration,
            final FileProvider<FileSystemManager> fileSystemProvider)
            throws FileSystemException {
        final String folderName = getProperty(configuration, BACKUP_FOLDER);
        final FileSystemManager fileSystemManager = fileSystemProvider.get();
        final FileObject folder = fileSystemManager.resolveFile(folderName);
        folder.createFolder();
        return folder;
    }

    @Provides @BackUpEndedMessage @Singleton
    public String provideBackUpEndedMessage(final Configuration configuration) {
        return configuration.getString(getProperty(configuration,
                BACK_UP_ENDED_MESSAGE));
    }

    @Provides @BackUpStartedMessage @Singleton
    public String provideBackUpStartedMessage(
            final Configuration configuration) {
        return configuration.getString(getProperty(configuration,
                BACK_UP_STARTED_MESSAGE));
    }

    @Provides @Singleton
    public CompressionLevel provideCompressionLevel(
            final Configuration configuration) {
        final String level = getProperty(configuration, COMPRESSION_LEVEL);
        return CompressionLevel.valueOf(level);
    }

    @Provides @Singleton
    public Duration provideDurationToKeepBackups(
            final Configuration configuration) {
        final String durationToKeepBackups = getProperty(configuration,
                ConfigurationKey.DURATION_TO_KEEP_BACKUPS);
        return Duration.parse(durationToKeepBackups);
    }

    @Provides @Singleton
    public Period provideBackUpPeriod(final Configuration configuration) {
        final String backUpPeriod = getProperty(configuration,
                ConfigurationKey.BACK_UP_PERIOD);
        return Period.parse(backUpPeriod);
    }

    private static String getProperty(final Configuration configuration,
            final ConfigurationKey key) {
        return configuration.getString(key.key(), key.defaultValue());
    }
}
