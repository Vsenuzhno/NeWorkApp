package ru.netology.nework.api

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.Job
import ru.netology.nework.dto.Media
import ru.netology.nework.dto.Post
import ru.netology.nework.dto.Token
import ru.netology.nework.dto.UserResponse

interface ApiService {

    //Users

    @FormUrlEncoded
    @POST("api/users/registration")
    suspend fun usersRegistration(
        @Field("login") login: String,
        @Field("pass") pass: String,
        @Field("name") name: String
    ): Response<Token>

    @Multipart
    @POST("api/users/registration")
    suspend fun usersRegistrationWithPhoto(
        @Query("login") login: String,
        @Query("pass") pass: String,
        @Query("name") name: String,
        @Part file: MultipartBody.Part
    ): Response<Token>

    @FormUrlEncoded
    @POST("api/users/authentication")
    suspend fun usersAuthentication(
        @Field("login") login: String,
        @Field("pass") pass: String
    ): Response<Token>

    @GET("api/users")
    suspend fun usersGetAllUser(): Response<List<UserResponse>>

    @GET("api/users/{id}")
    suspend fun usersGetUser(
        @Path("id") id: Int,
    ): Response<UserResponse>

    //Wall

    @POST("api/{authorId}/wall/{id}/likes")
    suspend fun wallLikesPost(
        @Path("authorId") authorId: Int,
        @Path("id") id: String,
    ): Response<Post>

    @DELETE("api/{authorId}/wall/{id}/likes")
    suspend fun wallUnLikesPost(
        @Path("authorId") authorId: Int,
        @Path("id") id: String,
    ): Response<Post>

    @GET("api/{authorId}/wall")
    suspend fun wallGetAllPost(
        @Path("authorId") authorId: Int,
    ): Response<List<Post>>

    @GET("api/{authorId}/wall/{id}/newer")
    suspend fun wallGetNewer(
        @Path("authorId") authorId: Int,
        @Path("id") id: Int,
    ): Response<List<Post>>

    @GET("api/{authorId}/wall/{id}/before")
    suspend fun wallGetBefore(
        @Path("authorId") authorId: Int,
        @Path("id") id: Int,
        @Query("count") count: Int,
    ): Response<List<Post>>

    @GET("api/{authorId}/wall/{id}/after")
    suspend fun wallGetAfter(
        @Path("authorId") authorId: Int,
        @Path("id") id: Int,
        @Query("count") count: Int,
    ): Response<List<Post>>

    @GET("api/{authorId}/wall/{id}")
    suspend fun wallGetPost(
        @Path("authorId") authorId: Int,
        @Path("id") id: Int,
    ): Response<Post>

    @GET("api/{authorId}/wall/latest")
    suspend fun wallGetLatest(
        @Path("authorId") authorId: Int,
        @Query("count") count: Int,
    ): Response<Post>

    //MyWall

    @POST("api/my/wall/{id}/likes")
    suspend fun myWallLikesPost(
        @Path("id") id: String,
    ): Response<Post>

    @DELETE("api/my/wall/{id}/likes")
    suspend fun myWallUnLikesPost(
        @Path("id") id: String,
    ): Response<Post>

    @GET("api/my/wall")
    suspend fun myWallGetAllPost(): Response<List<Post>>

    @GET("api/my/wall/{id}/newer")
    suspend fun myWallGetNewer(
        @Path("id") id: Int,
    ): Response<List<Post>>

    @GET("api/my/wall/{id}/before")
    suspend fun myWallGetBefore(
        @Path("id") id: Int,
        @Query("count") count: Int,
    ): Response<List<Post>>

    @GET("api/my/wall/{id}/after")
    suspend fun myWallGetAfter(
        @Path("id") id: Int,
        @Query("count") count: Int,
    ): Response<List<Post>>

    @GET("api/my/wall/{id}")
    suspend fun myWallGetPost(
        @Path("id") id: Int,
    ): Response<Post>

