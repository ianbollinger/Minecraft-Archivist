package org.celeria.minecraft.guice;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;
import java.lang.annotation.*;
import com.google.inject.BindingAnnotation;

public interface BukkitPlugin extends Runnable {
    @BindingAnnotation @Target({FIELD, PARAMETER, METHOD}) @Retention(RUNTIME)
    public @interface PluginVersion {}

    void stop();
}
