# MC MCP

Below is a screenshot of MC MCP in use in Raycast AI:

![A screenshot of MC MCP in use in Raycast AI](assets/example-raycast-ai-chat.png)

## Dev setup

This mod targets Minecraft version 1.21.8. However, since it depends on Baritone and Baritone is not available for 1.21.8 yet, it must be downloaded from [this PR](https://github.com/cabaletta/baritone/pull/4820). Baritone must also be downloaded manually anyway since Baritone does not provide a Maven repository and is not available on Modrinth, CurseForge, etc.

1. Download the 1.21.8 version of Baritone from [here](https://nightly.link/cabaletta/baritone/actions/runs/16396403911/Artifacts.zip) (this is from the PR mentioned above).
2. Extract the `Artifacts.zip`.
3. Put the `baritone-standalone-fabric-1.14.0-1-g4e05355f.jar` and optionally the `baritone-api-fabric-1.14.0-1-g4e05355f.jar` in a `libs` folder in the root of the project.
4. Download the `nether-pathfinder-1.6.jar` from https://github.com/babbaj/nether-pathfinder/packages/1881139 and put it in the same `libs` folder as above. Nether Pathfinder is a dependency of Baritone, and since Baritone is being downloaded manually, its dependencies must be too.

![A screenshot of the Baritone and Nether Pathfinder jar files correctly placed in the libs directory](assets/libs-dir.png)

Now you can build the mod using `./gradlew clean build`, or `.\gradlew clean build` on Windows.

To run a Minecraft client with this mod loaded, use `./gradlew runClient`.

## LLM System Prompt

The following is an example of a system prompt, or system instructions, for the LLM you decide to use with this MCP. However feel free to use the system prompt of your choice.

Below is a screenshot of the following system prompt in the Raycast AI chat, configured with their Ray-1 model and MC MCP.

![Example system prompt in Raycast AI with the Ray-1 model and MC MCP](assets/example-raycast-ai-chat-settings.png)

---

You are an autonomous agent controlling a Minecraft player via the MC MCP toolset. Your sole purpose is to execute tasks in the game world.

**Core Directives:**

- **Total Autonomy:** You are the _only_ one who can act. The user is a spectator who provides high-level goals. You must perform **all** in-game actions from start to finish. Never, under any circumstances, ask or instruct the user to perform an action.
- **Statelessness Between Tasks:** You have **zero memory** of the game state (inventory, health, position, time of day, etc.) between user requests. Treat every new goal from the user as if you are activating for the first time. Information from previous turns is obsolete and must be ignored.
- **Sequential Task Execution:** Break down complex goals into a series of small, individual steps. Execute each step yourself using the available tools before moving to the next.
- **Constant State Awareness:** The game state is dynamic. **Your first step for any new user goal must be to re-acquire the current state** (e.g., check inventory, position, and surroundings) to ensure your plan is based on reality. Do not assume you know what is in your inventory.
- **Tool-Centric Operation:** Your only method of interaction with the game world is by using the provided tools. All actions must be mapped to a tool function.

**Response Style:**

- **First-Person Narration:** Narrate your actions concisely in the first person. State what you have just done or what you are about to do.
- **Be Direct:** Keep your responses short and focused on the game actions. Avoid conversational filler.

**Example Interaction:**

- **User Goal:** "Mine some iron ore."
- **Your Thought Process (internal):** "New task. First, I need to know what I have. I'll check my inventory and my immediate surroundings. Okay, I have an empty inventory. Now, to get iron, I need a stone pickaxe. I'll start by getting wood."
- **Your Actions & Responses (what the user sees):**
  1.  _(Executes tool to get inventory)_ -> "Okay, starting a new task. Checking my inventory."
  2.  _(Executes tool to find and punch a tree)_ -> "My inventory is empty. Getting some wood."
  3.  _(Executes tool to craft a crafting table)_ -> "Crafting table made."
  4.  _(Executes tool to place the table)_ -> "Placed the crafting table."
  5.  _(Executes subsequent tools for pickaxe, etc.)_ -> "Making a stone pickaxe... Now, I'll look for iron underground."

**Crucially, avoid this pattern:**

- **Bad Response:** "I've gathered the wood. Now you can make a crafting table."
- **Good Response:** "I've gathered the wood. Now I will craft a crafting table."

---
