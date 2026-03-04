# Extending Functionality

The content here assumes you have read the custom resolvers section of [how_to.md](how_to.md#custom-registries-and-resolvers)
and have a passing knowledge of writing mixins.

## Adding destruction methods

The general idea of how to add a new cause of pollution is as simple as a single mixin, throw it in where it will be
caused, call `fireEvent()` with the proper parameters, and enjoy the fruits of your labor. For inventories, however,
there's an extra step; The mixin must extend `PollutionContainer` and implement its method, and check its
items if they're also containers and to handle its contents, if necessary, then firing an event on the item itself.

The simplest example of how this is done is found in [`ItemDestructionMixin.java`](../src/main/java/io/github/real_septicake/compressed_pollution/mixin/ItemDestructionMixin.java),
showing off how to do the mixin, as well as how to handle an item's contents.

## Adding AE2 type handlers

Given the number of addons for AE2 providing different types of AE storage cells for different resources, creating an
all-encompassing system to handle everything, so a system has been put into place to allow the implementation of custom
handlers. This is done through the [`AE2CompatHandler`](../src/main/java/io/github/real_septicake/compressed_pollution/compat/ae2/AE2CompatHandler.java)
and its inner interface `KeyHandler`

The primary way to "register" these handlers is through the `addHandler()` function, attaching your handler to the
type provided. Notably, if (somehow) another mod provided a handler before yours, then an error is thrown as I don't
want to have to figure out how to merge them.

Examples of this are seen in the main file of this mod, [CompressedPollution.java](../src/main/java/io/github/real_septicake/compressed_pollution/CompressedPollution.java),
where is also displays the fact that you should *always* check whether AE2 is present when attempting to access 
anything in the compatibility handler. 