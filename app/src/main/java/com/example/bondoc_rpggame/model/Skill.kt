package com.example.bondoc_rpggame.model

data class Skill(
    val id: String,
    val name: String,
    val cooldownMs: Long,
    val durationMs: Long = 0L
)
