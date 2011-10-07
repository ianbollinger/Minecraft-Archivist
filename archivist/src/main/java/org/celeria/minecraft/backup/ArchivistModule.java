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

import static org.joda.time.DateTimeFieldType.*;
import java.io.File;
import java.util.List;
import java.util.zip.*;
import com.google.common.collect.ImmutableList;
import com.google.inject.*;
import com.google.inject.throwingproviders.*;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.cache.DefaultFilesCache;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider;
import org.bukkit.*;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.celeria.minecraft.backup.ArchiveWorldTask.*;
import org.celeria.minecraft.backup.DeleteOldBackupsTask.CurrentTime;
import org.celeria.minecraft.guice.BukkitPlugin;
import org.joda.time.DateTimeFieldType;
import org.joda.time.format.*;

class ArchivistModule extends AbstractModule {
    private static final int TEMPORARY_FOLDER_ATTEMPTS = 10000;

    @Override
    protected void configure() {
        bind(BukkitPlugin.class).to(Archivist.class);
        bind(Checksum.class).to(Adler32.class);
        bind(CommandExecutor.class).to(ManualBackUpExecutor.class);
        install(ThrowingProviderBinder.forModule(this));
        install(new ConfigurationModule());
        bind(WorldTaskFactory.class).to(ArchiveWorldTaskFactory.class);
    }

    @Provides
    @Singleton
    public PluginCommand providePluginCommand(final Server server) {
        return server.getPluginCommand("backup");
    }

    @Provides
    @Singleton
    public DateTimeFormatter provideDateTimeFormater() {
        final List<DateTimeFieldType> fields = ImmutableList
                .<DateTimeFieldType>of(year(), monthOfYear(), dayOfMonth(),
                        hourOfDay());
        return ISODateTimeFormat.forFields(fields, true, true);
    }

    @CheckedProvides(FileProvider.class)
    @TemporaryFolder
    @Singleton
    public FileObject provideTemporaryFolder(
            final FileProvider<FileSystemManager> fileSystemProvider)
            throws FileSystemException {
        final FileSystemManager fileSystem = fileSystemProvider.get();
        final String folderName = System.getProperty("java.io.tmpdir");
        final FileObject baseFolder = fileSystem.resolveFile(folderName);
        final String baseName = System.currentTimeMillis() + "-";
        for (int counter = 0; counter < TEMPORARY_FOLDER_ATTEMPTS; ++counter) {
            final FileObject temporaryFolder = fileSystem.resolveFile(
                    baseFolder, baseName + counter);
            if (!temporaryFolder.exists()) {
                temporaryFolder.createFolder();
                return temporaryFolder;
            }
        }
        throw new FileSystemException("Failed to create directory within "
                + TEMPORARY_FOLDER_ATTEMPTS + " attempts (tried " + baseName
                + "0 to " + baseName + (TEMPORARY_FOLDER_ATTEMPTS - 1) + ')');
    }

    @Provides
    @CurrentTime
    public long provideCurrentTime() {
        return System.currentTimeMillis();
    }

    @CheckedProvides(FileProvider.class)
    @Singleton
    public FileSystemManager provideFileSystemManager()
            throws FileSystemException {
        final DefaultFileSystemManager fileSystemManager = new DefaultFileSystemManager();
        fileSystemManager.addProvider("file", new DefaultLocalFileProvider());
        fileSystemManager.setFilesCache(new DefaultFilesCache());
        fileSystemManager.setCacheStrategy(CacheStrategy.ON_RESOLVE);
        fileSystemManager.setBaseFile(new File("."));
        fileSystemManager.init();
        return fileSystemManager;
    }
}
