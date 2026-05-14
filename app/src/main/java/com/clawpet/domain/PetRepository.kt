package com.clawpet.domain

import kotlinx.coroutines.flow.Flow

interface PetRepository {
    fun observePet(): Flow<PetState>
    suspend fun getPet(): PetState
    suspend fun savePet(state: PetState)
    suspend fun performAction(action: PetAction): PetState
    suspend fun tickDecay(): PetState
}