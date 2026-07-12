# DLib Config API

---

## `registerOption` — Register a config option

Called during mod/client initialization (before `ConfigManager.load()`).

### Signature

```java
ConfigManager.registerOption(
    modId,          // String  — Internal mod identifier (used for file names)
    modDisplayName, // String  — Human-readable name shown in the GUI sidebar
    category,       // String  — Group/category name in config UI
    key,            // String  — Option label within the category
    uniqueKey,      // String  — Globally unique key (used with getOption)
    type,           // String  — One of: "toggle", "cycle", "text", "number", "item_select", "item_select_multi"
    defaultValue,   // String  — Default value if no saved config exists
    choices,        // List<String> — Choices for "cycle" / item IDs for "item_select" types (nullable)
    tooltip         // String  — Optional hover tooltip text (nullable)
);

// Overload without tooltip:
ConfigManager.registerOption(modId, modDisplayName, category, key, uniqueKey, type, defaultValue, choices);
```

### Parameter reference

| Parameter | Type | Description |
|---|---|---|
| `modId` | `String` | Internal mod ID, e.g. `"mymod"`. Used for the JSON filename (`config/dlib/mymod.json`). |
| `modDisplayName` | `String` | Display name in the GUI sidebar, e.g. `"My Mod"`. |
| `category` | `String` | Collapsible category heading, e.g. `"Items"`. |
| `key` | `String` | Label for this option, e.g. `"Favorite Item"`. |
| `uniqueKey` | `String` | Globally-unique key for runtime lookup via `getOption()`. Convention: `"modid+category+key"` (lowercase, `+` separators). |
| `type` | `String` | `"toggle"`, `"cycle"`, `"text"`, `"number"`, `"item_select"`, or `"item_select_multi"`. |
| `defaultValue` | `String` | Fallback value. For `item_select` types: comma-separated item IDs. |
| `choices` | `List<String>` | Required for `"cycle"`. Optional for `"item_select"` — see below. Pass `null` for others. |
| `tooltip` | `String` | Hover tooltip (blue `?` icon). Pass `null` or omit for no tooltip. |

---

## Option types

### `"toggle"` — ON / OFF

```java
ConfigManager.registerOption(
    "mymod", "My Mod", "General", "Enabled",
    "mymod+general+enabled", "toggle", "true", null,
    "Toggles the entire module on or off."
);
```

Runtime:
```java
ConfigManager.ConfigOption opt = ConfigManager.getOption("mymod+general+enabled");
boolean enabled = Boolean.parseBoolean(opt.value);
```

---

### `"cycle"` — Dropdown selection

```java
ConfigManager.registerOption(
    "mymod", "My Mod", "Visuals", "Particle Effect",
    "mymod+visuals+particle", "cycle", "Flame",
    List.of("Flame", "Smoke", "Heart", "None"),
    "Choose the particle effect to display."
);
```

Runtime:
```java
String effect = ConfigManager.getOption("mymod+visuals+particle").value;  // e.g. "Flame"
```

---

### `"text"` — Free-form text

```java
ConfigManager.registerOption(
    "mymod", "My Mod", "Visuals", "Status Tag",
    "mymod+visuals+tag", "text", "Hovering Elite", null
);
```

Runtime:
```java
String tag = ConfigManager.getOption("mymod+visuals+tag").value;  // e.g. "Hovering Elite"
```

---

### `"number"` — Numeric input

```java
ConfigManager.registerOption(
    "mymod", "My Mod", "Movement", "Speed Multiplier",
    "mymod+movement+speed", "number", "2.5", null,
    "Horizontal speed multiplier (supports decimals)."
);
```

Runtime:
```java
double speed = Double.parseDouble(ConfigManager.getOption("mymod+movement+speed").value);
```

---

### `"item_select"` — Single item picker

Opens a grid popup to pick **one** Minecraft item/block.

```java
// ── With a restricted list ─────────────────────────────────
ConfigManager.registerOption(
    "mymod", "My Mod", "Items", "Favorite Item",
    "mymod+items+favorite", "item_select",
    "minecraft:diamond",                                          // default
    List.of("minecraft:diamond", "minecraft:iron_ingot", "minecraft:gold_ingot"),
    "Pick your favorite item from the list."
);

// ── With ALL Minecraft items/blocks (pass null or empty list) ─
ConfigManager.registerOption(
    "mymod", "My Mod", "Items", "Any Block",
    "mymod+items+anyblock", "item_select",
    "minecraft:stone",                                            // default
    null,                                                         // null = all items
    "Choose any block or item from the entire game."
);
```

