package com.alpha.dots.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStoreFile
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.alpha.dots.Application
import com.alpha.dots.ui.viewModel.LoginViewModel
import com.alpha.dots.ui.viewModel.SettingsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
internal object AppModule {

    @Singleton
    @Provides
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { context.dataStoreFile("settings.preferences_pb") }
        )
    }

    @Singleton
    @Provides
    fun provideFirebaseDatabase(): FirebaseDatabase {
        return FirebaseDatabase.getInstance()
    }

    @Singleton
    @Provides
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Singleton
    @Provides
    fun provideCoroutineDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Singleton
    @Provides
    fun provideContext(@ApplicationContext appContext: Context): Context {
        return appContext
    }

    @Singleton
    @Provides
    fun provideApplication(@ApplicationContext appContext: Context): Application {
        return appContext as Application
    }

    @Provides
    @Singleton
    fun provideSettingsViewModel(
        @ApplicationContext context: Context,
        dataStore: DataStore<Preferences>
    ): SettingsViewModel {
        return SettingsViewModel(context, dataStore)
    }

    @Provides
    @Singleton
    fun provideLoginViewModel(
        application: Application,
        firebaseAuth: FirebaseAuth,
        firebaseFirestore: FirebaseFirestore
    ): LoginViewModel {
        return LoginViewModel(application, firebaseAuth, firebaseFirestore)
    }
}
