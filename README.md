# rom2j

<p align="center">
  <img src="docs/assets/readme/rom2nxt-emblem.png" alt="rom2nxt elemental cup emblem" width="360" />
</p>

<p align="center">
  <strong>A work-in-progress Java recovery of the ROM2 / Rage of Mages II runtime.</strong><br />
  Native behavior, original data formats, game UI, networking, and server flow are being rebuilt as a readable Java codebase without sanding away the old engine's shape.
</p>

<p align="center">
  <a href="LICENSE">AGPL-3.0-or-later</a> | Java 25 | LWJGL/OpenGL client | Dedicated server
</p>

## What this is

`rom2j` is a native-parity porting project for ROM2. It is not a loose remake and not a clean-room redesign. The goal is to recover the old Windows/MFC game engine as Java code that can still speak the same file formats, run the same gameplay data, and expose the same odd but important runtime behavior.

That makes the repository part game runtime, part reverse-engineering notebook, and part preservation tool. Model classes keep native offsets and addresses in comments, tracker docs record parity gaps, and launcher/server code is split only where the modern host platform needs a cleaner boundary.

## Current shape

| Area | Status                                                                                                                                             |
| --- | --- |
| Client runtime | LWJGL/GLFW launcher delegates into the recovered `CMainApp` shell.                                                                                 |
| Rendering and UI | Original-style 2D art, panels, portraits, menus, town/shop screens, selection UI, and in-map presentation are actively being ported and corrected. |
| Data formats | MFC archives, text tables, `.res`, `.reg`, `.alm`, palette, image, sound, save, and scenario structures are modeled in Java.                       |
| Multiplayer/server | Raw TCP/IP and dedicated-server paths exist, including a no-GL Swing operator surface.                                                  |
| HAT helper | A simple HAT-compatible HTTP server supports local server-list and discovery testing on port 6666. |
| Parity state | Work in progress. Some flows are playable or inspectable; other paths are WIP until native behavior is fully matched.                              |

## Why it is interesting

The project keeps the engine's original constraints visible. Data structures are not flattened into generic DTOs, game actions keep native-compatible packet layouts, and UI work follows the actual visual object/message flow. That gives future fixes a hard target: if Java diverges from native behavior, the difference can be traced, documented, and corrected instead of hidden behind a modern abstraction.

The result is a codebase that can be used to study the game, debug old content, restore dedicated hosting, and gradually make the runtime more portable while preserving the identity of the original engine.

## Entry points

| Purpose | Entry point / doc |
| --- | --- |
| Game client | `ua.millfreedom.rom2.starter.Rom2StarterLWJGL` |
| Dedicated server | `ua.millfreedom.rom2.starter.DedicatedServerStarter` |
| Simple HAT server | `ua.millfreedom.rom2.hatserver.HatHttpServer` |

Use the project root as the working directory because the runtime expects the original resource layout beside the code.

<details>
<summary>Screenshots</summary>

| Startup/menu art | In-game wilderness viewport |
| --- | --- |
| <img src="docs/assets/readme/01-main-menu.png" alt="Recovered ROM2 startup menu with stone disk and gold icons" /> | <img src="docs/assets/readme/02-wilderness-view.png" alt="Recovered in-game wilderness view with right-side character panel and bottom action bar" /> |

| Shop and inventory | Town navigation scene |
| --- | --- |
| <img src="docs/assets/readme/03-shop-inventory.png" alt="Shop and inventory screen with item grid, merchant art, and character portrait" /> | <img src="docs/assets/readme/04-town-scene.png" alt="Town scene with stone buildings, shop entrance, cart, and statue" /> |

| Character stats | Character creation |
| --- | --- |
| <img src="docs/assets/readme/05-character-stats.png" alt="Character statistics screen with attribute controls and portrait" /> | <img src="docs/assets/readme/06-character-creation.png" alt="Character creation screen with stained glass class portraits" /> |

</details>

## Build and validation

The main runtime is Maven based.
```bash
mvn package
```

## License

`rom2j` is licensed under the GNU Affero General Public License v3.0 or later. See [LICENSE](LICENSE).

Unless a file says otherwise, project-authored source and documentation are covered by that license.
Original game data, trademarks, and media compatibility material are not relicensed by this project notice.
