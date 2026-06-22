# 1. Register Options (Initialization)

Register all config options during mod/client startup.

### Example

```java
String modId = "FlyMod";

ConfigManager.registerOption(modId, "Movement Essentials", "Flight State", "toggle", "false", null);

ConfigManager.registerOption(modId, "Movement Essentials", "Horizontal Speed Modifier", "number", "2.5", null);

ConfigManager.registerOption(
    modId,
    "Visuals Mapping",
    "Custom Particle Effect",
    "cycle",
    "Flame",
    java.util.List.of("Flame", "Smoke", "Heart", "None")
);

ConfigManager.registerOption(modId, "Visuals Mapping", "Custom Status Tag", "text", "Hovering Elite", null);

ConfigManager.load();
```

---

## What the parameters mean

```
registerOption(modId, category, name, type, defaultValue, values)
```

* **modId** → Your mod identifier (namespace)
* **category** → Group name in config UI
* **name** → Option label
* **type** → Option type:

  * `"toggle"` → true/false
  * `"number"` → numeric value
  * `"cycle"` → selectable list
  * `"text"` → string input
* **defaultValue** → Starting value if none exists
* **values** → Only used for `"cycle"` options (list of choices)

---

# 2. Use Options (Runtime)

Read values during gameplay (ticks, rendering, events).

### Example

```java
var state = ConfigManager.getOption("FlyMod", "Movement Essentials", "Flight State");

if (state != null && Boolean.parseBoolean(state.value)) {

    var speed = ConfigManager.getOption("FlyMod", "Movement Essentials", "Horizontal Speed Modifier");

    double multiplier = (speed != null)
        ? Double.parseDouble(speed.value)
        : 1.0;

    player.setDeltaMovement(
        player.getDeltaMovement().x * multiplier,
        player.getDeltaMovement().y,
        player.getDeltaMovement().z * multiplier
    );
}
```