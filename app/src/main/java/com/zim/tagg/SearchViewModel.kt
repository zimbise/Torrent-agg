package com.zim.tagg

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch

class SearchViewModel(app: Application) : AndroidViewModel(app) {
    val results = MutableLiveData<List<ResultItem>>()
    private val mgr = ProviderManager(app)

    fun search(query: String) {
        viewModelScope.launch {
            val items = mgr.searchAll(query)
            results.postValue(items.sortedByDescending { it.seeders ?: 0 })
        }
    }
}
