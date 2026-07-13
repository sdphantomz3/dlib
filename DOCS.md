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
    type,           // String  — One of: "toggle", "cycle", "text", "number", "item_select", "item_select_multi", "action"
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
| `type` | `String` | `"toggle"`, `"cycle"`, `"text"`, `"number"`, `"item_select"`, `"item_select_multi"`, or `"action"`. |
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

### `"action"` — Action button (calls a function)

Renders as a clickable button that executes a `Runnable` callback provided by your mod.
**Action buttons are not persisted** — they have no value to save/load.

Use `registerAction` instead of `registerOption`:

```java
// ── Signature ────────────────────────────────────────────
ConfigManager.registerAction(
    modId,          // String  — Internal mod identifier
    modDisplayName, // String  — Human-readable name in GUI sidebar
    category,       // String  — Group/category name
    key,            // String  — Option key within the category
    uniqueKey,      // String  — Globally unique key
    buttonLabel,    // String  — Text shown on the button
    callback,       // Runnable — Code to execute when clicked
    tooltip         // String  — Optional hover tooltip (nullable)
);

// Overload without tooltip:
ConfigManager.registerAction(modId, modDisplayName, category, key, uniqueKey, buttonLabel, callback);
```

#### Example — Calling existing functions

```java
// ── Simple action ───────────────────────────────────────
ConfigManager.registerAction(
    "mymod", "My Mod", "Actions", "Reload Data",
    "mymod+actions+reload", "Reload",
    () -> MyDataManager.reloadFromDisk(),
    "Reloads all data files from disk."
);

// ── Action calling a method on your mod instance ─────────
ConfigManager.registerAction(
    "mymod", "My Mod", "Actions", "Reset Statistics",
    "mymod+actions+resetstats", "Reset Stats",
    () -> MyMod.getInstance().resetPlayerStats()
);

// ── Action with toast feedback ──────────────────────────
ConfigManager.registerAction(
    "mymod", "My Mod", "Debug", "Dump Config",
    "mymod+debug+dump", "Dump",
    () -> {
        MyMod.dumpConfigToLog();
        // Feedback is shown automatically by the config GUI
    },
    "Writes the current config to the game log."
);
```

**Important:** The callback runs on the **render thread**. For long-running operations, launch a background thread or use `CompletableFuture` inside your callback.

---

## `registerAction` — API reference

| Parameter | Type | Description |
|---|---|---|
| `modId` | `String` | Internal mod ID, e.g. `"mymod"`. |
| `modDisplayName` | `String` | Display name in the GUI sidebar, e.g. `"My Mod"`. |
| `category` | `String` | Collapsible category heading, e.g. `"Actions"`. |
| `key` | `String` | Label for this option, e.g. `"Reload Data"`. |
| `uniqueKey` | `String` | Globally-unique key. Convention: `"modid+category+key"`. |
| `buttonLabel` | `String` | Text displayed on the button in the GUI, e.g. `"Reload"`. |
| `callback` | `Runnable` | The function or lambda to execute when clicked. |
| `tooltip` | `String` | Hover tooltip (blue `?` icon). Pass `null` or omit for no tooltip. |

---

## `registerHeading` — Section label

Adds a gold-colored **read-only** heading label that visually groups related settings.
Headings can appear inside a category or at the top level (outside any category).
They are **not persisted** to disk.

### Signature

```java
// ── Inside a category ────────────────────────────────────
ConfigManager.registerHeading(modId, modDisplayName, category, headingText);

// ── Top‑level (no category) ──────────────────────────────
ConfigManager.registerHeading(modId, modDisplayName, headingText);
```

| Parameter | Type | Description |
|---|---|---|
| `modId` | `String` | Internal mod ID, e.g. `"mymod"`. |
| `modDisplayName` | `String` | Display name in the GUI sidebar, e.g. `"My Mod"`. |
| `category` | `String` | Category to place the heading in. `null` = top‑level. |
| `headingText` | `String` | Text to display, e.g. `"Basic Settings"`. |

### Examples

