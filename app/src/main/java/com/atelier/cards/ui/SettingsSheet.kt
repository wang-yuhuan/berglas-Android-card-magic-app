package com.atelier.cards.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.atelier.cards.TableState
import com.atelier.cards.domain.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    state: TableState,
    onDismiss: () -> Unit,
    onColor: (DeckColor) -> Unit,
    onAssign: (Quadrant, Suit) -> Unit,
    onRehearsal: (Boolean) -> Unit,
    onReset: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 26.dp)
            .padding(bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("演出准备", fontSize = 25.sp, fontFamily = FontFamily.Serif)
            Text("设置会保存在本机。关闭后直接返回牌桌。", fontSize = 12.sp, color = Ivory.copy(alpha = .55f))
            Text("牌组", color = Gold)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(selected = state.color == DeckColor.RED, onClick = { onColor(DeckColor.RED) }, label = { Text("酒红 · Red") })
                FilterChip(selected = state.color == DeckColor.BLUE, onClick = { onColor(DeckColor.BLUE) }, label = { Text("海军蓝 · Blue") })
            }
            HorizontalDivider(color = Gold.copy(alpha = .15f))
            Text("花色位置", color = Gold)
            Text("以整张牌桌的中心划分四象限。选择花色会自动交换对应位置；已锁定的牌不受影响。",
                fontSize = 12.sp, color = Ivory.copy(alpha = .6f))
            Quadrant.entries.forEach { quadrant ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(quadrant.chinese, Modifier.width(40.dp), fontSize = 12.sp)
                    Suit.entries.forEach { suit ->
                        FilterChip(selected = state.mapping[quadrant] == suit, onClick = { onAssign(quadrant, suit) },
                            label = { Text(suit.symbol, fontSize = 18.sp) }, modifier = Modifier.weight(1f))
                    }
                }
            }
            HorizontalDivider(color = Gold.copy(alpha = .15f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("练习辅助")
                    Text("显示象限与点数；正式表演请关闭。", fontSize = 11.sp, color = Ivory.copy(alpha = .55f))
                }
                Switch(checked = state.rehearsal, onCheckedChange = onRehearsal)
            }
            Text("单指操作：在牌盒中央区域向下滑动取牌。\n按住任意牌拖动；同一根手指双击某张牌翻开。\n牌可拖出屏幕；双击空白处展开并找回全部牌。\n长按空白处打开设置；秘密秒每 1.5 秒跳一次。\n盒装状态：底部 ◇ 双击重置、长按设置。\n第一次翻开的任何一张牌都是目标牌；之后不重复。",
                fontSize = 12.sp, lineHeight = 21.sp, color = Ivory.copy(alpha = .65f))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onReset, modifier = Modifier.weight(1f)) { Text("重新开始") }
                Button(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("返回牌桌") }
            }
        }
    }
}

