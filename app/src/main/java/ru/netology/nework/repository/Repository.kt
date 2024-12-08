package ru.netology.nework.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import retrofit2.Response
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.FeedItem
import ru.netology.nework.dto.Job
import ru.netology.nework.dto.Media
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.UserResponse
import ru.netology.nework.error.ApiErrorAuth
import ru.netology.nework.model.AttachmentModel

interface Repository {
    val dataPost: Flow<PagingData<FeedItem>>
    val dataEvent: Flow<PagingData<FeedItem>>
    val dataUser: Flow<PagingData<FeedItem>>
    val dataJob: Flow<List<Job>>

    suspend fun registration(
        login: String,
        name: String,
        pass: String,
        attachmentModel: AttachmentModel?
    ): ApiErrorAuth

    suspend fun login(login: String, pass: String): ApiErrorAuth
    fun logout()

    suspend fun getUsers()
    suspend fun getUser(id: Int): UserResponse

    suspend fun likePost(post: Post)
    suspend fun savePost(post: Post)
    suspend fun savePostWithAttachment(post: Post, attachmentModel: AttachmentModel)
    suspend fun deletePost(id: Int)

    suspend fun saveEvent(event: Event)
    suspend fun saveEventWithAttachment(event: Event, attachmentModel: AttachmentModel)
    suspend fun deleteEvent(id: Int)
    suspend fun likeEvent(event: Event)

    suspend fun getMyJobs()
    suspend fun getJobs(userId: Int)
    suspend fun saveJob(job: Job)
    suspend fun deleteJob(id: Int)
    suspend fun mediaSaveMedia(file: MultipartBody.Part): Response<Media>
}