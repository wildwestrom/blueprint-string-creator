# text2bp
Use this command line tool to turn any text string into a Factorio blueprint!
Supports newlines and tabs.

Each character is 16 pixels tall, and either 8 pixels or 16 pixels wide.

## Building from source
It should just be able to type `cargo run`.
If you're missing dependencies, you can either use the `flake.nix` file, or install them with your system package manager

## Usage
```console
$ text2bp --help

Convert any text into a blueprint string using GNU Unifont, a free pixel font

Usage: txt2bp [OPTIONS] <TEXT>

Arguments:
  <TEXT>

Options:
  -t, --tile-name <TILE_NAME>    Name of the tile you want to write with [default: stone-path] [possible values: acid-refined-concrete, black-refined-concrete, blue-refined-concrete, brown-refined-concrete, concrete, cyan-refined-concrete, deepwater, deepwater-green, dirt1, dirt2, dirt3, dirt4, dirt5, dirt6, dirt7, dry-dirt, grass1, grass2, grass3, grass4, green-refined-concrete, hazard-concrete-left, hazard-concrete-right, lab-dark1, lab-dark2, lab-white, landfill, nuclear-ground, orange-refined-concrete, out-of-map, pink-refined-concrete, purple-refined-concrete, red-desert0, red-desert1, red-desert2, red-desert3, red-refined-concrete, refined-concrete, refined-hazard-concrete-left, refined-hazard-concrete-right, sand1, sand2, sand3, stone-path, tile-unknown, tutorial-grid, water, water-green, water-mud, water-shallow, water-wube, yellow-refined-concrete]
  -l, --line-break <LINE_BREAK>  How many pixels of space between each line break [default: 1]
      --tab-width <TAB_WIDTH>    Width of each tab by number of spaces [default: 1]
  -h, --help                     Print help
  -V, --version                  Print version
```

You can then copy the blueprint and paste it into Factorio.

## Future plans

I'd really like to format the list of possible tile names more nicely, but I don't know if clap will let me do that.

## License
This program uses a compiled version of GNU Unifont for text rendering. GNU Unifont is licensed under the SIL Open Font License version 1.1. See <https://scripts.sil.org/OFL>

Blueprint String Creator
Copyright © 2024 Christian Westrom

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU General Public License as published by the Free Software
Foundation, either version 3 of the License, or (at your option) any later
version.

This program is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
this program. If not, see <https://www.gnu.org/licenses/>.

[](https://www.gnu.org/graphics/gplv3-or-later.png)
