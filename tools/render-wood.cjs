// Deterministic, offline vector wood material. Build once; display a single bitmap at runtime.
// Usage: node tools/render-wood.cjs <sharp module directory>
const fs = require('node:fs');
const sharp = require(process.argv[2] || 'sharp');
let seed = 62517;
const random = () => { seed = (seed * 1664525 + 1013904223) >>> 0; return seed / 4294967296; };
let fibers = '';
for (let i = 0; i < 170; i++) {
 const x = random() * 1100 - 38, a = 4 + random() * 32, phase = random() * 6.28;
 let d = '';
 for (let y = -50; y <= 1600; y += 18) {
  const bend = Math.sin(y / 360 + phase) * a + Math.sin(y / 110 + phase) * 2;
  d += `${y === -50 ? 'M' : 'L'}${(x + bend).toFixed(2)},${y} `;
 }
 fibers += `<path d="${d}" fill="none" stroke="${i % 3 ? '#201109' : '#b48452'}" stroke-opacity="${(.035 + random() * .07).toFixed(3)}" stroke-width="${(.3 + random() * 1.8).toFixed(2)}"/>`;
}
const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="1024" height="1536" viewBox="0 0 1024 1536">
<defs>
 <linearGradient id="timber"><stop stop-color="#49301f"/><stop offset=".3" stop-color="#61432c"/><stop offset=".62" stop-color="#68462d"/><stop offset="1" stop-color="#4c3221"/></linearGradient>
 <filter id="wood"><feTurbulence type="fractalNoise" baseFrequency=".047 .0026" numOctaves="3" seed="18"/><feColorMatrix type="saturate" values="0"/><feComponentTransfer><feFuncR type="linear" slope=".8" intercept=".15"/><feFuncG type="linear" slope=".8" intercept=".15"/><feFuncB type="linear" slope=".8" intercept=".15"/></feComponentTransfer><feBlend in2="SourceGraphic" mode="multiply"/></filter>
 <filter id="pores"><feTurbulence type="fractalNoise" baseFrequency=".42 .14" numOctaves="2" seed="51"/><feColorMatrix type="saturate" values="0"/></filter>
</defs>
<rect width="1024" height="1536" fill="url(#timber)"/>
<rect width="1024" height="1536" fill="url(#timber)" filter="url(#wood)" opacity=".7"/>
${fibers}
<rect width="1024" height="1536" filter="url(#pores)" opacity=".045"/>
</svg>`;
fs.mkdirSync('artwork', { recursive: true });
fs.writeFileSync('artwork/wood_table.svg', svg);
sharp(Buffer.from(svg)).webp({quality:90}).toFile('app/src/main/res/drawable-nodpi/wood_table.webp').then(() => console.log('Wood background rendered.'));
