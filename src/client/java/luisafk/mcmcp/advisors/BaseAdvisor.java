package luisafk.mcmcp.advisors;

import java.util.List;

import io.modelcontextprotocol.spec.McpSchema.Content;

public abstract class BaseAdvisor {

    public abstract void init();

    public abstract List<Content> get();
}
