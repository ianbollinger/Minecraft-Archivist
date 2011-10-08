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

import static org.junit.Assert.*;
import java.util.zip.Deflater;
import org.junit.Test;

public class CompressionLevelTest {
    @Test
    public void testAsInteger() {
        assertEquals(Deflater.DEFAULT_COMPRESSION,
                CompressionLevel.DEFAULT.asInteger());
    }
}
