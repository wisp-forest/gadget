**Debugger's Delight**

Gadget is a collection of tools to debug and inspect certain game elements from in-game - think Chrome's developer tools, or Firefoxes' Firebug.

Gadget's inspection tools work both in single player and on a remote server - if it also has Gadget installed.

## Entity Inspector

![Entity Inspector preview](https://raw.githubusercontent.com/wisp-forest/gadget/master/media/gadget-screenshot-entity-inspector-1.jpg)

Press the `Inspect (default: I)` key  when targeting a entity or block entity to view and edit its internal Java fields. You can inspect both the client and server Java object.

## NBT Inspector

![UI Inspector preview](https://raw.githubusercontent.com/wisp-forest/gadget/master/media/gadget-screenshot-nbt-inspector-1.jpg)

Press the `Inspect (default: I)` key when hovering over an item stack with NBT to view and edit its NBT.

## Packet Dumper

![Packet Dumper preview](https://raw.githubusercontent.com/wisp-forest/gadget/master/media/gadget-screenshot-packet-dump-viewer-1.jpg)

Press the `Dump Packets (default: K)` key to toggle dumping network traffic on. You can also join with the packet dumper engaged by right-clicking the server or world and selecting `Join and start dumping packets`.

Press `Dump Packets` again to stop recording and save the dump.  
You can review it from the Gadget's menu.

## UI Inspector

![UI Inspector preview](https://raw.githubusercontent.com/wisp-forest/gadget/master/media/gadget-screenshot-ui-inspector-1.jpg)

Hold `Ctrl+Shift` and hover over UI elements to show their size, coordinates and type. 

## Server owners and operators

Gadget supports the Fabric Permissions API.  
If not present, it defaults to requiring OP status.

Gadget uses the following permissions:

`gadget.inspect` - entity inspector usage permission  
`gadget.replaceStack` - NBT viewer write permission
