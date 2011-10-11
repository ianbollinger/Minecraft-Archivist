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

import com.google.inject.*;
import org.apache.commons.vfs2.*;

@Singleton
class TemporaryFolderProvider implements FileProvider<FileObject> {
    private static final int TEMPORARY_FOLDER_ATTEMPTS = 10000;
    private final FileProvider<FileSystemManager> fileSystemManagerProvider;

    @Inject
    TemporaryFolderProvider(
            final FileProvider<FileSystemManager> fileSystemManagerProvider) {
        this.fileSystemManagerProvider = fileSystemManagerProvider;
    }

    @Override
    public FileObject get() throws FileSystemException {
        final FileSystemManager fileSystem = fileSystemManagerProvider.get();
        final FileObject folder = getTemporaryFolder(fileSystem);
        final String childName = System.currentTimeMillis() + "-";
        return getChildFolder(fileSystem, folder, childName);
    }

    private FileObject getChildFolder(final FileSystemManager fileSystem,
            final FileObject baseFolder, final String baseName)
            throws FileSystemException {
        for (int counter = 0; counter < TEMPORARY_FOLDER_ATTEMPTS; ++counter) {
            final FileObject temporaryFolder = fileSystem.resolveFile(
                    baseFolder, baseName + counter);
            if (!temporaryFolder.exists()) {
                temporaryFolder.createFolder();
                return temporaryFolder;
            }
        }
        throw new FileSystemException("Failed to create directory within "
                + TEMPORARY_FOLDER_ATTEMPTS + " attempts (tried " + baseName
                + "0 to " + baseName + (TEMPORARY_FOLDER_ATTEMPTS - 1) + ')');
    }

    private FileObject getTemporaryFolder(final FileSystemManager fileSystem)
            throws FileSystemException {
        return fileSystem.resolveFile(System.getProperty("java.io.tmpdir"));
    }
}
