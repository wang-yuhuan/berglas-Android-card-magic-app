// Usage: node tools/render-faces.cjs <SVG directory> <sharp module directory>
// Only for rebuilding bundled assets; the Android app has no Node dependency.
const fs = require('node:fs');
const path = require('node:path');
const sharp = require(process.argv[3] || 'sharp');
const ranks = ['ace', '2', '3', '4', '5', '6', '7', '8', '9', '10', 'jack', 'queen', 'king'];
(async () => {
  for (const rank of ranks) for (const suit of ['spades', 'hearts', 'clubs', 'diamonds']) {
    const name = `${rank}_of_${suit}`;
    const svg = fs.readFileSync(path.join(process.argv[2], `${name}.svg`), 'utf8');
    const frame = 'fill:#FFFFFF;stroke-width:0.5;';
    if (!svg.includes(frame)) throw new Error(`Missing expected card outline: ${name}`);
    // Edit the native vector outline only; preserve every rank, pip and court illustration.
    const borderless = svg.replace(frame, 'fill:#FFFFFF;stroke:none;');
    await sharp(Buffer.from(borderless), { density: 360 })
      .resize({ width: 840 }).flatten({ background: '#ffffff' }).png()
      .toFile(path.join('app/src/main/assets/cards', `${name}.png`));
  }
  console.log('Rendered all 52 card faces at 840px width.');
})();