```java
// ── Inside a category ────────────────────────────────────
ConfigManager.registerOption("mymod", "My Mod", "General", "Enabled",
    "mymod+general+enabled", "toggle", "true", null);

ConfigManager.registerHeading("mymod", "My Mod", "General", "─ Advanced ─");

ConfigManager.registerOption("mymod", "My Mod", "General", "Speed",
    "mymod+general+speed", "number", "1.0", null);

// ── Top‑level, before all categories ─────────────────────
ConfigManager.registerHeading("mymod", "My Mod", "Quick Settings");

ConfigManager.registerOption("mymod", "My Mod", "Enabled",
    "mymod+general+enabled", "toggle", "true", null);
```

The heading renders as **bold gold text** with a subtle highlight bar behind it.

---

## `registerSeparator` — Visual divider

Adds a **yellow horizontal line** (2px) that visually separates settings above and below.
Separators can appear inside a category or at the top level.
They are **not persisted** to disk.

### Signature

```java
// ── Inside a category ────────────────────────────────────
ConfigManager.registerSeparator(modId, modDisplayName, category);

// ── Top‑level (no category) ──────────────────────────────
ConfigManager.registerSeparator(modId, modDisplayName);
```

| Parameter | Type | Description |
|---|---|---|
| `modId` | `String` | Internal mod ID, e.g. `"mymod"`. |
| `modDisplayName` | `String` | Display name in the GUI sidebar, e.g. `"My Mod"`. |
| `category` | `String` | Category to place the separator in. `null` = top‑level. |

### Examples

```java
// ── Inside a category, between option groups ─────────────
ConfigManager.registerOption("mymod", "My Mod", "Visuals", "Particles",
    "mymod+visuals+particles", "cycle", "Flame", List.of("Flame", "Smoke", "None"));

ConfigManager.registerSeparator("mymod", "My Mod", "Visuals");

ConfigManager.registerOption("mymod", "My Mod", "Visuals", "Color",
    "mymod+visuals+color", "cycle", "Red", List.of("Red", "Blue", "Green"));

// ── Top‑level, separating sections before categories ─────
ConfigManager.registerHeading("mymod", "My Mod", "Quick Toggles");
ConfigManager.registerOption("mymod", "My Mod", "Enabled",
    "mymod+general+enabled", "toggle", "true", null);

ConfigManager.registerSeparator("mymod", "My Mod");
```

---

## Registering options without a category

All registration methods have **no‑category overloads** for top‑level items
that appear before the collapsible category sections:

```java
// registerOption without category
ConfigManager.registerOption(modId, modDisplayName, key, uniqueKey, type, defaultValue, choices);
ConfigManager.registerOption(modId, modDisplayName, key, uniqueKey, type, defaultValue, choices, tooltip);

// registerAction without category
ConfigManager.registerAction(modId, modDisplayName, key, uniqueKey, buttonLabel, callback);
ConfigManager.registerAction(modId, modDisplayName, key, uniqueKey, buttonLabel, callback, tooltip);
```

Top‑level items render in registration order **above** all categories in the config panel.

---

## `getOption` — Read an option at runtime

```java
ConfigManager.ConfigOption opt = ConfigManager.getOption("mymod+general+enabled");

if (opt != null) {
    // opt.type    → "toggle", "cycle", "text", "number", "item_select", "item_select_multi", "action"
    // opt.value   → Current value as String
    // opt.defaultValue → Original default
    // opt.choices → The choices list (if any)
    // opt.tooltip → Tooltip text (if any)
    // opt.action  → Runnable callback (only for "action" type, null otherwise)

    // For toggle:
    boolean state = Boolean.parseBoolean(opt.value);

    // For number:
    double num = Double.parseDouble(opt.value);

    // For item_select:
    String singleItemId = opt.value;

    // For item_select_multi:
    String[] itemIds = opt.value.split(",");

    // For action:
    // opt.action is the Runnable — call it directly:
    if (opt.action != null) {
        opt.action.run();
    }
}
```

---

## `setOption` — Change an option's value from code

Programmatically updates the value of a registered option at runtime.
Useful for syncing external state or implementing code‑driven config changes.

### Signatures

