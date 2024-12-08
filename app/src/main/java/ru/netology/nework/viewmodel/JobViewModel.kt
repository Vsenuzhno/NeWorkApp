package ru.netology.nework.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dto.Job
import ru.netology.nework.model.FeedModelState
import ru.netology.nework.repository.Repository
import java.time.OffsetDateTime
import javax.inject.Inject

private val empty = Job(
    id = 0,
    name = "",
    position = "",
    start = OffsetDateTime.now(),
    finish = OffsetDateTime.now(),
    link = "",
    ownedByMe = false,
)

@HiltViewModel
class JobViewModel @Inject constructor(
    private val repository: Repository,
    appAuth: AppAuth,
) : ViewModel() {
    private val _dataState = MutableLiveData(FeedModelState())
    private val userId = MutableLiveData<Int>()
    private val edited = MutableLiveData(empty)

    @OptIn(ExperimentalCoroutinesApi::class)
    val data: Flow<List<Job>> = appAuth.authStateFlow.flatMapLatest { (myId, _) ->
        repository.dataJob.map {
            it.map { job ->
                job.copy(
                    ownedByMe = userId.value == myId
                )
            }
        }
    }.flowOn(Dispatchers.Default)

    fun getJobs(userId: Int?) = viewModelScope.launch {
        if (userId == null) {
            try {
                repository.getMyJobs()
                _dataState.postValue(FeedModelState())
            } catch (e: Exception) {
                e.printStackTrace()
                _dataState.postValue(FeedModelState(error = true))
            }
        } else {
            try {
                repository.getJobs(userId)
                _dataState.postValue(FeedModelState())
            } catch (e: Exception) {
                e.printStackTrace()
                _dataState.postValue(FeedModelState(error = true))
            }
        }
    }

    fun saveJob(
        name: String,
        position: String,
        start: OffsetDateTime,
        finish: OffsetDateTime,
        link: String?,
    ) {
        edited.value?.let {
            val jobCopy = it.copy(
                name = name.trim(),
                position = position.trim(),
                link = link?.trim(),
                start = start,
                finish = finish,
            )
            viewModelScope.launch {
                try {
                    repository.saveJob(jobCopy)
                    _dataState.value = FeedModelState()

                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)

                }
            }
        }
    }

    fun removeJob(id: Int) {
        viewModelScope.launch {
            try {
                repository.deleteJob(id)
                _dataState.value = FeedModelState()

            } catch (e: Exception) {
                _dataState.value = FeedModelState(error = true)

            }
        }
    }

    fun setId(id: Int) {
        userId.value = id
    }
}