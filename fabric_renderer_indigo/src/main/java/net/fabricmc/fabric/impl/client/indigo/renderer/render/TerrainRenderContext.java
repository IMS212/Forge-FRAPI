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

package net.fabricmc.fabric.impl.client.indigo.renderer.render;

import java.util.Set;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.AoCalculator;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;

/**
 * Implementation of {@link RenderContext} used during terrain rendering.
 * Dispatches calls from models during chunk rebuild to the appropriate consumer,
 * and holds/manages all of the state needed by them.
 */
public class TerrainRenderContext extends AbstractBlockRenderContext {
	public static final ThreadLocal<TerrainRenderContext> POOL = ThreadLocal.withInitial(TerrainRenderContext::new);

	private final ChunkRenderInfo chunkInfo = new ChunkRenderInfo();

	public TerrainRenderContext() {
		overlay = OverlayTexture.NO_OVERLAY;
		blockInfo.random = RandomSource.create();
	}

	@Override
	protected AoCalculator createAoCalc(BlockRenderInfo blockInfo) {
		return new AoCalculator(blockInfo) {
			@Override
			public int light(BlockPos pos, BlockState state) {
				return chunkInfo.cachedBrightness(pos, state);
			}

			@Override
			public float ao(BlockPos pos, BlockState state) {
				return chunkInfo.cachedAoLevel(pos, state);
			}
		};
	}

	@Override
	protected VertexConsumer getVertexConsumer(RenderType layer) {
		return chunkInfo.getInitializedBuffer(layer);
	}

	public void prepare(RenderChunkRegion blockView, SectionRenderDispatcher.RenderSection chunkRenderer, SectionRenderDispatcher.RenderSection.RebuildTask.CompileResults renderData, SectionBufferBuilderPack builders, Set<RenderType> initializedLayers) {
		blockInfo.prepareForWorld(blockView, true);
		chunkInfo.prepare(blockView, chunkRenderer, renderData, builders, initializedLayers);
	}

	public void release() {
		chunkInfo.release();
		blockInfo.release();
	}

	/** Called from chunk renderer hook. */
	public void tessellateBlock(BlockState blockState, BlockPos blockPos, final BakedModel model, RenderType renderType, ModelData modelData, PoseStack matrixStack) {
		try {
			Vec3 vec3d = blockState.getOffset(chunkInfo.blockView, blockPos);
			matrixStack.translate(vec3d.x, vec3d.y, vec3d.z);

			this.matrix = matrixStack.last().pose();
			this.normalMatrix = matrixStack.last().normal();

			blockInfo.recomputeSeed = true;

			aoCalc.clear();
			blockInfo.prepareForBlock(blockState, blockPos, renderType, modelData, model.useAmbientOcclusion());
			((FabricBakedModel) model).emitBlockQuads(blockInfo.blockView, blockInfo.blockState, blockInfo.blockPos, blockInfo.currentLayer, blockInfo.modelData, blockInfo.randomSupplier, this);
		} catch (Throwable throwable) {
			CrashReport crashReport = CrashReport.forThrowable(throwable, "Tessellating block in world - Indigo Renderer");
			CrashReportCategory crashReportSection = crashReport.addCategory("Block being tessellated");
			CrashReportCategory.populateBlockDetails(crashReportSection, chunkInfo.blockView, blockPos, blockState);
			Minecraft.getInstance().emergencySaveAndCrash(crashReport);
		}
	}
}
