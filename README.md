# Compressed Pollution

This project is an API meant for modpack developers, individual mods should not need to provide support for this mod.

### The Intent

Many pollution mods are just a single conglomerate value in the background that counts up, providing no difference
between individual types of pollution. This mod attempts to solve this issue by providing a simple[^1] and extensible
api that give the developer as much freedom as possible. However, first and foremost, this mod is built for the 
Compression modpack by Nyagi_Byte and her team, but it was built with the idea of being flexible enough to fit 
wherever it's needed. 

The docs directory contains information on this api
- [overview.md](docs/overview.md) talks about what to expect when using the api and what it provides.
- [how_to.md](./docs/how_to.md) talks about the basics of using the api, explaining how it works and how the datapacks
  are structured. This should be where you start when looking into it.
- [extending.md](./docs/extending.md) covers adding new ways for pollution to be caused.

[^1]Just using it is as simple as reading the datapack documentation, extending it would likely require knowledge of mixins