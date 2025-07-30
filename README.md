# MC MCP

## Dev setup

This mod targets Minecraft version 1.21.8. However, since it depends on Baritone and Baritone is not available for 1.21.8 yet, it must be downloaded from [this PR](https://github.com/cabaletta/baritone/pull/4820)

1. Download the 1.21.8 version of Baritone from [here](https://nightly.link/cabaletta/baritone/actions/runs/16396403911/Artifacts.zip) (this is from the PR mentioned above).
2. Extract the `Artifacts.zip`.
3. Put the `baritone-standalone-fabric-1.14.0-1-g4e05355f.jar` and optionally the `baritone-api-fabric-1.14.0-1-g4e05355f.jar` in a `libs` folder in the root of the project.
4. Download the `nether-pathfinder-1.6.jar` from https://github.com/babbaj/nether-pathfinder/packages/1881139 and put it in the same `libs` folder as above. Nether Pathfinder is a dependency of Baritone, and since Baritone is being downloaded manually, its dependencies must be too.

Now you can build the mod using `./gradlew clean build`, or `.\gradlew clean build` on Windows.

To run a Minecraft client with this mod loaded, use `./gradlew runClient`.

## LLM Prompt

```
You control a Minecraft player through the MC MCP available tools.

Do not answer anything not related to Minecraft.

You are encouraged to use as many tools as possible. Feel free to use as many tools as you need, to ensure you can produce the most accurate and helpful responses.

Player state (such as inventory, selected item in hand, health, hunger, position, biome, dimension, targeted block, etc) may and likely will change between messages, so always fetch the latest state to ensure best accuracy.

Keep responses short and concise, and preferably human-like.
```
