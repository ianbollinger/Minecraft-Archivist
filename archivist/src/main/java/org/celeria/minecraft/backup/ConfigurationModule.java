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

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.*;
import java.util.*;
import javax.annotation.concurrent.Immutable;
import com.google.common.collect.*;
import com.google.inject.*;
import com.google.inject.throwingproviders.CheckedProvides;
import com.google.inject.throwingproviders.ThrowingProviderBinder;
import org.apache.commons.vfs2.*;
import org.bukkit.*;
import org.bukkit.util.config.Configuration;
import org.celeria.minecraft.backup.Archivist.*;
import org.celeria.minecraft.backup.BackUpWorldsTask.*;

@Immutable
class ConfigurationModule extends AbstractModule {
    @BindingAnnotation @Target({FIELD, PARAMETER, METHOD}) @Retention(RUNTIME)
    public @interface DurationToKeepBackups {}

    private static final int DEFAULT_BACK_UP_INTERVAL = 3600 * 20;
    private static final int DEFAULT_DURATION_TO_KEEP_BACKUPS = 5 * 1000 * 60
            * 60 * 24;

    @Override
    protected void configure() {
        install(ThrowingProviderBinder.forModule(this));
    }

    @CheckedProvides(FileProvider.class) @BackupFolder @Singleton
    public FileObject provideBackupFolder(final Configuration configuration,
            final FileProvider<FileSystemManager> fileSystemProvider)
            throws FileSystemException {
        final String folderName = configuration.getString("backup-folder",
                "archives");
        final FileObject folder = fileSystemProvider.get().resolveFile(
                folderName);
        folder.createFolder();
        return folder;
    }

    @Provides @BackUpEndedMessage @Singleton
    public String provideBackUpEndedMessage(final Configuration configuration) {
        return configuration.getString("backup-ended-message", ChatColor.GREEN
                + "[Archivist] Backup ended.");
    }

    @Provides @BackUpStartedMessage @Singleton
    public String provideBackUpStartedMessage(
            final Configuration configuration) {
        return configuration.getString("backup-started-message",
                ChatColor.GREEN + "[Archivist] Backup started.");
    }

    @Provides @Singleton
    public CompressionLevel provideCompressionLevel(
            final Configuration configuration) {
        final String level = configuration.getString("compression-level",
                CompressionLevel.DEFAULT.toString());
        return CompressionLevel.valueOf(level);
    }

    @Provides @DurationToKeepBackups @Singleton
    public long provideDurationToKeepBackups(
            final Configuration configuration) {
        final int durationToKeepBackups = configuration.getInt(
                "duration-to-keep-backups", DEFAULT_DURATION_TO_KEEP_BACKUPS);
        return Math.max(1, durationToKeepBackups);
    }

    @Provides @Singleton
    public Iterable<World> provideWorlds(final Configuration configuration,
            final Server server) {
        final List<String> worldNamesList = configuration.getStringList(
                "worlds", ImmutableList.<String>of());
        final List<World> worldList = server.getWorlds();
        if (worldNamesList.isEmpty()) {
            return worldList;
        }
        return filteredWorldList(worldNamesList, worldList);
    }

    private Set<World> filteredWorldList(final List<String> worldNamesList,
            final List<World> worldList) {
        final Set<String> worldNames = ImmutableSet.copyOf(worldNamesList);
        final Set<World> worlds = Sets.newHashSet();
        for (final World world : worldList) {
            if (!worldNames.contains(world.getName())) {
                worlds.remove(world);
            }
        }
        return worlds;
    }

    @Provides @BackUpInterval @Singleton
    public long provideBackUpInterval(final Configuration configuration) {
        final int backUpInterval = configuration.getInt("backup-interval",
                DEFAULT_BACK_UP_INTERVAL);
        return Math.max(1, backUpInterval);
    }
}
