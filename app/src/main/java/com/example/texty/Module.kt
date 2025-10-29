package com.example.texty

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.example.texty.ui.view_model.AuthViewModel
import com.example.texty.ui.view_model.PostViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.dsl.viewModel
import org.koin.core.scope.get
import org.koin.dsl.module


private const val USER_PREFERENCES = "user_pre"
val appModule = module {
    single {
        PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler(produceNewData = { emptyPreferences() }),
            migrations = listOf(SharedPreferencesMigration(get(), USER_PREFERENCES)),
            scope = CoroutineScope(Dispatchers.IO), // Changed to IO for file ops
            produceFile = { get<Context>().preferencesDataStoreFile(USER_PREFERENCES) }
        )
    }
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
    single { FirebaseDatabase.getInstance() }
    single { UserPreferencesManager(get()) }

    viewModel { AuthViewModel(get(), get()) }
    viewModel { PostViewModel(get(), get(), get(), get()) }
   }
