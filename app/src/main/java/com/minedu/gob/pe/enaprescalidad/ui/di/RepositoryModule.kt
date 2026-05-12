package com.minedu.gob.pe.enaprescalidad.ui.di

import com.minedu.gob.pe.enaprescalidad.ui.data.imp.SidebarRepositoryImpl
import com.minedu.gob.pe.enaprescalidad.ui.domain.repository.SidebarRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    /**
     * Hilt sabrá que cuando alguien pida SidebarRepository,
     * debe inyectar SidebarRepositoryImpl.
     */
    @Binds
    @Singleton
    abstract fun bindSidebarRepository(
        impl: SidebarRepositoryImpl
    ): SidebarRepository
}
