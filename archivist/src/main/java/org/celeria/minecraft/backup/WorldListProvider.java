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

import java.util.*;
import javax.annotation.concurrent.Immutable;
import com.google.common.collect.*;
import com.google.inject.*;
import org.bukkit.*;
import org.bukkit.util.config.Configuration;

@Immutable
class WorldListProvider implements Provider<Iterable<World>> {
    private final Configuration configuration;
    private final Server server;

    @Inject
    WorldListProvider(final Configuration configuration, final Server server) {
        this.configuration = configuration;
        this.server = server;
    }

    @Override
    public Iterable<World> get() {
        final List<String> worldNames = configuration.getStringList(
                "worlds", ImmutableList.<String>of());
        final List<World> worlds = server.getWorlds();
        if (worldNames.isEmpty()) {
            return worlds;
        }
        return filteredWorldListFrom(ImmutableSet.copyOf(worldNames), worlds);
    }

    private Set<World> filteredWorldListFrom(final Set<String> worldNames,
            final List<World> worldList) {
        final Set<World> worlds = Sets.newHashSet(worldList);
        for (final World world : worldList) {
            if (!worldNames.contains(world.getName())) {
                worlds.remove(world);
            }
        }
        return worlds;
    }
}
