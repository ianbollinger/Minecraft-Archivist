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

import static org.mockito.Mockito.*;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.google.inject.Inject;
import org.bukkit.Server;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.config.Configuration;
import org.jukito.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JukitoRunner.class)
public class ArchivistPluginTest {
    private static final int TIMEOUT = 1000 * 1;

    public static class Module extends JukitoModule {
        @Override
        protected void configureTest() {
            bindMock(Plugin.class);
            try {
                bindMock(Configuration.class).in(TestSingleton.class);
                bind(PluginCommand.class).toConstructor(
                        PluginCommand.class.getDeclaredConstructor(
                                String.class, Plugin.class));
            } catch (final NoSuchMethodException e) {
                addError(e);
            }
        }
    }

    @Inject private ArchivistPlugin plugin;
    @Inject private Configuration configuration;
    @Inject private Server server;
    @Inject private BukkitScheduler scheduler;
    @Inject private PluginManager pluginManager;
    @Inject private PluginCommand pluginCommand;

    @Before
    public void setUpMocks() {
        when(server.getScheduler()).thenReturn(scheduler);
        when(server.getPluginManager()).thenReturn(pluginManager);
        when(server.getPluginCommand("backup")).thenReturn(pluginCommand);
        when(configuration.getString(anyString(), anyString())).thenReturn(
                CompressionLevel.DEFAULT.toString());
    }

    @Before
    public void setUpPrivateFields() throws Exception {
        setFieldTo(plugin, "config", configuration);
        setFieldTo(plugin, "server", server);
    }

    @Before
    public void setUpDescription() throws Exception {
        final URL resource = Resources.getResource("plugin.yml");
        setFieldTo(plugin, "description", getDescription(resource));
    }

    private PluginDescriptionFile getDescription(final URL resource)
            throws Exception {
        final Reader reader = Resources.newReaderSupplier(resource,
                Charsets.UTF_8).getInput();
        try {
            return new PluginDescriptionFile(reader);
        } finally {
            reader.close();
        }
    }

    @Test(timeout = TIMEOUT)
    public void test() {
        plugin.onEnable();
        // TODO: verify something!
    }

    private void setFieldTo(final Object instance, final String name,
            final Object value) throws Exception {
        final Field field = JavaPlugin.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(instance, value);
    }
}
