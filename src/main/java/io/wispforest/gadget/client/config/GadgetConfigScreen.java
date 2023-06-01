package io.wispforest.gadget.client.config;

import io.wispforest.gadget.Gadget;
import io.wispforest.gadget.mappings.MappingsManager;
import io.wispforest.gadget.path.FieldPathStep;
import io.wispforest.owo.config.Option;
import io.wispforest.owo.config.ui.ConfigScreen;
import io.wispforest.owo.config.ui.OptionComponentFactory;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GadgetConfigScreen extends ConfigScreen {
    @SuppressWarnings("unchecked")
    public GadgetConfigScreen(@Nullable Screen parent) {
        super(DEFAULT_MODEL_ID, Gadget.CONFIG, parent);

        extraFactories.put(opt -> opt.key().name().equals("hiddenFields"), (model, option) -> {
            var layout = new RemappingListOptionContainer(
                (Option<List<String>>)(Option<?>) option,
                FieldPathStep::remapFieldId,
                id -> MappingsManager.displayMappings().unmapFieldId(id)
            );
            return new OptionComponentFactory.Result<>(layout, layout);
        });
    }
}
