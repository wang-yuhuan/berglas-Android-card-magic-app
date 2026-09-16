# 测试清单 / Testing checklist

- 开盒前后背景一致，牌盒平滑向下移出。
  Background remains consistent; sleeve moves downward smoothly.
- 单击不翻牌，单指双击翻牌，快速操作不重复触发动画。
  Single taps do not reveal; single-finger double-taps do, without duplicate animations.
- 正反面牌均能拖动并保留位置，活动牌置顶。
  Face-down and revealed cards remain draggable, retain position, and move to the front.
- 翻牌不重置位置，拖动不改变牌面。
  Revealing preserves position; dragging preserves the face.
- 双击空白桌面可找回移出屏幕的牌。
  Double-tapping empty space retrieves off-screen cards.
- 检查红蓝素材、全部 52 张牌、后台返回、系统重建及重复开始。
  Check both colors, all 52 cards, background/resume, recreation, and repeated sessions.
- 在真机验证长时间操作、屏幕边缘、方向变化及断网体验。
  Verify sustained use, screen edges, orientation changes, and offline use on a phone.

自动检查运行方式见[开发说明](DEVELOPMENT.md)。
For automated checks, see [Development](DEVELOPMENT.md).

历史本地验证通过 14 项单元测试和 12 项设备测试；发布前请针对实际提交重新运行。原始报告不包含在公开仓库中。
Previous local validation passed 14 unit tests and 12 device tests. Rerun for the actual release commit. Raw local reports are excluded from the public repository.
