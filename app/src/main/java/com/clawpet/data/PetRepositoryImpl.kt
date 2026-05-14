package com.clawpet.data

import com.clawpet.domain.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PetRepositoryImpl @Inject constructor(
    private val petDao: PetDao,
) : PetRepository {

    override fun observePet(): Flow<PetState> {
        return petDao.observePet().map { entity ->
            entity?.toDomain() ?: PetState()
        }
    }

    override suspend fun getPet(): PetState {
        return petDao.getPet()?.toDomain() ?: PetState().also { savePet(it) }
    }

    override suspend fun savePet(state: PetState) {
        petDao.savePet(state.toEntity())
    }

    override suspend fun performAction(action: PetAction): PetState {
        val current = getPet()
        val updated = applyAction(current, action)
        savePet(updated)
        return updated
    }

    override suspend fun tickDecay(): PetState {
        val current = getPet()
        val minutesElapsed = (System.currentTimeMillis() - current.lastInteraction) / 60_000
        if (minutesElapsed < 5) return current // only decay after 5 min idle
        val decayed = decayStats(current, minutesElapsed.coerceAtMost(120)) // cap at 2h
        savePet(decayed)
        return decayed
    }

    private fun PetEntity.toDomain() = PetState(
        name = name, hunger = hunger, happiness = happiness, energy = energy,
        mood = PetMood.valueOf(mood), lastInteraction = lastInteraction,
        isAwake = isAwake, level = level, xp = xp, xpToNext = xpToNext,
    )

    private fun PetState.toEntity() = PetEntity(
        name = name, hunger = hunger, happiness = happiness, energy = energy,
        mood = mood.name, lastInteraction = lastInteraction,
        isAwake = isAwake, level = level, xp = xp, xpToNext = xpToNext,
    )
}