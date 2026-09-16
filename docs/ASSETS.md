# 素材说明 / Asset notes

## 牌面 / Card faces

来源 / Source: [hayeah/playing-cards-assets](https://github.com/hayeah/playing-cards-assets)。
完整第三方许可 / Full third-party license: [CARD_ASSETS_LICENSE.txt](CARD_ASSETS_LICENSE.txt)。

52 张牌面以宽 840px 的 PNG 保存于 app/src/main/assets/cards。导出时移除了纸张外轮廓描边，保留角标、花色与人物插图。替换时保留文件名和约 5:7 的比例。
The 52 faces are stored as 840px-wide PNGs in app/src/main/assets/cards. The outer paper outline was removed during export; indices, suits, and court illustrations were retained. Preserve filenames and the approximate 5:7 aspect ratio when replacing assets.

tools/render-faces.cjs 接受源 SVG 目录和 sharp 模块路径；仅重新制作素材时需要 Node.js 与 sharp，正常构建无需它们。
tools/render-faces.cjs accepts a source SVG directory and a sharp module path. Node.js and sharp are only needed to regenerate assets, not to build the app.

## 牌背 / Card backs

红蓝牌背为本项目使用图像生成工具制作的素材，保存在 app/src/main/res/drawable-nodpi。构图采用经典双向装饰风格；细节并非逐像素严格对称，不是印刷用矢量原稿。
The red and blue card backs were created for this project with an image-generation tool and are stored in app/src/main/res/drawable-nodpi. Their two-way ornamental composition is not pixel-perfect symmetry or print-ready vector artwork.

生成提示词 / Generation prompts: [IMAGE_PROMPTS.md](IMAGE_PROMPTS.md)。

## 牌盒与木桌 / Tuck box and tabletop

牌盒由 Compose 分层绘制，支持红蓝配色。修改 CardArtwork.kt 可调整折盖、侧面与正面；替换素材时应保持图层关系和连接位置。
The tuck box is drawn in Compose layers with red and blue variants. Edit CardArtwork.kt to adjust the flap, side, and front, preserving layer order and hinge alignment.

木桌是程序生成的纹理：artwork/wood_table.svg 为源文件，tools/render-wood.cjs 导出 wood_table.webp。运行时显示静态图片。
The tabletop is procedurally generated: artwork/wood_table.svg is the source, and tools/render-wood.cjs exports wood_table.webp. Runtime rendering uses a static image.

## 许可范围 / Licensing scope

项目自有代码及程序绘制素材适用根目录 MIT 许可；第三方牌面保留其独立许可。AI 生成牌背按原样随项目提供，不作独占权利承诺。
Project-owned code and procedurally drawn assets use the root MIT license. Third-party card faces retain their separate license. AI-generated backs are provided as-is without a claim of exclusive rights.

Gradle Wrapper 的许可文本见 [GRADLE_LICENSE.txt](GRADLE_LICENSE.txt)。AndroidX 与 Kotlin 依赖通过 Gradle 解析，不将本机依赖缓存提交到仓库。
See [GRADLE_LICENSE.txt](GRADLE_LICENSE.txt) for the Gradle Wrapper license. AndroidX and Kotlin dependencies are resolved through Gradle; local dependency caches are not committed.
