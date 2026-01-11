# Compressed Pollution

### A flexible API for global pollution mechanics in mod packs

## Goals:

- Flexibility
  - Handle only the basics, leaving how it is used to the developers
  - Allow for extensibility at every opportunity, avoid black-boxing wherever possible
- Efficiency
  - Keep overhead low without extreme compromises
- Simplicity
  - Easy to understand utilities and helper functions to smooth the process
  - Most values can be defined through datapack files

## What is provided:

- Basic functionality
  - All vanilla causes of item and fluid destruction, and many modded ones as well, are handled in-house, avoiding unnecessary boilerplate
- Built-in support for several mods
  - Mods that the api natively handles are:
    - [Applied Energistics 2](https://www.curseforge.com/minecraft/mc-mods/applied-energistics-2) by the AE2 team
      - Overflow card voiding items and fluids causes pollution to be released based on what was voided
      - Destroying cells causes pollution based on the cell's contents
    - [Packages](https://www.curseforge.com/minecraft/mc-mods/packages) by quat
      - Destroying a package causes pollution based on the total number of the root item
    - [Immersive Petroleum](https://www.curseforge.com/minecraft/mc-mods/immersive-petroleum) by the Immersive team
      - Fluids voided by flare stacks cause pollution
    - [Sophisticated Backpacks](https://www.curseforge.com/minecraft/mc-mods/sophisticated-backpacks) by P3pp3rF1y
      - Destroying a backpack causes pollution based on its contents
    - [Mekanism](https://www.curseforge.com/minecraft/mc-mods/mekanism) by the Mekanism team
      - Destroying QIO drives causes pollution based on its contents
  - "Can you natively support XYZ?"
    - Probably not, despite being a public API, this is first and foremost made for the [Compression](https://www.curseforge.com/minecraft/modpacks/compression) modpack and its dev team
    and aims to handle mods present within that pack. As much as I would like to support any and every mod possible, that is simply not going to happen,
    and as such the focus will remain on what the pack requires
  - "Why aren't you handling X way of destroying things in mod Y?"
    - Two possibilities, either it's not really possible with the info available where that happens in the mod's code, as most of this is done
    via mixin tomfoolery, or it's just not necessary for Compression. Feel free to handle it in your own code though, nothing stopping you there
- Extensible "registry resolvers"
  - Allow for custom datapack registries to be quickly created and used for defining pollution values for specific objects
  - Static methods for quick creation of the resolvers
  - Two [`TaggedPollutionRegistryResolvers`](../src/main/java/io/github/real_septicake/compressed_pollution/api/TaggedPollutionRegistryResolver.java) for items and fluids come pre-packaged,
  being `BuiltInResolvers#getItemResolver` and `BuiltInResolvers#getFluidResolver` respectively
- An event for when pollution is caused
  - Provides the ability to intercept and modify pollution before it is applied to the world
    - As a generic event it can be filtered to specific source types
    - Provides a nullable `sourcePos` to represent where the pollution was caused, if applicable
    - Provides the object causing the pollution to allow for greater control

## What is _not_ provided:

- Plug-&-Play solutions
  - This API is a tool, not an answer. The burden of work to make it do anything falls squarely on you