package com.progwml6.natura.world.worldgen.trees.overworld;

import java.util.Random;
import com.progwml6.natura.common.config.Config;
import com.progwml6.natura.overworld.NaturaOverworld;
import com.progwml6.natura.world.worldgen.trees.BaseTreeGenerator;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;

public class OverworldTreeGenerator extends BaseTreeGenerator
{
    public final int minTreeHeight;

    public final int treeHeightRange;

    public final IBlockState log;

    public final IBlockState leaves;

    public final boolean seekHeight;

    public final boolean isSapling;

    public OverworldTreeGenerator(int treeHeight, int treeRange, IBlockState log, IBlockState leaves, boolean seekHeight, boolean isSapling)
    {
        this.minTreeHeight = treeHeight;
        this.treeHeightRange = treeRange;
        this.log = log;
        this.leaves = leaves;
        this.seekHeight = seekHeight;
        this.isSapling = isSapling;
    }

    public OverworldTreeGenerator(int treeHeight, int treeRange, IBlockState log, IBlockState leaves)
    {
        this(treeHeight, treeRange, log, leaves, true, false);
    }

    @Override
    public void generateTree(Random rand, World worldIn, BlockPos position)
    {
        int heightRange = rand.nextInt(this.treeHeightRange) + this.minTreeHeight;

        if (this.seekHeight)
        {
            position = this.findGround(worldIn, position);

            if (position.getY() < 0)
            {
                return;
            }
        }

        if (position.getY() >= 1 && position.getY() + heightRange + 1 <= 256)
        {
            IBlockState state = worldIn.getBlockState(position.down());
            Block soil = state.getBlock();
            boolean isSoil = soil.canSustainPlant(state, worldIn, position.down(), EnumFacing.UP, NaturaOverworld.overworldSapling);

            if (isSoil)
            {
                if (!this.checkIfCanGrow(position, heightRange, worldIn))
                {
                    return;
                }

                soil.onPlantGrow(state, worldIn, position.down(), position);
                this.placeCanopy(worldIn, rand, position, heightRange);
                this.placeTrunk(worldIn, position, heightRange);
            }
        }
    }

    protected void placeCanopy(World world, Random random, BlockPos pos, int height)
    {
        for (int y = pos.getY() - 3 + height; y <= pos.getY() + height; ++y)
        {
            int subtract = y - (pos.getY() + height);
            int subtract2 = 1 - subtract / 2;

            for (int x = pos.getX() - subtract2; x <= pos.getX() + subtract2; ++x)
            {
                int mathX = x - pos.getX();

                for (int z = pos.getZ() - subtract2; z <= pos.getZ() + subtract2; ++z)
                {
                    int mathZ = z - pos.getZ();

                    if (Math.abs(mathX) != subtract2 || Math.abs(mathZ) != subtract2 || random.nextInt(2) != 0 && subtract != 0)
                    {
                        BlockPos blockpos = new BlockPos(x, y, z);
                        IBlockState state = world.getBlockState(blockpos);

                        if (state.getBlock().isAir(state, world, blockpos) || state.getBlock().canBeReplacedByLeaves(state, world, blockpos))
                        {
                            world.setBlockState(blockpos, this.leaves, 2);
                        }
                    }
                }
            }
        }
    }

    protected void placeTrunk(World world, BlockPos pos, int height)
    {
        for (int localHeight = 0; localHeight < height; ++localHeight)
        {
            BlockPos blockpos = new BlockPos(pos.getX(), pos.getY() + localHeight, pos.getZ());
            IBlockState state = world.getBlockState(blockpos);
            Block block = state.getBlock();

            if (block.isAir(state, world, blockpos))
            {
                world.setBlockState(blockpos, this.log, 2);
            }
        }
    }

    protected BlockPos findGround(World world, BlockPos pos)
    {
        int returnHeight = 0;

        int height = pos.getY();

        if (world.getWorldType() == WorldType.FLAT && this.isSapling)
        {
            do
            {
                BlockPos position = new BlockPos(pos.getX(), height, pos.getZ());
                IBlockState state = world.getBlockState(position);
                Block block = state.getBlock();
                boolean isSoil = block.canSustainPlant(state, world, position, EnumFacing.UP, NaturaOverworld.overworldSapling);

                if (isSoil && !world.getBlockState(position.up()).isFullBlock())
                {
                    returnHeight = height + 1;
                    break;
                }

                height--;
            } while (height > Config.flatSeaLevel);

        }
        else
        {
            do
            {
                BlockPos position = new BlockPos(pos.getX(), height, pos.getZ());
                IBlockState state = world.getBlockState(position);
                Block block = state.getBlock();
                boolean isSoil = block.canSustainPlant(state, world, position, EnumFacing.UP, NaturaOverworld.overworldSapling);

                if (isSoil && !world.getBlockState(position.up()).isFullBlock())
                {
                    returnHeight = height + 1;
                    break;
                }

                height--;
            } while (height > Config.seaLevel);
        }
        return new BlockPos(pos.getX(), returnHeight, pos.getZ());
    }

    protected boolean checkIfCanGrow(BlockPos position, int heightRange, World world)
    {
        boolean canGrowTree = true;
        int range;

        for (int y = position.getY(); y <= position.getY() + 1 + heightRange; ++y)
        {
            range = 1;

            if (y == position.getY())
            {
                range = 0;
            }

            if (y >= position.getY() + 1 + heightRange - 2)
            {
                range = 2;
            }

            BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();

            for (int x = position.getX() - range; x <= position.getX() + range && canGrowTree; ++x)
            {
                for (int z = position.getZ() - range; z <= position.getZ() + range && canGrowTree; ++z)
                {
                    canGrowTree = world.isAirBlock(blockPos.setPos(x, y, z));
                }
            }
        }

        return canGrowTree;
    }
}
