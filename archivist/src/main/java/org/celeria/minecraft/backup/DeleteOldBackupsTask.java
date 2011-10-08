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
import javax.annotation.concurrent.Immutable;
import com.google.inject.*;
import org.apache.commons.vfs2.*;
import org.celeria.minecraft.backup.BackUpWorldsTask.BackupFolder;
import org.celeria.minecraft.backup.ConfigurationModule.DurationToKeepBackups;
import org.slf4j.cal10n.LocLogger;

@Immutable
class DeleteOldBackupsTask implements Runnable {
    @BindingAnnotation @Target({FIELD, PARAMETER, METHOD}) @Retention(RUNTIME)
    public @interface CurrentTime {}

    private final LocLogger log;
    private final FileProvider<FileObject> backupFolderProvider;
    private final long durationToKeepBackups;
    private final long currentTime;

    @Inject
    DeleteOldBackupsTask(final LocLogger log,
            @BackupFolder
            final FileProvider<FileObject> backupFolderProvider,
            @DurationToKeepBackups final long durationToKeepBackups,
            @CurrentTime final long currentTime) {
        this.log = log;
        this.backupFolderProvider = backupFolderProvider;
        this.durationToKeepBackups = durationToKeepBackups;
        this.currentTime = currentTime;
    }

    @Override
    public void run() {
        try {
            final FileObject backupFolder = backupFolderProvider.get();
            for (final FileObject backup : backupFolder.getChildren()) {
                deleteBackupIfOld(backup);
            }
        } catch (final FileSystemException e) {
            log.error(ErrorMessage.CANNOT_ACCESS_BACKUP_FOLDER);
        }
    }

    private void deleteBackupIfOld(final FileObject backup) {
        try {
            if (backupIsOld(backup)) {
                deleteBackup(backup);
            }
        } catch (final FileSystemException e) {
            log.error(ErrorMessage.CANNOT_ACCESS_BACKUP, backup);
        }
    }

    private boolean backupIsOld(final FileObject backup)
            throws FileSystemException {
        final long lastModifiedTime = backup.getContent().getLastModifiedTime();
        return currentTime - durationToKeepBackups > lastModifiedTime;
    }

    private void deleteBackup(final FileObject backup) {
        try {
            backup.delete();
            log.info(LogMessage.DELETED_BACKUP, backup);
        } catch (final FileSystemException e) {
            log.warn(ErrorMessage.CANNOT_DELETE_BACKUP, backup);
        }
    }
}
