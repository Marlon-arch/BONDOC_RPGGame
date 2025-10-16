package com.example.bondoc_rpggame.model

sealed class SkillResult {
    data class AttackResult(
        val attacker: String,
        val defender: String,
        val rawDamage: Int,
        val finalDamage: Int,
        val isCrit: Boolean
    ) : SkillResult()

    data class HealResult(
        val target: String,
        val amount: Int
    ) : SkillResult()

    data class BlockResult(
        val target: String,
        val durationMs: Long,
        val reductionPercent: Int
    ) : SkillResult()

    data class SpecialResult(
        val attacker: String,
        val defender: String,
        val finalDamage: Int,
        val isCrit: Boolean
    ) : SkillResult()
}
