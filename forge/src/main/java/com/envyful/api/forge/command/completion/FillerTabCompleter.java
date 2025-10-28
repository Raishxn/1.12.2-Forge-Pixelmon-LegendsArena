package com.envyful.api.forge.command.completion;

import com.envyful.api.command.injector.TabCompleter;
import net.minecraft.entity.player.EntityPlayerMP;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

public class FillerTabCompleter implements TabCompleter<String, EntityPlayerMP> {
    @Override
    public Class<EntityPlayerMP> getSenderClass() {
        return EntityPlayerMP.class;
    }

    @Override
    public Class<String> getCompletedClass() {
        return String.class;
    }

    @Override
    public List<String> getCompletions(EntityPlayerMP sender, String[] currentData, Annotation... completionData) {
        return Collections.emptyList();
    }
}
