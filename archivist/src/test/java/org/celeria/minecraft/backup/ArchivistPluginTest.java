package org.celeria.minecraft.backup;

import static org.mockito.Mockito.*;
import java.io.*;
import java.lang.reflect.Field;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
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
    public void setUp() throws Exception {
        setFieldTo(plugin, "config", configuration);
        setFieldTo(plugin, "server", server);
        // TODO: make path less stupid.
        final Reader reader = Files.newReader(new File(
                "target/classes/plugin.yml"), Charsets.UTF_8);
        when(server.getScheduler()).thenReturn(scheduler);
        when(server.getPluginManager()).thenReturn(pluginManager);
        when(server.getPluginCommand("backup")).thenReturn(pluginCommand);
        when(configuration.getString(anyString(), anyString())).thenReturn(
                "DEFAULT_COMPRESSION");
        setFieldTo(plugin, "description", new PluginDescriptionFile(reader));
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
