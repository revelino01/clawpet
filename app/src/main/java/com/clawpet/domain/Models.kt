package com.clawpet.domain

/** The claw pet's current state. */
data class PetState(
    val name: String = "Clawy",
    val hunger: Int = 80,      // 0-100, 0 = starving
    val happiness: Int = 80,    // 0-100, 0 = miserable
    val energy: Int = 80,       // 0-100, 0 = exhausted
    val mood: PetMood = PetMood.HAPPY,
    val lastInteraction: Long = System.currentTimeMillis(),
    val isAwake: Boolean = true,
    val level: Int = 1,
    val xp: Int = 0,
    val xpToNext: Int = 100,
)

enum class PetMood(val emoji: String, val label: String) {
    HAPPY("😊", "Happy"),
    HUNGRY("😋", "Hungry"),
    SLEEPY("😴", "Sleepy"),
    SAD("😢", "Sad"),
    EXCITED("🤩", "Excited"),
    DEAD("💀", "Fainted"),
}

enum class PetAction(val xpGain: Int) {
    FEED(xpGain = 10),
    PET(xpGain = 5),
    PLAY(xpGain = 15),
    WAKE(xpGain = 2),
}

data class PetInteraction(
    val action: PetAction,
    val timestamp: Long = System.currentTimeMillis(),
)

/** Calculate mood from stats. */
fun deriveMood(state: PetState): PetMood {
    if (state.hunger <= 0 && state.energy <= 0) return PetMood.DEAD
    if (!state.isAwake) return PetMood.SLEEPY
    if (state.hunger < 25) return PetMood.HUNGRY
    if (state.happiness < 25) return PetMood.SAD
    if (state.happiness > 80 && state.hunger > 60) return PetMood.EXCITED
    return PetMood.HAPPY
}

/** Decay stats over time (called by WorkManager). */
fun decayStats(state: PetState, minutesElapsed: Long): PetState {
    val decay = (minutesElapsed * 0.15).toInt().coerceIn(0, 5)
    return state.copy(
        hunger = (state.hunger - decay).coerceAtLeast(0),
        happiness = (state.happiness - (decay * 0.8).toInt()).coerceAtLeast(0),
        energy = (state.energy - (decay * 0.5).toInt()).coerceAtLeast(0),
        isAwake = state.energy > 5 || !state.isAwake,
    ).let { it.copy(mood = deriveMood(it)) }
}

/** Apply an action to the pet. */
fun applyAction(state: PetState, action: PetAction): PetState {
    val newXp = state.xp + action.xpGain
    val leveled = newXp >= state.xpToNext
    return when (action) {
        PetAction.FEED -> state.copy(
            hunger = (state.hunger + 30).coerceAtMost(100),
            happiness = (state.happiness + 5).coerceAtMost(100),
            xp = if (leveled) newXp - state.xpToNext else newXp,
            level = if (leveled) state.level + 1 else state.level,
            xpToNext = if (leveled) (state.xpToNext * 1.2).toInt() else state.xpToNext,
            lastInteraction = System.currentTimeMillis(),
        ).let { it.copy(mood = deriveMood(it)) }
        PetAction.PET -> state.copy(
            happiness = (state.happiness + 20).coerceAtMost(100),
            energy = (state.energy + 5).coerceAtMost(100),
            xp = if (leveled) newXp - state.xpToNext else newXp,
            level = if (leveled) state.level + 1 else state.level,
            xpToNext = if (leveled) (state.xpToNext * 1.2).toInt() else state.xpToNext,
            lastInteraction = System.currentTimeMillis(),
        ).let { it.copy(mood = deriveMood(it)) }
        PetAction.PLAY -> state.copy(
            happiness = (state.happiness + 25).coerceAtMost(100),
            hunger = (state.hunger - 10).coerceAtLeast(0),
            energy = (state.energy - 20).coerceAtLeast(0),
            xp = if (leveled) newXp - state.xpToNext else newXp,
            level = if (leveled) state.level + 1 else state.level,
            xpToNext = if (leveled) (state.xpToNext * 1.2).toInt() else state.xpToNext,
            lastInteraction = System.currentTimeMillis(),
        ).let { it.copy(mood = deriveMood(it)) }
        PetAction.WAKE -> state.copy(
            isAwake = true,
            energy = (state.energy + 40).coerceAtMost(100),
            xp = if (leveled) newXp - state.xpToNext else newXp,
            level = if (leveled) state.level + 1 else state.level,
            xpToNext = if (leveled) (state.xpToNext * 1.2).toInt() else state.xpToNext,
            lastInteraction = System.currentTimeMillis(),
        ).let { it.copy(mood = deriveMood(it)) }
    }
}