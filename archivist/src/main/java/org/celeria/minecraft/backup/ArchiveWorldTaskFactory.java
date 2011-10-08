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
    public WorldTask create(final World world) throws FileSystemException {
        // TODO: this can throw an exception, log it!
        final FileSystemManager fileSystem = fileSystemProvider.get();
        // TODO: this can throw an exception, log it!
        final FileObject temporaryFolder = temporaryFolderProvider.get();
        final FileObject worldFolder = folderFor(world, fileSystem);
        final FileObject temporaryWorldFolder = temporaryFolderFor(world,
                temporaryFolder, fileSystem);
        final ZipOutputStream archive = archiveFor(world, fileSystem);
        return new ArchiveWorldTask(log, worldFolder, temporaryWorldFolder,
                archive, world);
    }

    private FileObject folderFor(final World world,
            final FileSystemManager fileSystem) throws FileSystemException {
        final String worldName = world.getName();
        try {
            return fileSystem.resolveFile(worldName);
        } catch (final FileSystemException e) {
            log.error(LogMessage.INVALID_FILE_NAME, worldName);
            throw e;
        }
    }

    private FileObject temporaryFolderFor(final World world,
            final FileObject temporaryFolder,
            final FileSystemManager fileSystem)
            throws FileSystemException {
        final String worldName = world.getName();
        return resolveFile(worldName, temporaryFolder, fileSystem);
    }

    private FileObject resolveFile(final String name, final FileObject path,
            final FileSystemManager fileSystem) throws FileSystemException {
        try {
            return fileSystem.resolveFile(path, name);
        } catch (final FileSystemException e) {
            log.error(LogMessage.INVALID_FILE_NAME, name);
            throw e;
        }
    }

    private ZipOutputStream archiveFor(final World world,
            final FileSystemManager fileSystem) throws FileSystemException {
        // TODO: this can throw an exception, log it!
        final FileObject backupFolder = backupFolderProvider.get();
        final String name = world.getName() + "_"
                + dateTimeFormatter.print(DateTime.now()) + ".zip";
        final FileObject archiveFile = resolveFile(name, backupFolder,
                fileSystem);
        final OutputStream output;
        try {
            output = archiveFile.getContent().getOutputStream();
        } catch (final FileSystemException e) {
            log.error(LogMessage.ERROR_OPENING, name);
            throw e;
        }
        final OutputStream checkedOutput = new CheckedOutputStream(output,
                checksum);
        final OutputStream bufferedOutput = new BufferedOutputStream(
                checkedOutput);
        final ZipOutputStream zipOutput = new ZipOutputStream(bufferedOutput);
        zipOutput.setLevel(compressionLevel.asInteger());
        return zipOutput;
    }
}
