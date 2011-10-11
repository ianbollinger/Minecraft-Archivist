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
import java.util.List;
import java.util.zip.*;
import javax.annotation.concurrent.Immutable;
import com.google.common.collect.ImmutableList;
import com.google.inject.*;
import com.google.inject.throwingproviders.*;
import org.apache.commons.vfs2.*;
import org.bukkit.*;
import org.bukkit.command.*;
import org.celeria.minecraft.backup.ArchiveWorldTask.TemporaryFolder;
import org.celeria.minecraft.guice.BukkitPlugin;
import org.joda.time.*;
import org.joda.time.format.*;

@Immutable
class ArchivistModule extends AbstractModule {
    @Override
    protected void configure() {
        bindImplementations();
        bindProviders();
        install(new ConfigurationModule());
    }

    private void bindImplementations() {
        bind(BukkitPlugin.class).to(Archivist.class);
        bind(Checksum.class).to(Adler32.class);
        bind(CommandExecutor.class).to(ManualBackUpExecutor.class);
        bind(WorldTaskFactory.class).to(ArchiveWorldTaskFactory.class);
    }

    private void bindProviders() {
        ThrowingProviderBinder.create(binder())
                .bind(FileProvider.class, FileObject.class)
                .annotatedWith(TemporaryFolder.class)
                .to(TemporaryFolderProvider.class)
                .in(Singleton.class);
        ThrowingProviderBinder.create(binder())
                .bind(FileProvider.class, FileSystemManager.class)
                .to(TemporaryFolderProvider.class)
                .in(Singleton.class);
    }

    @Provides @Singleton
    public PluginCommand providePluginCommand(final Server server) {
        return server.getPluginCommand("backup");
    }

    @Provides @Singleton
    public DateTimeFormatter provideDateTimeFormatter() {
        final List<DateTimeFieldType> fields = ImmutableList
                .<DateTimeFieldType>of(year(), monthOfYear(), dayOfMonth(),
                        hourOfDay());
        return ISODateTimeFormat.forFields(fields, true, true);
    }

    @Provides
    public Instant provideCurrentTime() {
        return Instant.now();
    }
}
