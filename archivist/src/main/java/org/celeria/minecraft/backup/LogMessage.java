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

import ch.qos.cal10n.*;

@BaseName("log_message")
@LocaleData({@Locale("en_US")})
public enum LogMessage {
    ERROR_COPYING,
    ERROR_ARCHIVING,
    ERROR_DELETING_TEMPORARY_FOLDER,
    ERROR_CREATING,
    PLUGIN_ENABLED,
    PLUGIN_DISABLED,
    BACKING_UP_WORLD,
    FOLDER_INACCESSIBLE,
    FILE_INACCESSIBLE,
    ERROR_DELETING_BACKUP,
    DELETED_BACKUP,
    INVALID_FILE_NAME,
    ERROR_OPENING,
    
    
    ERROR_CLOSING,
    ERROR_ARCHIVE_FORMAT,
    ERROR_WRITING_TO_ARCHIVE,
    ERROR_READING,
    ERROR_OPENING_FOR_READING,
    TASK_FAILED,
    ERROR_ACCESSING_FILE,
    ERROR_DETERMINING_FILE_TYPE
}
