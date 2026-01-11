# Using Compressed Pollution

## Data Packing

The main way of interacting with the api, from a value tweaking standpoint, is through datapack entries. The schema of which
is as follows:

```json5
{
  // === Only present in tagged registries (`TaggedPollutionRegistryResolver` vs `UntaggedPollutionRegistryResolver`) ===
  "tags": [
    // List of tags and the value associated with them
    {
      // Contains a "tag" key and a "value" key
      // The following would cause any vanilla log to produce 10 of this pollution on its destruction
      "tag": "minecraft:logs", // Must be a tag applicable to the backing tag registry
      "value": 10 // The amount of pollution created by the destruction of something matching the tag
    }
  ],
  // === Present in either version ===
  "values": {
    // Entries of `ResourceLocation` to `long` values
    // These represent specific values for specific objects
    // e.g.
    "minecraft:oak_log": 20 // This would mean that destroying an oak log would create 20 of this pollution
  }
}
```

The location of these entries is dependent on which registry they are intended for, and by extension what `ResourceLocation` was used
for its creation. For example, creating a pollution file for the pollution `pollution_mod:heavy_metals` for a registry with the
location `compressed_pollution:pollutions/item` (the location of Compressed Pollution's default item pollution registry) would
require this structure:

```
<Source directory>
  └ resources
    └ data
      └ pollution_mod ────────────── Pollution's namespace
        └ compressed_pollution ─────────┐
          └ pollutions ─────────────────┼ Registry's path
            └ item ─────────────────────┘
              └ heavy_metals.json ── Pollution's name
```

### How values are resolved

When resolving an object's pollution values, the resolver's cache is first checked to see if the values have already been
recently computed, and uses that if one is present. If there is a cache miss, the registry's entries are iterated over to
check what it produces. 

For each registry entry the `values` key is checked first for a match for the `ResourceLocation`
that the `toRL()` function produced from the object. If a match is found, then the match's value is used and the next entry
is processed, but if not and the resolver is a [`TaggedPollutionRegistryResolver`](../src/main/java/io/github/real_septicake/compressed_pollution/api/TaggedPollutionRegistryResolver.java)
then the `tags` key is iterated over _in order_ until either `isTag()` returns true or the end of the list is encountered.
If the former case is encountered first, then that match's value is used, else if the latter is met or the resolver is an
[`UntaggedPollutionRegistryResolver`](../src/main/java/io/github/real_septicake/compressed_pollution/api/UntaggedPollutionRegistryResolver.java)
then the entry is ignored and no value is produced for it.

Both options allow for 0 to be used as a value, giving the option to "short-circuit" the operation and cause none of that
type of pollution for that object, regardless of any later matches that would normally be found.

## Custom Registries and Resolvers

Both types of resolvers have been brought up thus far, so it begs the question, "How does one even use these?"

### Creation

Each resolver class provides a static utility method for quickly creating its respective resolver, being
called `create()` in both classes. This function takes in a `DataPackRegistryEvent$NewRegistry` event and the necessary
components to create a resolver, and returns an instance using the arguments passed in. Examples of this function being
used can be seen in the [`BuiltInResolvers`](../src/main/java/io/github/real_septicake/compressed_pollution/BuiltInResolvers.java)
class, although both are using `TaggedPollutionRegistryResolver`, but the untagged version is extremely similar with a just
a few less parameters.

### Usage

Both resolver classes provide a few methods to use them, being `resolve()` and the two `fireEvent()` methods. The former 
is mostly used internally, and goes through the process of resolving the pollution values for the provided object, [as
described above](#how-values-are-resolved). 

The latter is what will be used most commonly, as it handles both the firing of the event and the obtaining of the pollution
values for the provided object. The two methods are extremely similar, but with the notable difference being the `trans`
parameter, which is a function that gets called on the resolved pollution object before it gets sent off for the event.
This is mostly useful for objects that can appear in groups that get destroyed all at once, such as items in stacks, or
fluids being dealt with by the millibucket.

## Data Generation

Data generation for the registries created by the resolver `create()` methods are roughly the same as what would be done
for registries created by normal means, providing the registry key and the objects to be added.  The registry key for the 
resolver can be obtained from the `getRegistryKey()` method provided by the instance, and the registry entry can be
constructed by the class' `Builder` subclass. An (old but still mostly accurate) example can be found [here](
https://github.com/Real-Septicake/CompressedPollution/blob/ad999d34ff6fec539f81e2f911610f325e0beace/src/main/java/io/github/real_septicake/compressed_pollution/CompressedPollution.java#L181
). 

<sub>Do note that the link goes to a version of the file that was long before the simplification of the resolver, 
so references to an `X_REGISTRY_KEY` can be replaced with the `getRegistryKey()` call mentioned earlier</sub>

## Events

There is a single event provided by this api, that being [`PollutionEvent`](../src/main/java/io/github/real_septicake/compressed_pollution/events/PollutionEvent.java).
This is a generic fired whenever pollution is about to be applied to a `Level` that can be filtered to when a specific
class "type" causes it, e.g. a stack of items being dropped in lava fires a `PollutionEvent` for `Item.class`, a fluid
source block fires for `Fluid.class`, etc.

For the handlers, there is a fair bit of information to use, being:
- The object causing the pollution via `getObj()`
- The `Level` that the pollution is going to be applied to via `getLevel()`
- Where the pollution is being caused (if applicable) via `getSourcePos()`
  - This function will return null if there's no viable "source" position of the pollution, such as passive global increases
- And the pollution that will be applied via `getPollution()`

All of this provides the event handler with a lot of options regarding how the pollution should be handled, be it actively 
modified, canceled outright, or simply left alone.

### Firing

There are two main ways to fire off this event. The first was mentioned previously, being the `fireEvent()` methods on the
resolver classes, which creates the event based on the arguments passed to it as well as the data of the resolver itself.

The second, which `fireEvent()` is backed by, is `CompressedPollution#handlePollution()`, which is a more direct and way
of causing the event, allowing explicit assignment of each value in the event. This static function passes off
the parameters to the event batcher, which deduplicates and merges pollution objects that would cause the same event to
be fired. The events themselves are dispatched at the end of the server's tick, to be passed onto the handlers to cause
whatever changes or cancellations they end up doing.