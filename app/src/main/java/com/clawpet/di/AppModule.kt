package com.clawpet.di

import android.content.Context
import androidx.room.Room
import com.clawpet.data.PetDao
import com.clawpet.data.PetDatabase
import com.clawpet.data.PetRepositoryImpl
import com.clawpet.domain.PetRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PetDatabase =
        Room.databaseBuilder(context, PetDatabase::class.java, "clawpet-db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun providePetDao(db: PetDatabase): PetDao = db.petDao()

    @Provides @Singleton
    fun providePetRepository(impl: PetRepositoryImpl): PetRepository = impl
}