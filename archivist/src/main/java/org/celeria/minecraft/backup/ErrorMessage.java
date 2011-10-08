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

import ch.qos.cal10n.BaseName;
import ch.qos.cal10n.Locale;
import ch.qos.cal10n.LocaleData;

@BaseName("error_message")
@LocaleData({@Locale("en_US")})
public enum ErrorMessage {
    CANNOT_ACCESS_BACKUP,
    CANNOT_ACCESS_BACKUP_FOLDER,
    CANNOT_ACCESS_FILE,
    CANNOT_ACCESS_FILE_SYSTEM,
    CANNOT_ACCESS_FOLDER,
    CANNOT_ACCESS_TEMPORARY_FOLDER,
    CANNOT_ARCHIVE_TEMPORARY_FOLDER,
    CANNOT_COPY_WORLD,
    CANNOT_CREATE_TEMPORARY_FOLDER,
    CANNOT_DELETE_BACKUP,
    CANNOT_DELETE_TEMPORARY_FOLDER,
    CANNOT_DETERMINE_FILE_TYPE,
    CANNOT_OPEN_FILE_FOR_READING,
    CANNOT_OPEN_FILE_FOR_WRITING,
    CANNOT_READ_FILE,
    CANNOT_WRITE_TO_ARCHIVE,
    INVALID_FILE_NAME,
    TASK_FAILED
}
