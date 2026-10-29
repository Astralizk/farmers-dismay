
package net.eviz.farmersdismay.entity.custom;

import net.eviz.farmersdismay.entity.renderer.ModBlockEntities;
import net.eviz.farmersdismay.recipe.DecomposterRecipes;
import net.eviz.farmersdismay.recipe.DecomposterRecipes.Recipe;
import net.eviz.farmersdismay.screen.DecomposterScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DecomposterBlockEntity extends BlockEntity
        implements Inventory, NamedScreenHandlerFactory {

    private final DefaultedList<ItemStack> inventory =
            DefaultedList.ofSize(2, ItemStack.EMPTY);

    private int progress = 0;
    private int maxProgress = 100;

    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> maxProgress;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                progress = value;
            } else if (index == 1) {
                maxProgress = value;
            }
        }

        @Override
        public int size() {
            return 2;
        }
    };

    public DecomposterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DECOMPOSTER, pos, state);
    }

    public static void tick(
            World world,
            BlockPos pos,
            BlockState state,
            DecomposterBlockEntity entity
    ) {
        if (world.isClient) {
            return;
        }

        ItemStack input = entity.inventory.get(0);
        ItemStack output = entity.inventory.get(1);

        Recipe recipe = DecomposterRecipes.find(input);

        if (recipe == null) {
            entity.resetProgress();
            return;
        }

        ItemStack result = new ItemStack(
                recipe.output(),
                recipe.outputCount()
        );

        boolean outputFits = output.isEmpty()
                || (ItemStack.areItemsAndComponentsEqual(output, result)
                && output.getCount() + result.getCount()
                <= output.getMaxCount());

        if (!outputFits) {
            entity.resetProgress();
            return;
        }

        entity.maxProgress = recipe.processingTime();
        entity.progress++;

        if (entity.progress >= entity.maxProgress) {
            input.decrement(1);

            if (output.isEmpty()) {
                entity.inventory.set(1, result);
            } else {
                output.increment(result.getCount());
            }

            entity.progress = 0;
        }

        entity.markDirty();
    }

    private void resetProgress() {
        if (progress != 0) {
            progress = 0;
            markDirty();
        }
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("Decomposter");
    }

    @Override
    public ScreenHandler createMenu(
            int syncId,
            PlayerInventory playerInventory,
            PlayerEntity player
    ) {
        return new DecomposterScreenHandler(
                syncId,
                playerInventory,
                this,
                ScreenHandlerContext.create(world, pos),
                propertyDelegate
        );
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        return inventory.get(0).isEmpty()
                && inventory.get(1).isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        return inventory.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack result = Inventories.splitStack(inventory, slot, amount);
        if (!result.isEmpty()) {
            markDirty();
        }
        return result;
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack result = Inventories.removeStack(inventory, slot);
        if (!result.isEmpty()) {
            markDirty();
        }
        return result;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        inventory.set(slot, stack);

        if (stack.getCount() > getMaxCountPerStack()) {
            stack.setCount(getMaxCountPerStack());
        }

        markDirty();
    }

    @Override
    public void clear() {
        inventory.clear();
        markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return pos.isWithinDistance(player.getBlockPos(), 8);
    }

    @Override
    public void readNbt(
            NbtCompound nbt,
            RegistryWrapper.WrapperLookup registries
    ) {
        super.readNbt(nbt, registries);
        Inventories.readNbt(nbt, inventory, registries);
        progress = nbt.getInt("Progress");
        maxProgress = nbt.contains("MaxProgress")
                ? nbt.getInt("MaxProgress") : 100;
    }

    @Override
    protected void writeNbt(
            NbtCompound nbt,
            RegistryWrapper.WrapperLookup registries
    ) {
        super.writeNbt(nbt, registries);
        Inventories.writeNbt(nbt, inventory, registries);
        nbt.putInt("Progress", progress);
        nbt.putInt("MaxProgress", maxProgress);
    }
}
