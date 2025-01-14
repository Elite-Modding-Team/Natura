package com.progwml6.natura.overworld.block.saplings;

import java.util.List;
import java.util.Locale;
import java.util.Random;
import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.event.terraingen.TerrainGen;

import com.progwml6.natura.Natura;
import com.progwml6.natura.overworld.NaturaOverworld;
import com.progwml6.natura.overworld.block.leaves.BlockRedwoodLeaves;
import com.progwml6.natura.overworld.block.logs.BlockRedwoodLog;
import com.progwml6.natura.world.worldgen.trees.BaseTreeGenerator;
import com.progwml6.natura.world.worldgen.trees.overworld.RedwoodTreeGenerator;
import slimeknights.mantle.block.EnumBlock;

public class BlockRedwoodSapling extends BlockSapling
{
    public static final PropertyEnum<SaplingType> FOLIAGE = PropertyEnum.create("foliage", SaplingType.class);

    private final List<BlockPos> redwoodSaplingPositions = Lists.newArrayList();

    public BlockRedwoodSapling()
    {
        this.setCreativeTab(Natura.TAB);
        this.setDefaultState(this.blockState.getBaseState());
        this.setSoundType(SoundType.PLANT);
        this.setHardness(0.0F);
    }

    @Override
    public boolean isReplaceable(@Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos)
    {
        return false;
    }

    @Nonnull
    @Override
    public ItemStack getPickBlock(@Nonnull IBlockState state, @Nonnull RayTraceResult target, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player)
    {
        IBlockState iblockstate = world.getBlockState(pos);
        int meta = iblockstate.getBlock().getMetaFromState(iblockstate);
        return new ItemStack(Item.getItemFromBlock(this), 1, meta);
    }

    @Nonnull
    @Override
    public EnumPlantType getPlantType(@Nonnull IBlockAccess world, @Nonnull BlockPos pos)
    {
        return EnumPlantType.Plains;
    }

    @Override
    public void generateTree(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Random rand)
    {
        if (!TerrainGen.saplingGrowTree(worldIn, rand, pos)/* || pos.getY() < 5*/)
        {
            return;
        }

        BaseTreeGenerator gen = new BaseTreeGenerator();

        IBlockState bark;
        IBlockState heart;
        IBlockState root;
        IBlockState leaves;

        if (state.getValue(FOLIAGE) == SaplingType.REDWOOD)
        {
            int numSaplings = this.checkRedwoodSaplings(worldIn, pos);

            bark = NaturaOverworld.redwoodLog.getDefaultState().withProperty(BlockRedwoodLog.TYPE, BlockRedwoodLog.RedwoodType.BARK);
            heart = NaturaOverworld.redwoodLog.getDefaultState().withProperty(BlockRedwoodLog.TYPE, BlockRedwoodLog.RedwoodType.HEART);
            root = NaturaOverworld.redwoodLog.getDefaultState().withProperty(BlockRedwoodLog.TYPE, BlockRedwoodLog.RedwoodType.ROOT);
            leaves = NaturaOverworld.redwoodLeaves.getDefaultState().withProperty(BlockRedwoodLeaves.TYPE, BlockRedwoodLeaves.RedwoodType.NORMAL);

            if (numSaplings >= 40)
            {
                gen = new RedwoodTreeGenerator(bark, heart, root, leaves);
            }
        }
        else
        {
            Natura.log.warn("BlockRedwoodSapling Warning: Invalid sapling meta/foliage, " + state.getValue(FOLIAGE) + ". Please report!");
        }

        // replace saplings with air
        this.replaceBlocksWithAir(worldIn, pos);

        // try generating
        gen.generateTree(rand, worldIn, pos);

        if (worldIn.isAirBlock(pos))
        {
            this.replaceAirWithBlocks(worldIn, state);
            worldIn.setBlockState(pos, state, 4);
        }
    }

    @Override
    public int damageDropped(@Nonnull IBlockState state)
    {
        return this.getMetaFromState(state);
    }

    @Override
    public void getSubBlocks(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> list)
    {
        for (SaplingType type : SaplingType.values())
        {
            list.add(new ItemStack(this, 1, this.getMetaFromState(this.getDefaultState().withProperty(FOLIAGE, type))));
        }
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        if (meta < 0 || meta >= SaplingType.values().length)
        {
            meta = 0;
        }

        SaplingType sapling = SaplingType.values()[meta];

        return this.getDefaultState().withProperty(FOLIAGE, sapling);
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(FOLIAGE).ordinal();
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState()
    {
        // TYPE has to be included because of the BlockSapling constructor, but it's never used.
        return new BlockStateContainer(this, FOLIAGE, STAGE, TYPE);
    }

    /**
     * Check whether the given BlockPos has a Sapling of the given type
     */
    public int checkRedwoodSaplings(World worldIn, BlockPos pos)
    {
        int numSaplings = 0;

        for (int x = -3; x <= 3; x++)
        {
            for (int z = -3; z <= 3; z++)
            {
                if (this.isRedwoodComplete(worldIn, pos.add(x, 0, z), SaplingType.REDWOOD))
                {
                    numSaplings++;
                }
            }
        }

        return numSaplings;
    }

    /**
     * Check whether the given BlockPos has a Sapling of the given type
     */
    public boolean isRedwoodComplete(World worldIn, BlockPos pos, SaplingType type)
    {
        IBlockState iblockstate = worldIn.getBlockState(pos);
        return iblockstate.getBlock() == this && iblockstate.getValue(FOLIAGE) == type;
    }

    /**
     * Replaces redwood sapling's with air
     */
    public void replaceBlocksWithAir(World worldIn, BlockPos pos)
    {
        for (int x = -3; x <= 3; x++)
        {
            for (int z = -3; z <= 3; z++)
            {
                if (this.isRedwoodComplete(worldIn, pos.add(x, 0, z), SaplingType.REDWOOD))
                {
                    redwoodSaplingPositions.add(pos.add(x, 0, z));
                    worldIn.setBlockToAir(pos.add(x, 0, z));
                }
            }
        }
    }

    private void replaceAirWithBlocks(World worldIn, IBlockState state)
    {
        for (BlockPos pos : redwoodSaplingPositions)
        {
            if (worldIn.isAirBlock(pos))
            {
                worldIn.setBlockState(pos, state, 4);
            }
        }

        redwoodSaplingPositions.clear();
    }

    public enum SaplingType implements IStringSerializable, EnumBlock.IEnumMeta
    {
        REDWOOD;

        public final int meta;

        SaplingType()
        {
            this.meta = this.ordinal();
        }

        @Nonnull
        @Override
        public String getName()
        {
            return this.toString().toLowerCase(Locale.US);
        }

        @Override
        public int getMeta()
        {
            return this.meta;
        }
    }
}