```java
// Set by unique key (string value — works for all option types)
ConfigManager.setOption(uniqueKey, value);

// Convenience overload for boolean toggles
ConfigManager.setOption(uniqueKey, value);
```

| Parameter | Type | Description |
|---|---|---|
| `uniqueKey` | `String` | The globally unique key used during registration. |
| `value` | `String` / `boolean` | The new value. For toggles use `"true"` / `"false"` or the boolean overload. |

### Behavior

- If the `uniqueKey` is **not found**, a warning is logged and nothing happens.
- If the option type is `"heading"`, `"separator"`, or `"action"`, a warning is logged (these types have no mutable value).
- The change is **immediate in memory** — `getOption(uniqueKey).value` reflects the new value right away.
- The change is **not automatically saved**. Call `ConfigManager.saveMod("mymod")` to persist.

### Examples

```java
// ── Toggle on/off ────────────────────────────────────────
ConfigManager.setOption("mymod+general+enabled", "false");
ConfigManager.setOption("mymod+general+enabled", false);   // boolean overload

// ── Change a cycle option ────────────────────────────────
ConfigManager.setOption("mymod+visuals+particle", "Flame");

// ── Change a number ──────────────────────────────────────
ConfigManager.setOption("mymod+movement+speed", "3.0");

// ── Change a text field ──────────────────────────────────
ConfigManager.setOption("mymod+visuals+tag", "New Tag");

// ── Change an item_select ────────────────────────────────
ConfigManager.setOption("mymod+items+favorite", "minecraft:diamond");

// ── Change item_select_multi ─────────────────────────────
ConfigManager.setOption("mymod+items+blocks", "minecraft:dirt,minecraft:stone,minecraft:sand");

// ── Persist after changes ────────────────────────────────
ConfigManager.setOption("mymod+general+enabled", false);
ConfigManager.saveMod("mymod");
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

        // Action button — calls an existing function
        ConfigManager.registerAction(mod, "My Mod", "Actions", "Reload Config",
                "mymod+actions+reload", "Reload",
                () -> MyMod.reloadConfiguration(),
                "Reloads all configuration from an external source.");

        // ── Heading (inside a category) ─────────────────
        ConfigManager.registerHeading(mod, "My Mod", "General", "─ Advanced ─");

        // ── Separator (inside a category) ───────────────
        ConfigManager.registerSeparator(mod, "My Mod", "General");

        // ── Top‑level option (no category) ──────────────
        ConfigManager.registerOption(mod, "My Mod", "Debug Mode",
                "mymod+debug", "toggle", "false", null,
                "Enable extra debug logging.");

        // ── Top‑level heading ───────────────────────────
        ConfigManager.registerHeading(mod, "My Mod", "Important");

        // ── Top‑level separator ─────────────────────────
        ConfigManager.registerSeparator(mod, "My Mod");

        // ── Programmatic changes ────────────────────────
        ConfigManager.setOption("mymod+general+enabled", true);
        ConfigManager.setOption("mymod+general+speed", "2.0");

        // Load saved configs from disk (overwrites setOption calls above)
        ConfigManager.load();
    }
}
```

---

## Tips

- **`uniqueKey`** must be globally unique across ALL mods. Convention: `"modid+category+key"` in lowercase with `+` separators.
- **`choices`** for `item_select` types: if `null` or empty, the popup shows **all Minecraft items/blocks** automatically.
- **`defaultValue`** for `item_select_multi`: use comma-separated IDs like `"minecraft:dirt,minecraft:stone"`.
- **Action buttons** (`"action"` type), **headings**, and **separators** are **not persisted** — they have no value to save or load.
- **Action callbacks** run on the **render thread**. For long operations, use a background thread or `CompletableFuture`.
- **Headings and separators** can be placed inside a category or at the top level (no category). Use them to visually organize complex config screens.
- **`setOption`** changes are immediate in memory but **not auto‑saved**. Call `ConfigManager.saveMod("mymod")` after programmatic changes to persist them.
- Call `ConfigManager.load()` **after** registering all options to load saved values from disk.
- The config GUI opens from the pause screen (gear icon) or via ModMenu integration.