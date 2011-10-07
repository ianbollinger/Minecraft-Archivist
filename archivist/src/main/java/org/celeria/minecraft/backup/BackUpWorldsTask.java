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
import com.google.inject.*;
import org.apache.commons.vfs2.*;
import org.bukkit.*;
import org.celeria.minecraft.guice.TaskScheduler;
import org.slf4j.cal10n.LocLogger;

public class BackUpWorldsTask implements Runnable {
    @BindingAnnotation @Target({FIELD, PARAMETER, METHOD}) @Retention(RUNTIME)
    public @interface BackUpEndedMessage {}
    @BindingAnnotation @Target({FIELD, PARAMETER, METHOD}) @Retention(RUNTIME)
    public @interface BackUpStartedMessage {}
    @BindingAnnotation @Target({FIELD, PARAMETER, METHOD}) @Retention(RUNTIME)
    public @interface BackupFolder {}

    private final LocLogger log;
    private final Server server;
    private final TaskScheduler scheduler;
    private final Iterable<World> worlds;
    private final WorldTaskFactory worldTaskFactory;
    private final String backUpEndedMessage;
    private final String backUpStartedMessage;

    @Inject
    BackUpWorldsTask(final LocLogger log, final Server server,
            final TaskScheduler scheduler, final Iterable<World> worlds,
            final WorldTaskFactory worldTaskFactory,
            @BackUpEndedMessage final String backUpEndedMessage,
            @BackUpStartedMessage final String backUpStartedMessage) {
        this.log = log;
        this.server = server;
        this.scheduler = scheduler;
        this.worlds = worlds;
        this.worldTaskFactory = worldTaskFactory;
        this.backUpEndedMessage = backUpEndedMessage;
        this.backUpStartedMessage = backUpStartedMessage;
    }

    @Override
    public void run() {
        sendMessage(backUpStartedMessage);
        server.savePlayers();
        for (final World world : worlds) {
            runBackUpTask(world);
        }
        sendMessage(backUpEndedMessage);
    }

    private void sendMessage(final String message) {
        if (message.isEmpty()) {
            return;
        }
        server.broadcastMessage(message);
    }

    private void runBackUpTask(final World world) {
        log.info(LogMessage.BACKING_UP_WORLD, world.getName());
        final WorldTask factory;
        try {
            factory = worldTaskFactory.create(world);
        } catch (final FileSystemException e) {
            return;
        }
        scheduler.runAsynchronousTask(factory);
    }
}
