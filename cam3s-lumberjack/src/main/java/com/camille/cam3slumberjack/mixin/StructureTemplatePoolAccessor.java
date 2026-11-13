package com.camille.cam3slumberjack.mixin;

import java.util.List;

import com.mojang.datafixers.util.Pair;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

/**
 * Write access to a template pool's pieces. {@code templates} holds each piece once per weight point
 * and is what village generation picks from; {@code rawTemplates} holds (piece, weight) pairs.
 */
@Mixin(StructureTemplatePool.class)
public interface StructureTemplatePoolAccessor {
	@Accessor("templates")
	ObjectArrayList<StructurePoolElement> cam3sLumberjack$getTemplates();

	@Accessor("rawTemplates")
	List<Pair<StructurePoolElement, Integer>> cam3sLumberjack$getRawTemplates();

	@Mutable
	@Accessor("rawTemplates")
	void cam3sLumberjack$setRawTemplates(List<Pair<StructurePoolElement, Integer>> rawTemplates);
}
