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
        } catch (final IOException e) {
            log.error(ErrorMessage.TASK_FAILED, e);
        } finally {
            closeArchive();
        }
    }

    private void closeArchive() {
        try {
            // TODO: this doesn't seem appropriate, unless I *get* the
            // archiver.
            archive.close();
        } catch (final ArchiveException e) {
            log.warn(ErrorMessage.CANNOT_CLOSE_ARCHIVE);
        }
    }

    private void archiveWorld() throws IOException {
        createTemporaryFolder();
        saveAndCopyWorld();
        archiveCopiedWorld();
        deleteCopiedWorld();
    }

    private void saveAndCopyWorld() throws FileSystemException {
        world.setAutoSave(false);
        world.save();
        copyWorldToTemporaryFolder();
        world.setAutoSave(true);
    }

    private void createTemporaryFolder() throws FileSystemException {
        temporaryWorldFolder.createFolder();
    }

    private void copyWorldToTemporaryFolder() throws FileSystemException {
        temporaryWorldFolder.copyFrom(worldFolder, Selectors.SELECT_ALL);
    }

    private void archiveCopiedWorld() throws IOException {
        archiveFolder(temporaryWorldFolder, temporaryWorldFolder);
    }

    private void deleteCopiedWorld() throws FileSystemException {
        // TODO: do I really need to call both of these?
        temporaryWorldFolder.delete(Selectors.SELECT_ALL);
        temporaryWorldFolder.delete();
    }

    private void archiveFolder(final FileObject baseFolder,
            final FileObject folder) throws IOException {
        for (final FileObject file : folder.getChildren()) {
            archiveFileOrFolder(baseFolder, file);
        }
    }

    private void archiveFileOrFolder(final FileObject baseFolder,
            final FileObject file) throws IOException {
        if (isFolder(file)) {
            archiveFolder(baseFolder, file);
            return;
        }
        archiveFile(baseFolder, file);
    }

    private boolean isFolder(final FileObject file) throws FileSystemException {
        return file.getType().equals(FileType.FOLDER);
    }

    private void archiveFile(final FileObject baseFolder,
            final FileObject file) throws IOException {
        archive.write(relativeNameOf(baseFolder, file), file.getContent());
    }

    private String relativeNameOf(final FileObject baseFolder,
            final FileObject file) throws FileSystemException {
        return baseFolder.getName().getRelativeName(file.getName());
    }
}
