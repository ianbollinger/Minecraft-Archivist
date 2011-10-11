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
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import org.apache.commons.vfs2.*;
import org.slf4j.cal10n.LocLogger;

@Immutable
class Archive implements Closeable {
    private final LocLogger log;
    private final ZipOutputStream output;

    @Inject
    Archive(final LocLogger log, final ZipOutputStream output) {
        this.log = log;
        this.output = output;
    }

    @Override
    public void close() {
        close(output);
    }

    public void write(final String name, final FileContent content) {
        startEntry(entryFor(name, content));
        writeEntry(content);
        endEntry();
    }

    private ZipEntry entryFor(final String name,
            final FileContent content) {
        final ZipEntry entry = new ZipEntry(name);
        entry.setTime(timeLastModifiedOf(content));
        return entry;
    }

    private long timeLastModifiedOf(final FileContent content) {
        try {
            return content.getLastModifiedTime();
        } catch (final IOException e) {
            throw afterLogging(e, ErrorMessage.CANNOT_ACCESS_FILE,
                    content.getFile());
        }
    }

    private void writeEntry(final FileContent content) {
        final InputStream input = inputStreamFrom(content);
        try {
            copyStream(input);
        } finally {
            close(input);
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

    private void copyStream(final InputStream input) {
        try {
            ByteStreams.copy(input, output);
        } catch (final IOException e) {
            throw afterLogging(e, ErrorMessage.CANNOT_WRITE_TO_ARCHIVE);
        }
    }

    private void startEntry(final ZipEntry entry) {
        try {
            output.putNextEntry(entry);
        } catch (final ZipException e) {
            throw afterLogging(e, ErrorMessage.CANNOT_WRITE_TO_ARCHIVE);
        } catch (final IOException e) {
            throw afterLogging(e, ErrorMessage.CANNOT_WRITE_TO_ARCHIVE);
        }
    }

    private void endEntry() {
        try {
            output.closeEntry();
        } catch (final ZipException e) {
            throw afterLogging(e, ErrorMessage.CANNOT_WRITE_TO_ARCHIVE);
        } catch (final IOException e) {
            throw afterLogging(e, ErrorMessage.CANNOT_WRITE_TO_ARCHIVE);
        }
    }

    private void close(final Closeable input) {
        try {
            input.close();
        } catch (final IOException e) {
            log.warn(ErrorMessage.CANNOT_CLOSE_FILE);
        }
    }

    private RuntimeException afterLogging(final Throwable e,
            final ErrorMessage message, final Object... args) {
        log.error(message, args);
        throw new ArchiveException(e);
    }
}
