package com.clawpet.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "pet_state")
data class PetEntity(
    @PrimaryKey val id: Int = 1,
    val name: String = "Clawy",
    val hunger: Int = 80,
    val happiness: Int = 80,
    val energy: Int = 80,
    val mood: String = "HAPPY",
    val lastInteraction: Long = System.currentTimeMillis(),
    val isAwake: Boolean = true,
    val level: Int = 1,
    val xp: Int = 0,
    val xpToNext: Int = 100,
)

@Dao
interface PetDao {
    @Query("SELECT * FROM pet_state WHERE id = 1")
    fun observePet(): Flow<PetEntity?>

    @Query("SELECT * FROM pet_state WHERE id = 1")
    suspend fun getPet(): PetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePet(pet: PetEntity)

    @Query("DELETE FROM pet_state")
    suspend fun deleteAll()
}

@Database(entities = [PetEntity::class], version = 1, exportSchema = false)
abstract class PetDatabase : RoomDatabase() {
    abstract fun petDao(): PetDao
}