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
import com.google.common.io.*;
import com.google.inject.Inject;
import org.apache.commons.vfs2.*;

@Immutable
class Archive implements Closeable {
    private final ZipOutputStream output;

    @Inject
    Archive(final ZipOutputStream output) {
        this.output = output;
    }

    @Override
    public void close() throws ArchiveException {
        try {
            output.close();
        } catch (final IOException e) {
            throw new ArchiveException("Could not close archive.", e);
        }
    }

    public void write(final String name, final FileContent content)
            throws ArchiveException {
        try {
            output.putNextEntry(entryFor(name, content));
            writeEntry(content);
            output.closeEntry();
        } catch (final IOException e) {
            throw new ArchiveException("Could not write file to archive.", e);
        }
    }

    private ZipEntry entryFor(final String name, final FileContent content)
            throws FileSystemException {
        final ZipEntry entry = new ZipEntry(name);
        entry.setTime(content.getLastModifiedTime());
        return entry;
    }

    private void writeEntry(final FileContent content) throws IOException {
        final InputStream input = inputStreamFrom(content);
        boolean threw = true;
        try {
            ByteStreams.copy(input, output);
            threw = false;
        } finally {
            Closeables.close(input, threw);
        }
    }

    private InputStream inputStreamFrom(final FileContent content)
            throws FileSystemException {
        return content.getInputStream();
    }
}
