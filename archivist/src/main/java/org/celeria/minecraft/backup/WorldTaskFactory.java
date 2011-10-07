package org.celeria.minecraft.backup;

import org.apache.commons.vfs2.FileSystemException;
import org.bukkit.World;

interface WorldTaskFactory {
    WorldTask create(World world) throws FileSystemException;
}
