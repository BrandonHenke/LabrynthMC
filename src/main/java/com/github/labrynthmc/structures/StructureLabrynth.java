package com.github.labrynthmc.structures;

import com.github.labrynthmc.Grid;
import com.github.labrynthmc.Labrynth;
import com.mojang.datafixers.Dynamic;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.template.TemplateManager;
import org.apache.logging.log4j.Level;

import java.util.Random;
import java.util.function.Function;

public class StructureLabrynth extends Structure<NoFeatureConfig>
{

	public StructureLabrynth(Function<Dynamic<?>, ? extends NoFeatureConfig> configFactoryIn) {
		super(configFactoryIn);
	}

	//*
	@Override
	protected ChunkPos getStartPositionForPosition(ChunkGenerator<?> chunkGenerator, Random random, int x, int z, int spacingOffsetsX, int spacingOffsetsZ)
	{
		Grid.Coords c = Labrynth.labrynth.getCenter();

		if (c != null) return new ChunkPos(x, z);
		else return null;
		/*
		int maxDistance = 1;
		int minDistance = 0;

		int xTemp = x + maxDistance * spacingOffsetsX;
		int ztemp = z + maxDistance * spacingOffsetsZ;
		int xTemp2 = xTemp < 0 ? xTemp - maxDistance + 1 : xTemp;
		int zTemp2 = ztemp < 0 ? ztemp - maxDistance + 1 : ztemp;
		int validChunkX = xTemp2 / maxDistance;
		int validChunkZ = zTemp2 / maxDistance;

		((SharedSeedRandom) random).setLargeFeatureSeedWithSalt(chunkGenerator.getSeed(), validChunkX, validChunkZ, this.getSeedModifier());
		validChunkX = validChunkX * maxDistance;
		validChunkZ = validChunkZ * maxDistance;
		validChunkX = validChunkX + random.nextInt(maxDistance - minDistance);
		validChunkZ = validChunkZ + random.nextInt(maxDistance - minDistance);
		return new ChunkPos(validChunkX, validChunkZ);
		*/
	}

	//*/

	@Override
	public String getStructureName()
	{
		return Labrynth.MODID+":labrynth";
	}

	@Override
	public IStartFactory getStartFactory()
	{
		return StructureLabrynth.Start::new;
	}

	@Override
	public int getSize()
	{
		return 0;
	}

	protected int getSeedModifier()
	{
		return 12345678;
	}

	@Override
	public boolean func_225558_a_(BiomeManager p_225558_1_, ChunkGenerator<?> chunkGen, Random rand, int chunkPosX, int chunkPosZ, Biome biome)
	{
		ChunkPos chunkpos = this.getStartPositionForPosition(chunkGen, rand, chunkPosX, chunkPosZ, 0, 0);

		//Checks to see if current chunk is valid to spawn in.
		if (chunkpos != null)
		{
			//Checks if the biome can spawn this structure.
			if (chunkGen.hasStructure(biome, this))
			{
				return true;
			}
		}

		return false;
	}

	public static class Start extends StructureStart
	{
		public Start(Structure<?> structureIn, int chunkX, int chunkZ, MutableBoundingBox mutableBoundingBox, int referenceIn, long seedIn)
		{
			super(structureIn, chunkX, chunkZ, mutableBoundingBox, referenceIn, seedIn);
		}
		@Override
		public void init(ChunkGenerator<?> generator, TemplateManager templateManagerIn, int chunkX, int chunkZ, Biome biomeIn)
		{

			for (Grid.Coords pos : Labrynth.labrynth.getKeys())
			{
				ResourceLocation cellType;
				byte sides[];
				//byte openSides[] = Labrynth.labrynth.getCell(pos).getOpenSides();

				char type = Labrynth.labrynth.getCell(pos).getType();

				switch (type){
					case 'H':
						cellType = StructureLabrynthPieces.HALL_WAY;
						sides = new byte[]{1,0,1,0};
						break;
					case 'L':
						cellType = StructureLabrynthPieces.ELL;
						sides = new byte[]{1,1,0,0};
						break;
					case 'T':
						cellType = StructureLabrynthPieces.TEE;
						sides = new byte[]{1,1,1,0};
						break;
					case 'D':
						cellType = StructureLabrynthPieces.DEAD_END;
						sides = new byte[]{1,0,0,0};
						break;
					default:
						cellType = StructureLabrynthPieces.FOUR_WAY;
						sides = new byte[]{1,1,1,1};
				}

				byte[] os = Labrynth.labrynth.getCell(pos).getOpenSides();
				int o = 8 * os[0] + 4 * os[1] + 2 * os[2] + 1 * os[3];
				int r;
				outer: for (r = 0; r < 4; r++) {
					switch (o) {
						case 8: // D
							break outer;
						case 12: // L
							break outer;
						case 10: // H
							break outer;
						case 14: // T
							break outer;
						case 15: // 4
							break outer;
					}
					o = (o << 1)&15 + (o >> 3);
				}


				System.out.println(r);
				Rotation rotation = Rotation.values()[r%4];
				//Turns the chunk coordinates into actual coordinates we can use. (Gets center of that chunk)
				int x = (chunkX << 4);
				int z = (chunkZ << 4);

				//Finds the y value of the terrain at location.
				//int surfaceY = generator.func_222531_c(x, z, Heightmap.Type.WORLD_SURFACE_WG);
				BlockPos blockpos = new BlockPos(x, 70, z);

				//Now adds the structure pieces to this.components with all details such as where each part goes
				//so that the structure can be added to the world by worldgen.
				StructureLabrynthPieces.start(templateManagerIn, cellType, blockpos, rotation, this.components, this.rand);

				//Sets the bounds of the structure.
				this.recalculateStructureSize();

				//I use to debug and quickly find out if the structure is spawning or not and where it is.
				Labrynth.LOGGER.log(Level.DEBUG, "Labrynth at " + (blockpos.getX()) + " " + blockpos.getY() + " " + (blockpos.getZ()));
			}
		}
	}

}
