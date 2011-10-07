package org.celeria.minecraft.backup;

import com.google.inject.throwingproviders.CheckedProvider;
import org.apache.commons.vfs2.*;

public interface FileProvider<T> extends CheckedProvider<T> {
    @Override
    T get() throws FileSystemException;
}
