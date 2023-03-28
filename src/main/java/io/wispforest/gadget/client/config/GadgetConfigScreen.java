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

        // TODO: actually make a good frontend for this config option.
//        extraFactories.put(opt -> opt.key().name().equals("quiltflowerVersion"), (model, option) -> {
//            var optionComponent = model.expandTemplate(FlowLayout.class,
//                "config-option",
//                OptionComponents.packParameters(option.translationKey(), option.value().toString())
//            );
//
//            HorizontalFlowLayout container = Containers.horizontalFlow(Sizing.content(), Sizing.content());
//
//            var versions = new ArrayList<String>();
//            versions.add("LATEST");
//            versions.addAll(QuiltflowerVersions.versions());
//
//            var selector = new DropdownSelectorWidget<>(versions, Text::literal);
//
//            selector.editable(!option.detached());
//
//            container.child(new SearchAnchorComponent(
//                container,
//                option.key(),
//                option::translationKey,
//                selector::parsedValue
//            ));
//
//            container.child(selector);
//
//            return new OptionComponentFactory.Result(container, selector);
//        });

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
