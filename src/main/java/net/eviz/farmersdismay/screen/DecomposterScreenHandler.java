
package net.eviz.farmersdismay.screen;

import net.eviz.farmersdismay.recipe.DecomposterRecipes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.slot.Slot;

public class DecomposterScreenHandler extends ScreenHandler {

    private final Inventory inventory;
    private final PropertyDelegate propertyDelegate;

    public DecomposterScreenHandler(
            int syncId,
            PlayerInventory playerInventory,
            Inventory inventory,
            ScreenHandlerContext context,
            PropertyDelegate propertyDelegate
    ) {
        super(ModScreenHandlers.DECOMPOSTER, syncId);

        checkSize(inventory, 2);

        this.inventory = inventory;
        this.propertyDelegate = propertyDelegate;

        inventory.onOpen(playerInventory.player);

        addSlot(new Slot(inventory, 0, 39, 35) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return true;
            }
        });

        addSlot(new Slot(inventory, 1, 119, 35) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(
                        playerInventory,
                        col + row * 9 + 9,
                        8 + col * 18,
                        84 + row * 18
                ));
            }
        }

        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(
                    playerInventory,
                    col,
                    8 + col * 18,
                    142
            ));
        }

        addProperties(propertyDelegate);
    }

    public int getScaledProgress() {
        int progress = propertyDelegate.get(0);
        int maxProgress = propertyDelegate.get(1);

        if (maxProgress <= 0 || progress <= 0) {
            return 0;
        }

        return Math.min(49, progress * 49 / maxProgress);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= slots.size()) {
            return ItemStack.EMPTY;
        }

        Slot slot = slots.get(slotIndex);

        if (!slot.hasStack()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getStack();
        ItemStack original = stack.copy();

        if (slotIndex == 1) {
            if (!insertItem(stack, 2, 38, true)) {
                return ItemStack.EMPTY;
            }
        } else if (slotIndex >= 2) {
            if (DecomposterRecipes.find(stack) == null
                    || !insertItem(stack, 0, 1, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!insertItem(stack, 2, 38, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }

        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTakeItem(player, stack);
        return original;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        inventory.onClose(player);
    }
}
