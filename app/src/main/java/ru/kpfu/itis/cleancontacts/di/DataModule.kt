package ru.kpfu.itis.cleancontacts.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.kpfu.itis.cleancontacts.data.repository.ContactsRepositoryImpl
import ru.kpfu.itis.cleancontacts.domain.repository.ContactsRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    abstract fun bindRepository(impl: ContactsRepositoryImpl): ContactsRepository
}