    @GET("api/my/wall/latest")
    suspend fun myWallGetLatest(
        @Query("count") count: Int,
    ): Response<Post>

    //Posts

    @GET("api/posts")
    suspend fun postsGetAllPost(): Response<List<Post>>

    @POST("api/posts")
    suspend fun postsSavePost(
        @Body post: Post,
    ): Response<Post>

    @POST("api/posts/{id}/likes")
    suspend fun postsLikePost(
        @Path("id") id: Int,
    ): Response<Post>

    @DELETE("api/posts/{id}/likes")
    suspend fun postsUnLikePost(
        @Path("id") id: Int,
    ): Response<Post>

    @GET("api/posts/{id}/newer")
    suspend fun postsGetNewerPost(@Path("id") id: Int): Response<List<Post>>

    @GET("api/posts/{id}/before")
    suspend fun postsGetBeforePost(
        @Path("id") id: Int,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("api/posts/{id}/after")
    suspend fun postsGetAfterPost(
        @Path("id") id: Int,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("api/posts/{id}")
    suspend fun postsGetPost(
        @Path("id") id: Int,
    ): Response<Post>

    @DELETE("api/posts/{id}")
    suspend fun postsDeletePost(
        @Path("id") id: Int,
    ): Response<Unit>

    @GET("api/posts/latest")
    suspend fun postsGetLatestPage(@Query("count") count: Int): Response<List<Post>>

    //Jobs

    @GET("api/{userId}/jobs")
    suspend fun jobsGetAllJob(
        @Path("userId") userId: Int,
    ): Response<List<Job>>

    //MyJob

    @GET("api/my/jobs")
    suspend fun myJobGetAllJob(): Response<List<Job>>

    @POST("api/my/jobs")
    suspend fun myJobSaveJob(
        @Body job: Job
    ): Response<Job>

    @DELETE("api/my/jobs/{id}")
    suspend fun myJobDeleteJob(
        @Path("id") id: Int,
    ): Response<Unit>

    //Media

    @Multipart
    @POST("api/media")
    suspend fun mediaSaveMedia(
        @Part file: MultipartBody.Part
    ): Response<Media>

    //Events

    @GET("api/events")
    suspend fun eventsGetAllEvent(): Response<List<Event>>

    @POST("api/events")
    suspend fun eventsSaveEvent(
        @Body event: Event
    ): Response<Event>

    @POST("api/events/{id}/participants")
    suspend fun eventsSaveParticipantsEvent(
        @Path("id") id: Int,
    ): Response<Event>

    @DELETE("api/events/{id}/participants")
    suspend fun eventsDeleteParticipantsEvent(
        @Path("id") id: Int,
    ): Response<Event>

    @POST("api/events/{id}/likes")
    suspend fun eventsLikeEvent(
        @Path("id") id: Int,
    ): Response<Event>

    @DELETE("api/events/{id}/likes")
    suspend fun eventsUnLikeEvent(
        @Path("id") id: Int,
    ): Response<Event>

    @GET("api/events/{id}/newer")
    suspend fun eventsGetNewerEvent(
        @Path("id") id: Int,
    ): Response<List<Event>>

    @GET("api/events/{id}/before")
    suspend fun eventsGetBeforeEvent(
        @Path("id") id: Int,
        @Query("count") count: Int
    ): Response<List<Event>>

    @GET("api/events/{id}/after")
    suspend fun eventsGetAfterEvent(
        @Path("id") id: Int,
        @Query("count") count: Int
    ): Response<List<Event>>

    @GET("api/events/{id}")
    suspend fun eventsGetEvent(
        @Path("id") id: Int,
    ): Response<Event>

    @DELETE("api/events/{id}")
    suspend fun eventsDeleteEvent(
        @Path("id") id: Int,
    ): Response<Unit>

    @GET("api/events/latest")
    suspend fun eventsGetLatestPageEvent(@Query("count") count: Int): Response<List<Event>>

}