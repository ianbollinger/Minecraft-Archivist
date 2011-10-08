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
    public WorldTask create(final World world) {
        final String worldName = world.getName();
        final FileSystemManager fileSystem = getFileSystemManager();
        final FileObject temporaryWorldFolder = resolveFile(worldName,
                getTemporaryFolder(), fileSystem);
        return new ArchiveWorldTask(log, folderFor(worldName, fileSystem),
                temporaryWorldFolder, archiveFor(world, fileSystem), world);
    }

    private FileSystemManager getFileSystemManager() {
        try {
            return fileSystemProvider.get();
        } catch (final FileSystemException e) {
            log.error(ErrorMessage.CANNOT_ACCESS_FILE_SYSTEM);
            throw new WorldTaskException(e);
        }
    }

    private FileObject getTemporaryFolder() {
        try {
            return temporaryFolderProvider.get();
        } catch (final FileSystemException e) {
            log.error(ErrorMessage.CANNOT_ACCESS_TEMPORARY_FOLDER);
            throw new WorldTaskException(e);
        }
    }

    private FileObject folderFor(final String name,
            final FileSystemManager fileSystem) {
        try {
            return fileSystem.resolveFile(name);
        } catch (final FileSystemException e) {
            log.error(ErrorMessage.INVALID_FILE_NAME, name);
            throw new WorldTaskException(e);
        }
    }

    private FileObject resolveFile(final String name, final FileObject path,
            final FileSystemManager fileSystem) {
        try {
            return fileSystem.resolveFile(path, name);
        } catch (final FileSystemException e) {
            log.error(ErrorMessage.INVALID_FILE_NAME, name);
            throw new WorldTaskException(e);
        }
    }

    private ZipOutputStream archiveFor(final World world,
            final FileSystemManager fileSystem) {
        final FileObject archiveFile = getArchiveFile(world, fileSystem);
        final OutputStream output = getOutputStream(archiveFile);
        final OutputStream checkedOutput = new CheckedOutputStream(output,
                checksum);
        final OutputStream bufferedOutput = new BufferedOutputStream(
                checkedOutput);
        final ZipOutputStream zipOutput = new ZipOutputStream(bufferedOutput);
        zipOutput.setLevel(compressionLevel.asInteger());
        return zipOutput;
    }

    private FileObject getArchiveFile(final World world,
            final FileSystemManager fileSystem) {
        final FileObject backupFolder;
        try {
            backupFolder = backupFolderProvider.get();
        } catch (final FileSystemException e) {
            log.error(ErrorMessage.CANNOT_ACCESS_BACKUP_FOLDER);
            throw new WorldTaskException(e);
        }
        final String name = world.getName() + "_"
                + dateTimeFormatter.print(DateTime.now()) + ".zip";
        return resolveFile(name, backupFolder, fileSystem);
    }

    private OutputStream getOutputStream(final FileObject file) {
        try {
            return contentOf(file).getOutputStream();
        } catch (final FileSystemException e) {
            log.error(ErrorMessage.CANNOT_OPEN_FILE_FOR_WRITING, file);
            throw new WorldTaskException(e);
        }
    }

    private FileContent contentOf(final FileObject file) {
        try {
            return file.getContent();
        } catch (final FileSystemException e) {
            log.error(ErrorMessage.CANNOT_OPEN_FILE_FOR_WRITING, file);
            throw new WorldTaskException(e);
        }
    }
}
