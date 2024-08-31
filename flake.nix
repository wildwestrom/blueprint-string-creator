{
  description = "Rust example flake for Zero to Nix";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs";
    rust-overlay.url = "github:oxalica/rust-overlay";
    devenv.url = "github:cachix/devenv";
    systems.url = "github:nix-systems/default";
  };

  outputs =
    {
      self,
      nixpkgs,
      devenv,
      systems,
      rust-overlay,
      ...
    }@inputs:
    let
      forEachSystem = nixpkgs.lib.genAttrs (import systems);
    in
    {
      devShells = forEachSystem (
        system:
        let
          overlays = [ (import rust-overlay) ];
          pkgs = import nixpkgs { inherit system overlays; };
        in
        {
          default = devenv.lib.mkShell {
            inherit inputs pkgs;
            modules = [
              {
                stdenv = pkgs.stdenvAdapters.useMoldLinker pkgs.clangStdenv;
                packages = with pkgs; [
                  (rust-bin.stable.latest.default.override {
                    extensions = [
                      "rust-src"
                      "rustfmt"
                      "rust-analyzer"
                    ];
                  })
                  cargo-watch
                ];
              }
            ];
          };
        }
      );
    };
}
