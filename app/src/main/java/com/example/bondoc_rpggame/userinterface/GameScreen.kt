package com.example.bondoc_rpggame.userinterface

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bondoc_rpggame.GameViewModel
import com.example.bondoc_rpggame.R
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign


@Composable
fun GameScreen(viewModel: GameViewModel = viewModel()) {
    val playerHp by viewModel.playerHealth.collectAsState()
    val enemyHp by viewModel.enemyHealth.collectAsState()

    val playerAttackRem by viewModel.playerAttackRemaining.collectAsState()
    val playerBlockRem by viewModel.playerBlockRemaining.collectAsState()
    val playerHealRem by viewModel.playerHealRemaining.collectAsState()
    val playerSpecialRem by viewModel.playerSpecialRemaining.collectAsState()

    val enemyAttackRem by viewModel.enemyAttackRemaining.collectAsState()
    val enemyBlockRem by viewModel.enemyBlockRemaining.collectAsState()
    val enemyHealRem by viewModel.enemyHealRemaining.collectAsState()
    val enemySpecialRem by viewModel.enemySpecialRemaining.collectAsState()

    val playerHit by viewModel.playerHit.collectAsState()
    val enemyHit by viewModel.enemyHit.collectAsState()
    val playerHealed by viewModel.playerHealed.collectAsState()
    val enemyHealed by viewModel.enemyHealed.collectAsState()

    val logs by viewModel.log.collectAsState()
    val playerBlocking by viewModel.playerBlocking.collectAsState()
    val enemyBlocking by viewModel.enemyBlocking.collectAsState()

    val isGameOver by viewModel.gameOver.collectAsState()
    val resultText by viewModel.gameResult.collectAsState()

    var lastPlayerHp by remember { mutableStateOf(playerHp) }
    var lastEnemyHp by remember { mutableStateOf(enemyHp) }

    val playerFlash = animateFloatAsState(
        targetValue = if (playerHp < lastPlayerHp) 1f else 0f,
        animationSpec = tween(250), label = "pFlash"
    )
    val enemyFlash = animateFloatAsState(
        targetValue = if (enemyHp < lastEnemyHp) 1f else 0f,
        animationSpec = tween(250), label = "eFlash"
    )
    LaunchedEffect(playerHp) { lastPlayerHp = playerHp }
    LaunchedEffect(enemyHp) { lastEnemyHp = enemyHp }

    val idleBounce = rememberInfiniteTransition(label = "bounce")
    val playerScale by idleBounce.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(tween(850, easing = LinearEasing), RepeatMode.Reverse),
        label = "pScale"
    )
    val enemyScale by idleBounce.animateFloat(
        initialValue = 1.02f,
        targetValue = 0.98f,
        animationSpec = infiniteRepeatable(tween(920, easing = LinearEasing), RepeatMode.Reverse),
        label = "eScale"
    )

    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Coroutine RPG Game",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Enemy card
            CharacterCard(
                title = "WILD SLIME",
                hp = enemyHp,
                maxHp = viewModel.enemy.maxHealth,
                attackRem = enemyAttackRem,
                blockRem = enemyBlockRem,
                healRem = enemyHealRem,
                specialRem = enemySpecialRem,
                scale = enemyScale,
                blockVisible = enemyBlocking,
                hpFlash = enemyFlash.value,
                isHit = enemyHit,
                isHealed = enemyHealed,
                isPlayer = false
            )

            // Player card
            CharacterCard(
                title = "BLASTOISE",
                hp = playerHp,
                maxHp = viewModel.player.maxHealth,
                attackRem = playerAttackRem,
                blockRem = playerBlockRem,
                healRem = playerHealRem,
                specialRem = playerSpecialRem,
                scale = playerScale,
                blockVisible = playerBlocking,
                hpFlash = playerFlash.value,
                isHit = playerHit,
                isHealed = playerHealed,
                isPlayer = true
            )

            // Combat log
            Text(
                "Combat Log",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Divider()
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFFF7F7F7), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                reverseLayout = true
            ) {
                itemsIndexed(logs, key = { idx, item -> item.hashCode() + idx }) { _, item ->
                    var visible by remember(item) { mutableStateOf(false) }
                    LaunchedEffect(item) { visible = true }

                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(animationSpec = tween(200))
                    ) {
                        Text(
                            text = "• $item",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .padding(vertical = 2.dp)
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
    if (isGameOver) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Battle Over") },
            text = { Text(resultText) },
            confirmButton = {
                Button(onClick = { viewModel.resetGame() }) {
                    Text("Reset")
                }
            }
        )
    }
}

