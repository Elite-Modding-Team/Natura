package com.progwml6.natura.tools.item.tools;

import net.minecraft.item.ItemPickaxe;

import com.progwml6.natura.Natura;

public class ItemNaturaPickaxe extends ItemPickaxe
{
    public ItemNaturaPickaxe(ToolMaterial toolMaterialIn)
    {
        super(toolMaterialIn);

        this.setCreativeTab(Natura.TAB);
    }
}
