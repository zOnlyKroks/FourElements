package de.zonlykroks.fourelements.client;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PositionAwareModelLoadingPlugin implements ModelLoadingPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger("FourElements");

    @Override
    public void initialize(Context pluginContext) {
        LOGGER.info("Initializing Position-Aware Model Loading Plugin");

        pluginContext.modifyBlockModelAfterBake().register(ModelModifier.WRAP_LAST_PHASE, (model, context) -> {
            LOGGER.debug("Wrapping block model for state: {}", context.state());
            return new PositionAwareBlockStateModel(model);
        });
    }
}