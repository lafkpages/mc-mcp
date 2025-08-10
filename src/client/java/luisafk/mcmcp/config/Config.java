package luisafk.mcmcp.config;

import static luisafk.mcmcp.Client.MOD_ID;

import com.google.gson.GsonBuilder;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import dev.isxander.yacl3.platform.YACLPlatform;
import net.minecraft.util.Identifier;

// See:
// - https://github.com/copilot/c/5cc06f05-83ab-4a24-82ba-88962d35677f
// - https://github.com/copilot/c/2c7e83bb-3bdd-4368-9af4-101a7ab56214

public class Config {

    public static final ConfigClassHandler<Config> HANDLER = ConfigClassHandler.createBuilder(Config.class)
            .id(Identifier.of(MOD_ID, "config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(YACLPlatform.getConfigDir().resolve("mc-mcp.json5"))
                    .appendGsonBuilder(GsonBuilder::setPrettyPrinting)
                    .setJson5(true)
                    .build())
            .build();

    @SerialEntry
    public boolean enableAdvisors = true;

}
