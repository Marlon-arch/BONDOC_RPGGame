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

    val logs by viewModel.log.collectAsState()
    val playerBlocking by viewModel.playerBlocking.collectAsState()
    val enemyBlocking by viewModel.enemyBlocking.collectAsState()

    // HP bar flash when damaged
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

    // idle bounce of sprites
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
                    Text("Coroutine RPG Battle", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    IconButton(onClick = { viewModel.resetGame() }) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "Reset")
                    }
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
                title = "Enemy",
                hp = enemyHp,
                maxHp = viewModel.enemy.maxHealth,
                attackRem = enemyAttackRem,
                blockRem = enemyBlockRem,
                healRem = enemyHealRem,
                specialRem = enemySpecialRem,
                scale = enemyScale,
                blockVisible = enemyBlocking,
                hpFlash = enemyFlash.value
            )

            // spacer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .background(Color(0xFFDDE7FF), RoundedCornerShape(8.dp))
            )

            // Player card
            CharacterCard(
                title = "Player",
                hp = playerHp,
                maxHp = viewModel.player.maxHealth,
                attackRem = playerAttackRem,
                blockRem = playerBlockRem,
                healRem = playerHealRem,
                specialRem = playerSpecialRem,
                scale = playerScale,
                blockVisible = playerBlocking,
                hpFlash = playerFlash.value
            )

            // Reset button
            OutlinedButton(
                onClick = { viewModel.resetGame() },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) { Text("Reset") }

            // Combat log
            Text("Combat Log", fontWeight = FontWeight.Bold)
            Divider()
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFFF7F7F7), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                reverseLayout = true
            ) {
                itemsIndexed(logs) { _, item ->
                    Text(
                        text = "• $item",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
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
    hpFlash: Float
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEFEFF))
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )

                //Sprite
                Image(
                    painter = painterResource(
                        if (title == "Player") R.drawable.player_sprite else R.drawable.enemy_sprite
                    ),
                    contentDescription = "$title sprite",
                    modifier = Modifier
                        .size(64.dp)
                        .scale(scale)
                        .graphicsLayer {
                            if (title != "Player") scaleX = -1f
                        }
                )
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
            )
            Text("HP: $hp / $maxHp", fontWeight = FontWeight.Medium)

            AnimatedVisibility(blockVisible) {
                AssistChip(
                    onClick = {},
                    label = { Text("BLOCKING") },
                    leadingIcon = {
                        Icon(Icons.Default.Security, contentDescription = null)
                    }
                )
            }

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

fun Double.format(digits: Int): String = "%.${digits}f".format(this)