Runtime — `option.value` is a **single item ID** string:
```java
ConfigManager.ConfigOption opt = ConfigManager.getOption("mymod+items+favorite");
String itemId = opt.value;  // e.g. "minecraft:diamond"

// Resolve to an actual Item:
Identifier id = Identifier.of(itemId.split(":")[0], itemId.split(":")[1]);
Item item = BuiltInRegistries.ITEM.get(id);
```

---

### `"item_select_multi"` — Multi item picker

Opens a grid popup to pick **multiple** Minecraft items/blocks (click to toggle).

```java
// ── With a restricted list ─────────────────────────────────
ConfigManager.registerOption(
    "mymod", "My Mod", "Items", "Allowed Blocks",
    "mymod+items+blocks", "item_select_multi",
    "minecraft:dirt,minecraft:stone",                              // defaults (comma-separated)
    List.of("minecraft:dirt", "minecraft:stone", "minecraft:grass_block", "minecraft:sand"),
    "Select which blocks are allowed."
);

// ── With ALL Minecraft items/blocks ────────────────────────
ConfigManager.registerOption(
    "mymod", "My Mod", "Items", "Block List",
    "mymod+items+blocklist", "item_select_multi",
    "",                                                            // no defaults
    null,                                                          // null = all items
    "Choose any items/blocks from the entire game."
);
```

Runtime — `option.value` is a **comma-separated** string of item IDs:
```java
ConfigManager.ConfigOption opt = ConfigManager.getOption("mymod+items+blocks");
String raw = opt.value;  // e.g. "minecraft:dirt,minecraft:stone,minecraft:sand"

// Parse into a Set or List:
Set<String> selectedItems = new HashSet<>();
if (!raw.isEmpty()) {
    for (String id : raw.split(",")) {
        selectedItems.add(id.trim());
    }
}

// Check if a specific item is selected:
boolean hasDirt = selectedItems.contains("minecraft:dirt");
```

---

## `getOption` — Read an option at runtime

```java
ConfigManager.ConfigOption opt = ConfigManager.getOption("mymod+general+enabled");

if (opt != null) {
    // opt.type    → "toggle", "cycle", "text", "number", "item_select", "item_select_multi"
    // opt.value   → Current value as String
    // opt.defaultValue → Original default
    // opt.choices → The choices list (if any)
    // opt.tooltip → Tooltip text (if any)

    // For toggle:
    boolean state = Boolean.parseBoolean(opt.value);

    // For number:
    double num = Double.parseDouble(opt.value);

    // For item_select:
    String singleItemId = opt.value;

    // For item_select_multi:
    String[] itemIds = opt.value.split(",");
}
```

---

## Complete example

```java
public class MyModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        String mod = "mymod";

        // Toggle
        ConfigManager.registerOption(mod, "My Mod", "General", "Enabled",
                "mymod+general+enabled", "toggle", "true", null, "Master on/off switch.");

        // Cycle
        ConfigManager.registerOption(mod, "My Mod", "General", "Mode",
                "mymod+general+mode", "cycle", "Normal",
                List.of("Normal", "Hardcore", "Creative"), null);

        // Number
        ConfigManager.registerOption(mod, "My Mod", "General", "Speed",
                "mymod+general+speed", "number", "1.5", null, "Movement multiplier.");

        // Text
        ConfigManager.registerOption(mod, "My Mod", "General", "Greeting",
                "mymod+general+greeting", "text", "Hello!", null);

        // Single item picker (specific list)
        ConfigManager.registerOption(mod, "My Mod", "Items", "Favorite Block",
                "mymod+items+favblock", "item_select", "minecraft:diamond",
                List.of("minecraft:diamond", "minecraft:iron_ingot", "minecraft:gold_ingot"),
                "Pick one favorite block.");

        // Multi item picker (all Minecraft items)
        ConfigManager.registerOption(mod, "My Mod", "Items", "Whitelisted Blocks",
                "mymod+items+whitelist", "item_select_multi", "minecraft:stone,minecraft:dirt",
                null,  // null = show all Minecraft items/blocks
                "Blocks that are allowed for placement.");

        // Load saved configs from disk
        ConfigManager.load();
    }
}
```

---

## Tips

- **`uniqueKey`** must be globally unique across ALL mods. Convention: `"modid+category+key"` in lowercase with `+` separators.
- **`choices`** for `item_select` types: if `null` or empty, the popup shows **all Minecraft items/blocks** automatically.
- **`defaultValue`** for `item_select_multi`: use comma-separated IDs like `"minecraft:dirt,minecraft:stone"`.
- Call `ConfigManager.load()` **after** registering all options to load saved values from disk.
- The config GUI opens from the pause screen (gear icon) or via ModMenu integration.