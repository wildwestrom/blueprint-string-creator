use convert_case::{Case, Casing};
use proc_macro::TokenStream;
use quote::quote;
use syn::{parse_macro_input, Ident, Lit, LitStr, NestedMeta};

#[proc_macro]
pub fn create_tile_enum(input: TokenStream) -> TokenStream {
    let nestedmeta = parse_macro_input!(input as syn::AttributeArgs);

    let string_variants: Vec<LitStr> = nestedmeta
        .into_iter()
        .map(|meta| match meta {
            NestedMeta::Lit(lit) => match lit {
                Lit::Str(s) => s,
                otherlit => panic!("Expected a str, got {:?}", otherlit),
            },
            NestedMeta::Meta(m) => panic!("Expected a lit, got {:?}", m),
        })
        .collect();
    let variants: Vec<Ident> = string_variants
        .clone()
        .into_iter()
        .map(|litstr| {
            //
            let camelcase_variant = litstr.value().to_case(Case::Pascal);
            Ident::new(&camelcase_variant, proc_macro2::Span::call_site())
        })
        .collect();

    let enum_def = quote! {
        #[derive(Debug, Clone, ValueEnum)]
        enum Tile {
            #(#variants),*
        }

        impl ToString for Tile {
            fn to_string(&self) -> String {
                match *self {
                    #(Self::#variants => #string_variants.to_owned()),*
                }
            }
        }
    };

    enum_def.into()
}
