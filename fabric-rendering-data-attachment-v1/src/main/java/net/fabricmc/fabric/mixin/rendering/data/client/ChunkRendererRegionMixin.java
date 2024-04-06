/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
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

package net.fabricmc.fabric.mixin.rendering.data.client;

import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import org.spongepowered.asm.mixin.Mixin;


import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;

/**
 * Since {@link RenderAttachedBlockView} is only automatically implemented on {@link net.minecraft.world.level.LevelReader} instances and
 * {@link RenderChunkRegion} does not implement {@link net.minecraft.world.level.LevelReader}, this mixin manually implements
 * {@link RenderAttachedBlockView} on {@link RenderChunkRegion}. The BlockView API v2 implementation ensures
 * that all default method implementations of {@link RenderAttachedBlockView} work here automatically.
 */
@Mixin(RenderChunkRegion.class)
public abstract class ChunkRendererRegionMixin implements RenderAttachedBlockView {
}
