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
import com.google.common.io.ByteStreams;
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
            throw afterLogging(e, ErrorMessage.CANNOT_CREATE_TEMPORARY_FOLDER,
                    temporaryWorldFolder);
        }
    }

    private void copyWorldToTemporaryFolder() {
        try {
            temporaryWorldFolder.copyFrom(worldFolder, Selectors.SELECT_ALL);
        } catch (final IOException e) {
            throw afterLogging(e, ErrorMessage.CANNOT_COPY_WORLD,
                    temporaryWorldFolder, worldFolder);
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
            throw afterLogging(e, ErrorMessage.CANNOT_DELETE_TEMPORARY_FOLDER,
                    temporaryWorldFolder);
        }
    }

    private void archiveFolder(final FileObject baseFolder,
            final FileObject folder) {
        for (final FileObject file : childrenOf(folder)) {
            archiveFileOrFolder(baseFolder, file);
        }
    }

    private FileObject[] childrenOf(final FileObject folder) {
        try {
            return folder.getChildren();
        } catch (final FileSystemException e) {
            throw afterLogging(e, ErrorMessage.CANNOT_ACCESS_FOLDER, folder);
        }
    }

    private void archiveFileOrFolder(final FileObject baseFolder,
            final FileObject file) {
        if (isFolder(file)) {
            archiveFolder(baseFolder, file);
            return;
        }
        archiveFile(baseFolder, file);
    }

    private void archiveFile(final FileObject baseFolder,
            final FileObject file) {
        final FileContent content = contentOf(file);
        final ZipEntry entry = archiveEntryFor(file, baseFolder, content);
        createArchiveEntry(entry, content);
    }

    private boolean isFolder(final FileObject file) {
        return typeOf(file).equals(FileType.FOLDER);
    }

    private FileType typeOf(final FileObject file) {
        try {
            return file.getType();
        } catch (final FileSystemException e) {
            throw afterLogging(e, ErrorMessage.CANNOT_DETERMINE_FILE_TYPE);
        }
    }

    private void createArchiveEntry(final ZipEntry entry,
            final FileContent content) {
        startArchiveEntry(entry);
        writeArchiveEntry(content);
        endArchiveEntry();
    }

    private ZipEntry archiveEntryFor(final FileObject file,
            final FileObject baseFolder, final FileContent content) {
        final String name = relativeNameOf(file.getName(),
                baseFolder.getName());
        final ZipEntry archiveEntry = archiveEntryFor(name, content);
        return archiveEntry;
    }

    private ZipEntry archiveEntryFor(final String name,
            final FileContent content) {
        final ZipEntry entry = new ZipEntry(name);
        entry.setTime(timeLastModifiedOf(content));
        return entry;
    }

    private void writeArchiveEntry(final FileContent content) {
        final InputStream input = inputStreamFrom(content);
        try {
            copyStream(input, archive);
        } finally {
            close(input);
        }
    }

    private String relativeNameOf(final FileName name,
            final FileName baseFolderName) {
        try {
            return baseFolderName.getRelativeName(name);
        } catch (final FileSystemException e) {
            throw afterLogging(e, ErrorMessage.CANNOT_ACCESS_FILE, name);
        }
    }

    private long timeLastModifiedOf(final FileContent content) {
        try {
            return content.getLastModifiedTime();
        } catch (final IOException e) {
            throw afterLogging(e, ErrorMessage.CANNOT_ACCESS_FILE,
                    content.getFile());
        }
    }

    private void startArchiveEntry(final ZipEntry entry) {
        try {
            archive.putNextEntry(entry);
        } catch (final ZipException e) {
            throw afterLogging(e, ErrorMessage.CANNOT_WRITE_TO_ARCHIVE);
        } catch (final IOException e) {
            throw afterLogging(e, ErrorMessage.CANNOT_WRITE_TO_ARCHIVE);
        }
    }

    private FileContent contentOf(final FileObject source) {
        try {
            return source.getContent();
        } catch (final FileSystemException e) {
            throw afterLogging(e, ErrorMessage.CANNOT_ACCESS_FILE, source);
        }
    }

    private InputStream inputStreamFrom(final FileContent content) {
        try {
            return content.getInputStream();
        } catch (final FileSystemException e) {
            throw afterLogging(e, ErrorMessage.CANNOT_OPEN_FILE_FOR_READING,
                    content.getFile());
        }
    }

    public void copyStream(final InputStream input, final OutputStream output) {
        try {
            ByteStreams.copy(input, output);
        } catch (final IOException e) {
            throw afterLogging(e, ErrorMessage.CANNOT_WRITE_TO_ARCHIVE);
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
            throw afterLogging(e, ErrorMessage.CANNOT_WRITE_TO_ARCHIVE);
        } catch (final IOException e) {
            throw afterLogging(e, ErrorMessage.CANNOT_WRITE_TO_ARCHIVE);
        }
    }

    private RuntimeException afterLogging(final Throwable e,
            final ErrorMessage message, final Object... args) {
        log.error(message, args);
        throw new WorldTaskException(e);
    }
}
