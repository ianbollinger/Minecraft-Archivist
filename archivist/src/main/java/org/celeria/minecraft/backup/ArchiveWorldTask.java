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
import java.io.*;
import java.lang.annotation.*;
import java.util.zip.*;
import javax.annotation.concurrent.Immutable;
import com.google.inject.*;
import org.apache.commons.vfs2.*;
import org.bukkit.World;
import org.slf4j.cal10n.LocLogger;

@Immutable
class ArchiveWorldTask implements WorldTask {
    @BindingAnnotation @Target({FIELD, PARAMETER, METHOD}) @Retention(RUNTIME)
    public @interface WorldFolder {}
    @BindingAnnotation @Target({FIELD, PARAMETER, METHOD}) @Retention(RUNTIME)
    public @interface TemporaryFolder {}
    @BindingAnnotation @Target({FIELD, PARAMETER, METHOD}) @Retention(RUNTIME)
    public @interface TemporaryWorldFolder {}

    private static final int BUFFER_SIZE = 0x1000;
    private final LocLogger log;
    private final World world;
    private final FileObject worldFolder;
    private final FileObject temporaryWorldFolder;
    private final ZipOutputStream archive;

    @Inject
    ArchiveWorldTask(final LocLogger log,
            @WorldFolder final FileObject worldFolder,
            @TemporaryWorldFolder final FileObject temporaryWorldFolder,
            final ZipOutputStream archive, final World world) {
        this.log = log;
        this.world = world;
        this.worldFolder = worldFolder;
        this.temporaryWorldFolder = temporaryWorldFolder;
        this.archive = archive;
    }

    @Override
    public final void run() {
        try {
            archiveWorld();
        } catch (final WorldTaskException e) {
            log.error(ErrorMessage.TASK_FAILED, e.getCause());
        } finally {
            // TODO: this doesn't seem appropriate, unless I *get* the archive.
            close(archive);
        }
    }

    private void archiveWorld() {
        createTemporaryFolder();
        saveAndCopyWorld();
        archiveCopiedWorld();
        deleteCopiedWorld();
    }

    private void saveAndCopyWorld() {
        world.setAutoSave(false);
        world.save();
        copyWorldToTemporaryFolder();
        world.setAutoSave(true);
    }

    private void createTemporaryFolder() {
        try {
            temporaryWorldFolder.createFolder();
        } catch (final FileSystemException e) {
            log.error(ErrorMessage.CANNOT_CREATE_TEMPORARY_FOLDER,
                    temporaryWorldFolder);
            throw new WorldTaskException(e);
        }
    }

    private void copyWorldToTemporaryFolder() {
        try {
            temporaryWorldFolder.copyFrom(worldFolder, Selectors.SELECT_ALL);
        } catch (final IOException e) {
            log.error(ErrorMessage.CANNOT_COPY_WORLD, temporaryWorldFolder,
                    worldFolder);
            throw new WorldTaskException(e);
        }
    }

    private void archiveCopiedWorld() {
        archiveFolder(temporaryWorldFolder, temporaryWorldFolder);
    }

    private void deleteCopiedWorld() {
        try {
            // TODO: do I really need to call both of these?
            temporaryWorldFolder.delete(Selectors.SELECT_ALL);
            temporaryWorldFolder.delete();
        } catch (final FileSystemException e) {
            log.error(ErrorMessage.CANNOT_DELETE_TEMPORARY_FOLDER,
                    temporaryWorldFolder);
            throw new WorldTaskException(e);
        }
    }

    private void archiveFolder(final FileObject baseFolder,
            final FileObject folder) {
        for (final FileObject file : childrenOf(folder)) {
            archiveFile(baseFolder, file);
        }
    }

    private FileObject[] childrenOf(final FileObject folder) {
        try {
            return folder.getChildren();
        } catch (final FileSystemException e) {
            log.error(ErrorMessage.CANNOT_ACCESS_FOLDER, folder);
            throw new WorldTaskException(e);
        }
    }

    private void archiveFile(final FileObject baseFolder,
            final FileObject file) {
        if (typeOf(file).equals(FileType.FOLDER)) {
            archiveFolder(baseFolder, file);
            return;
        }
        writeEntry(baseFolder, file);
    }