@Composable
private fun CharacterCard(
    title: String,
    hp: Int,
    maxHp: Int,
    attackRem: Long,
    blockRem: Long,
    healRem: Long,
    specialRem: Long,
    scale: Float,
    blockVisible: Boolean,
    hpFlash: Float,
    isHit: Boolean,
    isHealed: Boolean,
    isPlayer: Boolean
) {
    val cardShape = RoundedCornerShape(20.dp)
    val gradientBrush = if (isPlayer) {
        Brush.verticalGradient(
            colors = listOf(Color(0xFFF2F7FF), Color(0xFFDCE8FF))
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(Color(0xFFF2FFF5), Color(0xFFDFF8E5))
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .background(gradientBrush)
    ) {
        Card(
            shape = cardShape,
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isPlayer) "Blastoise" else "Wild Slime",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(scale)
                        .clip(RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(
                            id = if (isPlayer) R.drawable.player_sprite else R.drawable.enemy_sprite
                        ),
                        contentDescription = "${if (isPlayer) "Blastoise" else "Slime"} sprite",
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { if (!isPlayer) scaleX = -1f }
                    )

                    // Overlay fx
                    AnimatedOverlay(visible = blockVisible, color = Color(0xFF4DA3FF), alphaOn = 0.20f)
                    AnimatedOverlay(visible = isHealed,     color = Color(0xFF67D36E), alphaOn = 0.22f)
                    AnimatedOverlay(visible = isHit,        color = Color.White,       alphaOn = 0.35f)
                }

                // Blocking badge
                AnimatedVisibility(blockVisible) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFE6F0FF),
                        border = ButtonDefaults.outlinedButtonBorder
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("BLOCKING", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                // HP bar
                val progress = hp.toFloat() / maxHp.toFloat()
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .background(
                            if (hpFlash > 0f) Color(0xFFFFE5E5) else Color.Transparent,
                            RoundedCornerShape(6.dp)
                        ),
                    color = Color(0xFF4CAF50),
                    trackColor = Color(0xFFE8F5E9)
                )
                Text("HP: $hp / $maxHp", fontWeight = FontWeight.Medium)

                // Cooldowns
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CooldownIconTimer(attackRem, Icons.Default.Bolt,     "Attack", Modifier.weight(1f))
                    CooldownIconTimer(blockRem,  Icons.Default.Security, "Block",  Modifier.weight(1f))
                    CooldownIconTimer(healRem,   Icons.Default.Healing,  "Heal",   Modifier.weight(1f))
                }
            }
        }
    }
}


@Composable
private fun CooldownIconTimer(
    remainingMs: Long,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDesc: String,
    modifier: Modifier = Modifier
) {
    val seconds = (remainingMs / 1000.0).format(1)

    Surface(
        modifier = modifier
            .height(34.dp),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 0.dp,
        border = ButtonDefaults.outlinedButtonBorder,
        color = Color(0xFFEFF3FF)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = contentDesc,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "${seconds}s",
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Clip
            )
        }
    }
}

@Composable
private fun AnimatedOverlay(
    visible: Boolean,
    color: Color,
    alphaOn: Float
) {
    val target = if (visible) alphaOn else 0f
    val alpha by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = if (visible) 80 else 180),
        label = "overlayAlpha"
    )
    if (alpha > 0f) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color.copy(alpha = alpha), shape = RoundedCornerShape(12.dp))
        )
    }
}

fun Double.format(digits: Int): String = "%.${digits}f".format(this)
