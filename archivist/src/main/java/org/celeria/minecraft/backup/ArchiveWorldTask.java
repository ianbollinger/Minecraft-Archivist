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

    private final LocLogger log;
    private final World world;
    private final FileObject worldFolder;
    private final FileObject temporaryWorldFolder;
    private final Archive archive;

    @Inject
    ArchiveWorldTask(final LocLogger log,
            @WorldFolder final FileObject worldFolder,
            @TemporaryWorldFolder final FileObject temporaryWorldFolder,
            final Archive archiver, final World world) {
        this.log = log;
        this.world = world;
        this.worldFolder = worldFolder;
        this.temporaryWorldFolder = temporaryWorldFolder;
        this.archive = archiver;
    }

    @Override
    public final void run() {
        try {
            archiveWorld();
        } catch (final WorldTaskException e) {
            log.error(ErrorMessage.TASK_FAILED, e.getCause());
        } finally {
            try {
                // TODO: this doesn't seem appropriate, unless I *get* the
                // archiver.
                archive.close();
            } catch (final IOException e) {
                log.warn(ErrorMessage.CANNOT_CLOSE_ARCHIVE);
            }
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
        final String name = relativeNameOf(file.getName(),
                baseFolder.getName());
        try {
            archive.write(name, contentOf(file));
        } catch (final ArchiveException e){
            throw new WorldTaskException(e);
        }
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

    private String relativeNameOf(final FileName name,
            final FileName baseFolderName) {
        try {
            return baseFolderName.getRelativeName(name);
        } catch (final FileSystemException e) {
            throw afterLogging(e, ErrorMessage.CANNOT_ACCESS_FILE, name);
        }
    }

    private FileContent contentOf(final FileObject source) {
        try {
            return source.getContent();
        } catch (final FileSystemException e) {
            throw afterLogging(e, ErrorMessage.CANNOT_ACCESS_FILE, source);
        }
    }

    private RuntimeException afterLogging(final Throwable e,
            final ErrorMessage message, final Object... args) {
        log.error(message, args);
        throw new WorldTaskException(e);
    }
}
