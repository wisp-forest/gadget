package io.wispforest.gadget.testmod.client;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class FunnyItem extends Item {
    public FunnyItem() {
        super(new Item.Settings());
    }

    @Override
    public Text getName(ItemStack stack) {
        if (Screen.hasShiftDown()) {
            stack.getOrCreateNbt().putString("owl", "yay");
        }

        return super.getName(stack);
    }
}
