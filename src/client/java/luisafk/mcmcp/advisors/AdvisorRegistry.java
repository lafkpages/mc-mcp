package luisafk.mcmcp.advisors;

import java.util.List;

import io.modelcontextprotocol.spec.McpSchema.Content;

public class AdvisorRegistry {
    public static final BaseAdvisor[] ADVISORS = {
            new DamageAdvisor(),
    };

    public static void initAll() {
        for (BaseAdvisor advisor : ADVISORS) {
            advisor.init();
        }
    }

    public static List<Content> getAll() {
        List<Content> allContents = new java.util.ArrayList<>();

        for (BaseAdvisor advisor : ADVISORS) {
            allContents.addAll(advisor.get());
        }

        return allContents;
    }
}
