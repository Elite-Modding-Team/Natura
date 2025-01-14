package com.progwml6.natura.nether.block.leaves;

import java.util.List;
import java.util.Locale;
import java.util.Random;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import com.progwml6.natura.Natura;
import com.progwml6.natura.common.block.base.BlockLeavesBase;
import com.progwml6.natura.nether.NaturaNether;

import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import slimeknights.mantle.block.EnumBlock;

public class BlockNetherLeaves extends BlockLeavesBase
{
    public static final PropertyEnum<BlockNetherLeaves.LeavesType> TYPE = PropertyEnum.create("type", BlockNetherLeaves.LeavesType.class);

    public BlockNetherLeaves()
    {
        this.setCreativeTab(Natura.TAB);

        Blocks.FIRE.setFireInfo(this, 0, 0);

        this.setDefaultState(this.blockState.getBaseState().withProperty(CHECK_DECAY, false).withProperty(DECAYABLE, true));
    }

    @Override
    public void updateTick(World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, Random rand)
    {
        super.updateTick(worldIn, pos, state, rand);
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list)
    {
        for (LeavesType type : LeavesType.values())
        {
            list.add(new ItemStack(this, 1, this.getMetaFromState(this.getDefaultState().withProperty(TYPE, type))));
        }
    }

    @Override
    protected int getSaplingDropChance(IBlockState state)
    {
        return 25;
    }

    // sapling item
    @Nonnull
    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        if (state.getValue(TYPE) == LeavesType.BLOODWOOD)
        {
            return Item.getItemFromBlock(NaturaNether.netherSapling2);
        }
        else
        {
            return Item.getItemFromBlock(NaturaNether.netherSapling);
        }
    }

    // sapling meta
    @Override
    public int damageDropped(IBlockState state)
    {
        return state.getValue(TYPE).getSaplingMeta();
    }

    @Nonnull
    @Override
    public ItemStack getPickBlock(@Nonnull IBlockState state, RayTraceResult target, @Nonnull World world, @Nonnull BlockPos pos, EntityPlayer player)
    {
        int meta = state.getValue(TYPE).getWailaLeavesMeta();
        return new ItemStack(Item.getItemFromBlock(this), 1, meta);
    }

    // item dropped on silktouching
    @Nonnull
    @Override
    protected ItemStack getSilkTouchDrop(@Nonnull IBlockState state)
    {
        return new ItemStack(Item.getItemFromBlock(this), 1, (state.getValue(TYPE)).ordinal() & 3);
    }

    @Nonnull
    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, TYPE, CHECK_DECAY, DECAYABLE);
    }

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        int type = meta % 4;

        if (type < 0 || type >= LeavesType.values().length)
        {
            type = 0;
        }

        LeavesType logtype = LeavesType.values()[type];

        return this.getDefaultState().withProperty(TYPE, logtype).withProperty(DECAYABLE, (meta & 4) == 0).withProperty(CHECK_DECAY, (meta & 8) > 0);
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        int meta = (state.getValue(TYPE)).ordinal() & 3; // only first 2 bits

        if (!state.getValue(DECAYABLE))
        {
            meta |= 4;
        }

        if (state.getValue(CHECK_DECAY))
        {
            meta |= 8;
        }

        return meta;
    }

    @Nonnull
    @Override
    public List<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune)
    {
        IBlockState state = world.getBlockState(pos);

        return Lists.newArrayList(this.getSilkTouchDrop(state));
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
    {
        super.getDrops(drops, world, pos, state, fortune);

        Random rand = new Random();
        rand.setSeed(2 ^ 16 + 2 ^ 8 + (4 * 3 * 271));

        if (state.getValue(TYPE) == LeavesType.BLOODWOOD)
        {
            if (fortune > 3 || rand.nextInt(40 - fortune * 10) == 0)
            {
                drops.add(new ItemStack(Items.REDSTONE));
            }
        }
    }

    public enum LeavesType implements IStringSerializable, EnumBlock.IEnumMeta
    {
        GHOSTWOOD(0, 0), BLOODWOOD(0, 1), FUSEWOOD(1, 2);

        public final int meta;

        public final int saplingMeta;

        public final int wailaLeavesMeta;

        LeavesType(int saplingMeta, int wailaLeavesMeta)
        {
            this.meta = this.ordinal();
            this.saplingMeta = saplingMeta;
            this.wailaLeavesMeta = wailaLeavesMeta;
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

        public int getSaplingMeta()
        {
            return this.saplingMeta;
        }

        public int getWailaLeavesMeta()
        {
            return this.wailaLeavesMeta;
        }
    }

}
