package com.progwml6.natura.nether.block.vine;

import java.util.Random;

import com.progwml6.natura.Natura;
import net.minecraft.block.BlockVine;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockNetherThornVines extends BlockVine
{
    public BlockNetherThornVines()
    {
        super();
        this.setCreativeTab(Natura.TAB);
        this.setHardness(1.0F);
        this.setLightLevel(0.825F);
        this.setSoundType(SoundType.PLANT);
    }

    @Override
    public void onEntityCollision(World worldIn, BlockPos pos, IBlockState state, Entity entityIn)
    {
        Random random = new Random();
        random.setSeed(2 ^ 16 + 2 ^ 8 + (4 * 3 * 271));

        if (!(entityIn instanceof EntityItem) && !(entityIn instanceof EntityGhast) && random.nextInt(30) == 0)
        {
            DamageSource source = random.nextBoolean() ? DamageSource.CACTUS : DamageSource.LAVA;
            entityIn.attackEntityFrom(source, 1);
        }
    }
}
