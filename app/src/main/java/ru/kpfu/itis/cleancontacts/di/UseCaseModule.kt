package ru.kpfu.itis.cleancontacts.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.kpfu.itis.cleancontacts.data.usecase.DeleteDuplicatesUseCaseImpl
import ru.kpfu.itis.cleancontacts.data.usecase.GetContactsUseCaseImpl
import ru.kpfu.itis.cleancontacts.domain.usecase.DeleteDuplicatesUseCase
import ru.kpfu.itis.cleancontacts.domain.usecase.GetContactsUseCase

@Module
@InstallIn(SingletonComponent::class)
abstract class UseCaseModule {
    @Binds
    abstract fun bindGetContacts(impl: GetContactsUseCaseImpl): GetContactsUseCase
    @Binds
    abstract fun bindDeleteDuplicates(impl: DeleteDuplicatesUseCaseImpl): DeleteDuplicatesUseCase
}