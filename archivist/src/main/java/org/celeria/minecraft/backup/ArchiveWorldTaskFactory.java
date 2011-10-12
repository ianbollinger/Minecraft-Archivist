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

import java.io.*;
import java.util.zip.*;
import javax.annotation.concurrent.Immutable;
import com.google.inject.Inject;
import org.apache.commons.vfs2.*;
import org.bukkit.World;
import org.celeria.minecraft.backup.ArchiveWorldTask.TemporaryFolder;
import org.celeria.minecraft.backup.BackUpWorldsTask.BackupFolder;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.cal10n.LocLogger;

@Immutable
class ArchiveWorldTaskFactory implements WorldTaskFactory {
    private final LocLogger log;
    private final Checksum checksum;
    private final CompressionLevel compressionLevel;
    private final DateTimeFormatter dateTimeFormatter;
    private final FileProvider<FileSystemManager> fileSystemProvider;
    private final FileProvider<FileObject> temporaryFolderProvider;
    private final FileProvider<FileObject> backupFolderProvider;

    @Inject
    ArchiveWorldTaskFactory(
            final LocLogger log,
            final Checksum checksum,
            final CompressionLevel compressionLevel,
            final DateTimeFormatter dateFormatter,
            final FileProvider<FileSystemManager> fileSystemProvider,
            @TemporaryFolder
            final FileProvider<FileObject> temporaryFolderProvider,
            @BackupFolder final FileProvider<FileObject> backupFolderProvider) {
        this.log = log;
        this.checksum = checksum;
        this.compressionLevel = compressionLevel;
        this.dateTimeFormatter = dateFormatter;
        this.fileSystemProvider = fileSystemProvider;
        this.temporaryFolderProvider = temporaryFolderProvider;
        this.backupFolderProvider = backupFolderProvider;
    }

    @Override
    public WorldTask create(final World world) throws IOException {
        final String worldName = world.getName();
        final FileSystemManager fileSystem = fileSystemProvider.get();
        final FileObject worldFolder = fileSystem.resolveFile(worldName);
        return new ArchiveWorldTask(log, worldFolder,
                temporaryFolderFor(worldName, fileSystem),
                archiveFor(world, fileSystem), world);
    }

    private FileObject temporaryFolderFor(final String worldName,
            final FileSystemManager fileSystem) throws FileSystemException {
        return fileSystem.resolveFile(temporaryFolderProvider.get(), worldName);
    }

    private Archive archiveFor(final World world,
            final FileSystemManager fileSystem) throws FileSystemException {
        return archiveFor(fileFor(world, fileSystem));
    }

    private Archive archiveFor(final FileObject file)
            throws FileSystemException {
        final ZipOutputStream stream = zipStreamFor(file);
        stream.setLevel(compressionLevel.asInteger());
        return new Archive(stream);
    }

    private FileObject fileFor(final World world,
            final FileSystemManager fileSystem) throws FileSystemException {
        return fileSystem.resolveFile(getBackupFolder(), fileNameFor(world));
    }

    private ZipOutputStream zipStreamFor(final FileObject archiveFile)
            throws FileSystemException {
        final OutputStream checkedStream = new CheckedOutputStream(
                streamFor(archiveFile), checksum);
        return new ZipOutputStream(new BufferedOutputStream(checkedStream));
    }

    private FileObject getBackupFolder() throws FileSystemException {
        return backupFolderProvider.get();
    }

    private String fileNameFor(final World world) {
        return world.getName() + "_" + dateTimeFormatter.print(DateTime.now())
                + ".zip";
    }

    private OutputStream streamFor(final FileObject file)
            throws FileSystemException {
        return file.getContent().getOutputStream();
    }
}
