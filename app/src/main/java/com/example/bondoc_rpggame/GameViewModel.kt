package com.example.bondoc_rpggame

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bondoc_rpggame.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

class GameViewModel : ViewModel() {

    val player = Player()
    val enemy = Enemy()

    private val playerAttack = Skill("p_attack", "Attack", cooldownMs = 2200)
    private val playerBlock  = Skill("p_block", "Block", cooldownMs = 6000, durationMs = 2000)
    private val playerHeal   = Skill("p_heal", "Heal",  cooldownMs = 8000)
    private val playerSpecial= Skill("p_special", "Special", cooldownMs = 12000)

    private val enemyAttack  = Skill("e_attack", "Attack", cooldownMs = 2000)
    private val enemyBlock   = Skill("e_block", "Block",  cooldownMs = 7000, durationMs = 1800)
    private val enemyHeal    = Skill("e_heal", "Heal",   cooldownMs = 9000)
    private val enemySpecial = Skill("e_special", "Special", cooldownMs = 14000)

    private val _playerHealth = MutableStateFlow(player.currentHealth)
    val playerHealth = _playerHealth.asStateFlow()

    private val _enemyHealth = MutableStateFlow(enemy.currentHealth)
    val enemyHealth = _enemyHealth.asStateFlow()

    private val _playerAttackRemaining = MutableStateFlow(playerAttack.cooldownMs)
    val playerAttackRemaining = _playerAttackRemaining.asStateFlow()

    private val _playerBlockRemaining = MutableStateFlow(playerBlock.cooldownMs)
    val playerBlockRemaining = _playerBlockRemaining.asStateFlow()

    private val _playerHealRemaining = MutableStateFlow(playerHeal.cooldownMs)
    val playerHealRemaining = _playerHealRemaining.asStateFlow()

    private val _playerSpecialRemaining = MutableStateFlow(playerSpecial.cooldownMs)
    val playerSpecialRemaining = _playerSpecialRemaining.asStateFlow()

    private val _enemyAttackRemaining = MutableStateFlow(enemyAttack.cooldownMs)
    val enemyAttackRemaining = _enemyAttackRemaining.asStateFlow()

    private val _enemyBlockRemaining = MutableStateFlow(enemyBlock.cooldownMs)
    val enemyBlockRemaining = _enemyBlockRemaining.asStateFlow()

    private val _enemyHealRemaining = MutableStateFlow(enemyHeal.cooldownMs)
    val enemyHealRemaining = _enemyHealRemaining.asStateFlow()

    private val _enemySpecialRemaining = MutableStateFlow(enemySpecial.cooldownMs)
    val enemySpecialRemaining = _enemySpecialRemaining.asStateFlow()

    private val _log = MutableStateFlow<List<String>>(emptyList())
    val log = _log.asStateFlow()

    val playerBlocking = MutableStateFlow(false)
    val enemyBlocking = MutableStateFlow(false)

    val playerHit = MutableStateFlow(false)
    val enemyHit = MutableStateFlow(false)
    val playerHealed = MutableStateFlow(false)
    val enemyHealed = MutableStateFlow(false)

    val gameOver = MutableStateFlow(false)
    val gameResult = MutableStateFlow("")

    private val runningJobs = mutableListOf<Job>()

    init {
        resetGame()
    }

    private fun startSkillTimerLoop(
        owner: Character,
        skill: Skill,
        remainingFlow: MutableStateFlow<Long>,
        onTrigger: suspend () -> Unit
    ): Job = viewModelScope.launch(Dispatchers.Default) {
        var remaining = remainingFlow.value
        val step = 100L

        while (isActive && player.isAlive() && enemy.isAlive()) {
            delay(step)
            remaining = (remaining - step).coerceAtLeast(0)
            remainingFlow.value = remaining

            if (remaining == 0L) {
                onTrigger()
                remaining = skill.cooldownMs
                remainingFlow.value = remaining
            }
        }
    }

    private fun performAttack(attacker: Character, defender: Character, isSpecial: Boolean = false) {
        if (!attacker.isAlive() || !defender.isAlive()) return

        val (damage, isCrit) = attacker.computeAttackDamageAgainst(defender)

        // Stronger multiplier
        val final = if (isSpecial) (damage * 1.5).roundToInt() else damage

        // Block reduce if defender is blocking
        val reduced = if (defender.isBlocking)
            (final * (100 - defender.blockReductionPercent) / 100.0).roundToInt()
        else final

        defender.applyDamage(reduced)

        (viewModelScope.launch {
            if (defender === player) {
                playerHit.value = true
                delay(150)
                playerHit.value = false
            } else {
                enemyHit.value = true
                delay(150)
                enemyHit.value = false
            }
        })

        updateHpFlows()

        val result: SkillResult = if (isSpecial) {
            SkillResult.SpecialResult(attacker.name, defender.name, reduced, isCrit)
        } else {
            SkillResult.AttackResult(attacker.name, defender.name, final, reduced, isCrit)
        }
        appendLog(formatResult(result))
        checkGameEnd()
    }

