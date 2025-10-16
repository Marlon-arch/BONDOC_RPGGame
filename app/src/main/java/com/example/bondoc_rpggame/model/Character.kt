package com.example.bondoc_rpggame.model

import kotlin.math.max
import kotlin.random.Random

open class Character(
    val name: String,
    val maxHealth: Int,
    val attack: Int,
    val defense: Int,
    val critChance: Double = 0.1,
    val critMultiplier: Double = 1.5,
) {
    var currentHealth: Int = maxHealth
        internal set

    var isBlocking: Boolean = false
    var blockReductionPercent: Int = 50

    fun applyDamage(amount: Int) {
        currentHealth = max(0, currentHealth - max(0, amount))
    }

    fun heal(amount: Int) {
        currentHealth = max(0, minOf(maxHealth, currentHealth + max(0, amount)))
    }

    fun isAlive() = currentHealth > 0

    fun computeAttackDamageAgainst(defender: Character): Pair<Int, Boolean> {
        val base = Random.nextInt(attack / 2, attack + 1)
        val isCrit = Random.nextDouble() < critChance
        val critScaled = if (isCrit) (base * critMultiplier).toInt() else base
        val mitigated = max(1, critScaled - (defender.defense / 2))
        return mitigated to isCrit
    }
}

class Player : Character("Player", 100, 20, 8, critChance = 0.15, critMultiplier = 1.6)
class Enemy  : Character("Slime", 110, 18, 6, critChance = 0.10, critMultiplier = 1.5)
