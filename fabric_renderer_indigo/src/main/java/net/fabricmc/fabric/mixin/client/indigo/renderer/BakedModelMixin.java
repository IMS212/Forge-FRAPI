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

package net.fabricmc.fabric.mixin.client.indigo.renderer;

import java.util.function.Supplier;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.AbstractBlockRenderContext;
import net.fabricmc.fabric.impl.renderer.VanillaModelEncoder;

@Mixin(BakedModel.class)
public interface BakedModelMixin extends FabricBakedModel {
	/**
	 * Override the fallback path to shade vanilla quads differently.
	 */
	@Override
	default void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, RenderType renderType, ModelData modelData, Supplier<RandomSource> randomSupplier, RenderContext context) {
		VanillaModelEncoder.emitBlockQuads((BakedModel) this, state, renderType, modelData, randomSupplier, context, ((AbstractBlockRenderContext) context).getVanillaModelEmitter());
	}
}
