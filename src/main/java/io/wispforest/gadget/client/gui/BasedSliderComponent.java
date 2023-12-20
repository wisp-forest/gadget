package io.wispforest.gadget.client.gui;

import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.SliderComponent;
import io.wispforest.owo.ui.core.OwoUIDrawContext;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.util.NinePatchTexture;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class BasedSliderComponent extends SliderComponent {
    private Function<Double, Text> tooltipFactory;

    public BasedSliderComponent(Sizing horizontalSizing) {
        super(horizontalSizing);
    }

    public BasedSliderComponent tooltipFactory(Function<Double, Text> tooltipFactory) {
        this.tooltipFactory = tooltipFactory;
        return this;
    }

    @Override
    public void renderWidget(DrawContext ctx, int mouseX, int mouseY, float delta) {
        NinePatchTexture.draw(ButtonComponent.DISABLED_TEXTURE, (OwoUIDrawContext) ctx, getX(), getY(), width, height);

        NinePatchTexture.draw(
            (hovered ? ButtonComponent.HOVERED_TEXTURE : ButtonComponent.ACTIVE_TEXTURE),
            (OwoUIDrawContext) ctx,
            this.getX() + (int)(this.value * (double)(this.width - 8)),
            getY(),
            8,
            20
        );

        int textColor = this.active ? 16777215 : 10526880;
        this.drawScrollableText(ctx, MinecraftClient.getInstance().textRenderer, 2, textColor | MathHelper.ceil(this.alpha * 255.0F) << 24);
    }

    @Override
    public boolean onMouseScroll(double mouseX, double mouseY, double amount) {
        if (!this.active) return super.onMouseScroll(mouseX, mouseY, amount);

        this.value(MathHelper.clamp(this.value + .005 * amount, 0, 1));

        super.onMouseScroll(mouseX, mouseY, amount);
        return true;
    }

    @Override
    public void drawTooltip(OwoUIDrawContext ctx, int mouseX, int mouseY, float partialTicks, float delta) {
        if (!shouldDrawTooltip(mouseX, mouseY)) return;
        if (tooltipFactory == null) return;

        double tooltipValue = MathHelper.clamp((mouseX - (double)(this.getX() + 4)) / (double)(this.width - 8), 0, 1);

        List<TooltipComponent> tooltip = new ArrayList<>();
        tooltip.add(TooltipComponent.of(tooltipFactory.apply(tooltipValue).asOrderedText()));
        ctx.drawTooltip(MinecraftClient.getInstance().textRenderer, mouseX, mouseY, tooltip);
    }
}
