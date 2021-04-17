package com.example.todo.ui.task

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.createDataStore
import com.example.todo.ui.TAG
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject


data class PreferencesExtracted(
    val sortOrder: PreferenceManager.SortBy,
    val hideComplete: Boolean
)


class PreferenceManager
@Inject
constructor(@ApplicationContext context: Context) {
    private val dataStore = context.createDataStore("data_store")

    val preferenceFlow = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
                throw exception
            } else {
                Log.e(TAG, "error retrieving preferences", exception)
            }
        }
        .map { pref ->
            val sortOrder =
                SortBy.valueOf(
                    pref[PreferencesKey.sortOrder] ?: SortBy.SORT_BY_DATE.name
                )

            val hideCompleted =
                pref[PreferencesKey.hideComplete] ?: false

            PreferencesExtracted(sortOrder, hideCompleted)
        }


    suspend fun onSortOrderSelect(orderBy: SortBy) {
        dataStore.edit { pref ->
            pref[PreferencesKey.sortOrder] = orderBy.name
        }
    }

    suspend fun onHideCompletedSelect(hide: Boolean) {
        dataStore.edit { pref ->
            pref[PreferencesKey.hideComplete] = hide
        }
    }

    private object PreferencesKey {
        val sortOrder = preferencesKey<String>("sort_order")
        val hideComplete = preferencesKey<Boolean>("hide_complete")
    }

    enum class SortBy { SORT_BY_NAME, SORT_BY_DATE }

}