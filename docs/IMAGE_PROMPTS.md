# Image generation prompts

Mode: built-in imagegen (not CLI).

## Red back

Use case: product-mockup. Asset type: production-ready flat playing-card back texture for Android app. Create a single premium classic American magician playing card back, portrait poker aspect ratio 5:7. Full rectangular canvas is the card itself edge to edge, no surrounding scene, no perspective, no shadow. Warm ivory narrow outer border, rich oxblood red ink. Exquisite intricate engraved symmetrical scrollwork, guilloche, botanical acanthus flourishes, twin circular ornamental medallions related by exact 180-degree rotational symmetry, fine crosshatching, tiny stars, balanced elaborate double-line frames. Truly elegant traditional letterpress playing card quality, sharp precise lines readable at phone size, subtle linen paper texture, flat evenly lit. Original unbranded art, no words, no lettering, no logos, no watermark. Strict two-way rotational symmetry.

## Blue back

Use case: precise-object-edit. This is a production playing card back texture. Change ONLY the red/oxblood printing ink of this card back to rich classic navy/cobalt blue printing ink. Preserve identical framing, warm ivory paper, all engraved scrollwork, two medallions, fine lines, dimensions and proportions. Flat full card edge-to-edge canvas, no shadows or surrounding scene, no added text or watermarks.

Input reference: app/src/main/res/drawable-nodpi/back_red.png


## Walnut table — 2.2 attempt and final asset

Built-in imagegen attempt: failed with usage_limit_reached; no generated image was returned. CLI/API fallback was not used.

Prompt: Generate a photorealistic material texture asset for an offline Android playing-card table. Portrait 2:3 composition, straight overhead orthographic photograph of a single continuous fine dark walnut wooden tabletop filling the entire frame edge to edge. Warm restrained medium-dark chocolate brown, beautiful subtle lengthwise wood grain and tiny pores, matte oiled finish, softly diffuse even illumination, low contrast, elegant high-end magician's study table. Grain should look photographic and natural rather than drawn lines. No objects, no cards, no box, no text, no logos, no hands, no table edges, no horizon, no dramatic glare, no border, no vignetting. This is a reusable background material over which interactive cards will be placed.

Final project asset: app/src/main/res/drawable-nodpi/wood_table.webp, built locally from artwork/wood_table.svg with tools/render-wood.cjs. Deterministic vector wood grain and noise, rasterized once at build-artwork time; not AI-generated and not a photograph.
