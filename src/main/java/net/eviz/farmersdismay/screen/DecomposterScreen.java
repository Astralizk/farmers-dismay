
package net.eviz.farmersdismay.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class DecomposterScreen extends HandledScreen<DecomposterScreenHandler> {

    private static final Identifier TEXTURE =
            Identifier.of("farmersdismay", "textures/gui/decomposter_gui.png");

    private static final Identifier ARROW_TEXTURE =
            Identifier.of("farmersdismay", "textures/gui/decomposter_arrow.png");

    private static final Identifier COMPLETION_TEXTURE =
            Identifier.of("farmersdismay", "textures/gui/decomposter_completed.png");

    public DecomposterScreen(
            DecomposterScreenHandler handler,
            PlayerInventory inventory,
            Text title
    ) {
        super(handler, inventory, Text.empty());

        backgroundWidth = 176;
        backgroundHeight = 166;
    }

    @Override
    protected void drawBackground(
            DrawContext context,
            float delta,
            int mouseX,
            int mouseY
    ) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        context.drawTexture(
                TEXTURE,
                x, y,
                0.0F, 0.0F,
                176, 166,
                256, 256
        );

        int progress = handler.getScaledProgress();

        if (progress > 0) {
            context.drawTexture(
                    ARROW_TEXTURE,
                    x + 62, y + 35,
                    0.0F, 0.0F,
                    progress, 15,
                    49, 15
            );
        }

        if (handler.getSlot(1).hasStack()) {
            context.drawTexture(
                    COMPLETION_TEXTURE,
                    x + 123, y + 25,
                    0.0F, 0.0F,
                    8, 8,
                    8, 8
            );
        }
    }

    @Override
    protected void drawForeground(
            DrawContext context,
            int mouseX,
            int mouseY
    ) {
    }

    @Override
    public void render(
            DrawContext context,
            int mouseX,
            int mouseY,
            float delta
    ) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
