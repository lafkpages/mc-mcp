package luisafk.mcmcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

public class Client implements ClientModInitializer {
	public static final MinecraftClient MC = MinecraftClient.getInstance();
	public static final FabricLoader FABRIC_LOADER = FabricLoader.getInstance();

	public static final String MOD_ID = "mc-mcp";
	public static final String MOD_VERSION = FABRIC_LOADER.getModContainer(MOD_ID).get().getMetadata().getVersion()
			.getFriendlyString();

	public static final Boolean IS_BARITONE_INSTALLED = FABRIC_LOADER.isModLoaded("baritone");

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private McpServer mcpServer;

	@Override
	public void onInitializeClient() {
		// Start the MCP server when the mod initializes
		mcpServer = new McpServer();
		mcpServer.start();

		// Register shutdown hook
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			if (mcpServer != null) {
				mcpServer.stop();
				mcpServer = null;
			}
		}));
	}
}