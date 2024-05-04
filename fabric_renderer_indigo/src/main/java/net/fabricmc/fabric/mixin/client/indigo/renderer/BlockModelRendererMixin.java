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

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.VanillaAoHelper;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.BlockRenderContext;

@Mixin(ModelBlockRenderer.class)
public abstract class BlockModelRendererMixin {
	@Unique
	private final ThreadLocal<BlockRenderContext> fabric_contexts = ThreadLocal.withInitial(BlockRenderContext::new);

	@Inject(at = @At("HEAD"), method = "tesselateBlock(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/client/resources/model/BakedModel;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;ZLnet/minecraft/util/RandomSource;JILnet/neoforged/neoforge/client/model/data/ModelData;Lnet/minecraft/client/renderer/RenderType;)V", cancellable = true)
	private void hookRender(BlockAndTintGetter blockView, BakedModel model, BlockState state, BlockPos pos, PoseStack matrix, VertexConsumer buffer, boolean cull, RandomSource rand, long seed, int overlay, ModelData modelData, RenderType renderType, CallbackInfo ci) {
		if (!((FabricBakedModel) model).isVanillaAdapter()) {
			BlockRenderContext context = fabric_contexts.get();
			context.render(blockView, model, state, pos, renderType, modelData, matrix, buffer, cull, rand, seed, overlay);
			ci.cancel();
		}
	}

	@Inject(at = @At("RETURN"), method = "<init>*")
	private void onInit(CallbackInfo ci) {
		VanillaAoHelper.initialize((ModelBlockRenderer) (Object) this);
	}
}
