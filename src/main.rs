use std::{
    io::{BufReader, Read},
    path::Path,
};

use clap::{Parser, ValueEnum};
use cosmic_text::{
    self, fontdb::Database, Attrs, Buffer, Color, FontSystem, Metrics, Shaping, SwashCache,
};
use flate2::{bufread::ZlibEncoder, Compression};
use serde_json::json;
use text2bp_macros::create_tile_enum;

create_tile_enum! {
    "acid-refined-concrete",
    "black-refined-concrete",
    "blue-refined-concrete",
    "brown-refined-concrete",
    "concrete",
    "cyan-refined-concrete",
    "deepwater",
    "deepwater-green",
    "dirt-1",
    "dirt-2",
    "dirt-3",
    "dirt-4",
    "dirt-5",
    "dirt-6",
    "dirt-7",
    "dry-dirt",
    "grass-1",
    "grass-2",
    "grass-3",
    "grass-4",
    "green-refined-concrete",
    "hazard-concrete-left",
    "hazard-concrete-right",
    "lab-dark-1",
    "lab-dark-2",
    "lab-white",
    "landfill",
    "nuclear-ground",
    "orange-refined-concrete",
    "out-of-map",
    "pink-refined-concrete",
    "purple-refined-concrete",
    "red-desert-0",
    "red-desert-1",
    "red-desert-2",
    "red-desert-3",
    "red-refined-concrete",
    "refined-concrete",
    "refined-hazard-concrete-left",
    "refined-hazard-concrete-right",
    "sand-1",
    "sand-2",
    "sand-3",
    "stone-path",
    "tile-unknown",
    "tutorial-grid",
    "water",
    "water-green",
    "water-mud",
    "water-shallow",
    "water-wube",
    "yellow-refined-concrete"
}

/// Convert any text into a blueprint string using GNU Unifont, a free pixel font.
#[derive(Debug, Parser)]
#[command(version, about, long_about = None)]
struct Cli {
    /// Name of the tile you want to write with.
    #[clap(short, long, value_enum, default_value_t = Tile::StonePath)]
    tile_name: Tile,
    /// How many pixels of space between each line break.
    #[clap(short, long, default_value_t = 1)]
    line_break: u8,
    /// Width of each tab by number of spaces.
    #[clap(long, default_value_t = 1)]
    tab_width: u8,
    text: String,
}

fn main() {
    let args = Cli::parse();
    // dbg!(&args);

    let mut fontdb = Database::new();
    fontdb
        .load_font_file(Path::new("./resources/unifont-15.1.05.otf"))
        .unwrap();
    fontdb
        .load_font_file(Path::new("./resources/unifont_jp-15.1.05.otf"))
        .unwrap();
    let mut font_system = FontSystem::new_with_locale_and_db("en_US".into(), fontdb);
    let mut swash_cache = SwashCache::new();

    const FONT_SIZE: f32 = 16.0;
    let line_height: f32 = FONT_SIZE + args.line_break as f32;
    let metrics = Metrics::new(FONT_SIZE, line_height);

    let mut buffer = Buffer::new(&mut font_system, metrics);

    let mut buffer = buffer.borrow_with(&mut font_system);

    let width = f32::MAX;
    buffer.set_size(Some(width), None);

    let attrs = Attrs::new();
    // Parameterized
    attrs.family(cosmic_text::Family::Name("Unifont"));

    let text = args.text.clone();
    buffer.set_text(&text, attrs, Shaping::Advanced);

    // Honestly have no idea what this shit does
    buffer.shape_until_scroll(true);

    // Default text color (0xFF, 0xFF, 0xFF is white), not like it matters here
    const TEXT_COLOR: Color = Color::rgb(0xFF, 0xFF, 0xFF);

    let height = line_height * buffer.layout_runs().count() as f32;

    let mut tiles_json = json!({
        "blueprint": {
            "item": "blueprint",
            "icons": [
                // Maybe not the best default icons, but whatever.
                {"index": 1, "signal": {"name": "signal-A", "type": "virtual"}},
                {"index": 2, "signal": {"name": "signal-B", "type": "virtual"}},
                {"index": 3, "signal": {"name": "signal-C", "type": "virtual"}},
                {"index": 4, "signal": {"name": "signal-D", "type": "virtual"}},
            ],
            "label": args.text,
            "tiles": [],
            "version": 281479274299391_u64 /*WTF is this shit?*/,
        }
    });

    let mut tiles = Vec::new();
    let tilename = args.tile_name.to_string();

    // Draw to the canvas
    buffer.draw(&mut swash_cache, TEXT_COLOR, |x, y, w, h, color| {
        let a = color.a();
        if a == 0 || x < 0 || x >= width as i32 || y < 0 || y >= height as i32 || w != 1 || h != 1 {
            // Ignore alphas of 0, or invalid x, y coordinates, or unimplemented sizes
            return;
        }

        tiles.push(json!({
            "name": tilename.to_owned(),
            "position": {"x": x, "y": y},
        }));
    });

    tiles_json["blueprint"]["tiles"] = tiles.into();
    let tiles_json_string = tiles_json.to_string();

    let uncompressed = tiles_json_string.clone();
    let uncompressed = uncompressed.as_bytes();
    let b = BufReader::new(uncompressed);
    let mut z = ZlibEncoder::new(b, Compression::best());
    let mut compressed = Vec::new();
    z.read_to_end(&mut compressed).unwrap();

    use base64::prelude::*;

    let mut base64_encoded = BASE64_STANDARD.encode(compressed);
    // // Apparently the byte 48, is the version. It must go at the beginning.
    base64_encoded.insert(0, char::from_u32(48).unwrap());
    // dbg!(&base64_encoded);
    println!("{}", base64_encoded);
}