    private FileType typeOf(final FileObject file) {
        try {
            return file.getType();
        } catch (final FileSystemException e) {
            log.error(ErrorMessage.CANNOT_DETERMINE_FILE_TYPE);
            throw new WorldTaskException(e);
        }
    }

    private void writeEntry(final FileObject baseFolder,
            final FileObject source) {
        final ZipEntry entry = getArchiveEntry(baseFolder, source);
        final FileContent content = contentOf(source);
        entry.setTime(timeLastModifiedOf(content));
        startArchiveEntry(entry);
        writeArchiveEntry(content);
        endArchiveEntry();
    }

    private void writeArchiveEntry(final FileContent content) {
        final InputStream input = inputStreamFrom(content);
        try {
            copyStream(input, archive);
        } finally {
            close(input);
        }
    }

    private ZipEntry getArchiveEntry(final FileObject baseFolder,
            final FileObject source) {
        final FileName name = source.getName();
        final FileName baseFolderName = baseFolder.getName();
        final String relativeName = relativeNameOf(name, baseFolderName);
        return new ZipEntry(relativeName);
    }

    private String relativeNameOf(final FileName name,
            final FileName baseFolderName) {
        try {
            return baseFolderName.getRelativeName(name);
        } catch (final FileSystemException e) {
            log.error(ErrorMessage.CANNOT_ACCESS_FILE, name);
            throw new WorldTaskException(e);
        }
    }

    private long timeLastModifiedOf(final FileContent content) {
        try {
            return content.getLastModifiedTime();
        } catch (final IOException e) {
            log.error(ErrorMessage.CANNOT_ACCESS_FILE, content.getFile());
            throw new WorldTaskException(e);
        }
    }

    private void startArchiveEntry(final ZipEntry entry) {
        try {
            archive.putNextEntry(entry);
        } catch (final ZipException e) {
            log.error(ErrorMessage.CANNOT_WRITE_TO_ARCHIVE);
            throw new WorldTaskException(e);
        } catch (final IOException e) {
            log.error(ErrorMessage.CANNOT_WRITE_TO_ARCHIVE);
            throw new WorldTaskException(e);
        }
    }

    private FileContent contentOf(final FileObject source) {
        try {
            return source.getContent();
        } catch (final FileSystemException e) {
            log.error(ErrorMessage.CANNOT_ACCESS_FILE, source);
            throw new WorldTaskException(e);
        }
    }

    private InputStream inputStreamFrom(final FileContent content) {
        try {
            return content.getInputStream();
        } catch (final FileSystemException e) {
            log.error(ErrorMessage.CANNOT_OPEN_FILE_FOR_READING,
                    content.getFile());
            throw new WorldTaskException(e);
        }
    }

    public void copyStream(final InputStream input, final OutputStream output) {
        final byte[] buffer = new byte[BUFFER_SIZE];
        while (true) {
            final int bytesRead = readIntoBuffer(input, buffer);
            if (bytesRead < 0) {
                break;
            }
            writeFromBuffer(output, buffer, bytesRead);
        }
    }

    private int readIntoBuffer(final InputStream input, final byte[] buffer) {
        try {
            return input.read(buffer);
        } catch (final IOException e) {
            log.error(ErrorMessage.CANNOT_READ_FILE);
            throw new WorldTaskException(e);
        }
    }

    private void writeFromBuffer(final OutputStream output,
            final byte[] buffer, final int bytesRead) {
        try {
            output.write(buffer, 0, bytesRead);
        } catch (final IOException e) {
            log.error(ErrorMessage.CANNOT_WRITE_TO_ARCHIVE);
            throw new WorldTaskException(e);
        }
    }

    private void close(final Closeable input) {
        try {
            input.close();
        } catch (final IOException e) {
            log.warn(ErrorMessage.CANNOT_CREATE_TEMPORARY_FOLDER);
        }
    }

    private void endArchiveEntry() {
        try {
            archive.closeEntry();
        } catch (final ZipException e) {
            log.error(ErrorMessage.CANNOT_WRITE_TO_ARCHIVE);
            throw new WorldTaskException(e);
        } catch (final IOException e) {
            log.error(ErrorMessage.CANNOT_WRITE_TO_ARCHIVE);
            throw new WorldTaskException(e);
        }
    }
}