    private fun performBlock(target: Character, durationMs: Long) {
        if (!target.isAlive()) return

        target.isBlocking = true
        if (target === player) playerBlocking.value = true else enemyBlocking.value = true

        val result = SkillResult.BlockResult(target.name, durationMs, target.blockReductionPercent)
        appendLog(formatResult(result))

        // Turn off after duration
        viewModelScope.launch {
            delay(durationMs)
            target.isBlocking = false
            if (target === player) playerBlocking.value = false else enemyBlocking.value = false
        }
    }

    private fun performHeal(target: Character) {
        if (!target.isAlive()) return
        val amount = (target.maxHealth * 0.18).roundToInt() // heal 18% HP
        target.heal(amount)

        viewModelScope.launch {
            if (target === player) {
                playerHealed.value = true
                delay(250)
                playerHealed.value = false
            } else {
                enemyHealed.value = true
                delay(250)
                enemyHealed.value = false
            }
        }

        updateHpFlows()
        appendLog(formatResult(SkillResult.HealResult(target.name, amount)))
    }

    private fun updateHpFlows() {
        _playerHealth.value = player.currentHealth
        _enemyHealth.value = enemy.currentHealth
    }

    private fun appendLog(line: String) {
        _log.value = (listOf(line) + _log.value).take(60) // keep recent 60 lines
    }

    private fun formatResult(res: SkillResult): String = when (res) {
        is SkillResult.AttackResult ->
            "${res.attacker} attacks ${res.defender} for ${res.finalDamage} dmg" +
                    (if (res.isCrit) " (CRIT!)" else "") +
                    (if (res.finalDamage < res.rawDamage) " [blocked]" else "")
        is SkillResult.HealResult ->
            "${res.target} heals ${res.amount} HP"
        is SkillResult.BlockResult ->
            "${res.target} is blocking (${res.reductionPercent}% for ${res.durationMs}ms)"
        is SkillResult.SpecialResult ->
            "${res.attacker} uses SPECIAL on ${res.defender} for ${res.finalDamage} dmg" +
                    (if (res.isCrit) " (CRIT!)" else "")
    }

    private fun checkGameEnd() {
        if (!player.isAlive() || !enemy.isAlive()) {
            val msg = if (player.isAlive()) "You Win!" else "You Lose!"
            appendLog(msg)
            stopAllTimers()
            gameResult.value = msg
            gameOver.value = true
        }
    }

    fun resetGame() {
        stopAllTimers()
        gameOver.value = false
        gameResult.value = ""

        player.apply {
            currentHealth = maxHealth
            isBlocking = false
        }
        enemy.apply {
            currentHealth = maxHealth
            isBlocking = false
        }
        updateHpFlows()
        playerBlocking.value = false
        enemyBlocking.value = false

        _playerAttackRemaining.value = playerAttack.cooldownMs
        _playerBlockRemaining.value = playerBlock.cooldownMs
        _playerHealRemaining.value = playerHeal.cooldownMs
        _playerSpecialRemaining.value = playerSpecial.cooldownMs

        _enemyAttackRemaining.value = enemyAttack.cooldownMs
        _enemyBlockRemaining.value = enemyBlock.cooldownMs
        _enemyHealRemaining.value = enemyHeal.cooldownMs
        _enemySpecialRemaining.value = enemySpecial.cooldownMs

        _log.value = listOf("A wild Slime appears! Battle starts.")

        runningJobs += startSkillTimerLoop(player, playerAttack, _playerAttackRemaining) {
            performAttack(player, enemy)
        }
        runningJobs += startSkillTimerLoop(player, playerBlock, _playerBlockRemaining) {
            performBlock(player, playerBlock.durationMs)
        }
        runningJobs += startSkillTimerLoop(player, playerHeal, _playerHealRemaining) {
            performHeal(player)
        }
        runningJobs += startSkillTimerLoop(player, playerSpecial, _playerSpecialRemaining) {
            performAttack(player, enemy, isSpecial = true)
        }

        runningJobs += startSkillTimerLoop(enemy, enemyAttack, _enemyAttackRemaining) {
            performAttack(enemy, player)
        }
        runningJobs += startSkillTimerLoop(enemy, enemyBlock, _enemyBlockRemaining) {
            performBlock(enemy, enemyBlock.durationMs)
        }
        runningJobs += startSkillTimerLoop(enemy, enemyHeal, _enemyHealRemaining) {
            performHeal(enemy)
        }
        runningJobs += startSkillTimerLoop(enemy, enemySpecial, _enemySpecialRemaining) {
            performAttack(enemy, player, isSpecial = true)
        }
    }

    fun stopAllTimers() {
        runningJobs.forEach { it.cancel() }
        runningJobs.clear()
    }

    override fun onCleared() {
        super.onCleared()
        stopAllTimers()
    }
}
