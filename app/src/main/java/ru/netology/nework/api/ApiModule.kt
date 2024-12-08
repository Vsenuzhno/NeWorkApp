package ru.netology.nework.api

import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import ru.netology.nework.BuildConfig.API_KEY
import ru.netology.nework.BuildConfig.BASE_URL
import ru.netology.nework.auth.AppAuth
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ApiModule {

    @Singleton
    @Provides
    fun provideOkHttp(
        appAuth: AppAuth
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            appAuth.authStateFlow.value.token?.let { token ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", token)
                    .addHeader("Api-Key", API_KEY)
                    .build()
                return@addInterceptor chain.proceed(request)
            }
            val request = chain.request().newBuilder()
                .addHeader("Api-Key", API_KEY)
                .build()
            chain.proceed(request)
        }
        .build()

    @Singleton
    @Provides
    fun provideRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit = Retrofit.Builder()
        .addConverterFactory(
            GsonConverterFactory.create(
                GsonBuilder().registerTypeAdapter(
                    OffsetDateTime::class.java,
                    object : TypeAdapter<OffsetDateTime>() {
                        override fun write(out: JsonWriter?, value: OffsetDateTime?) {
                            out?.value(value?.toEpochSecond())
                        }

                        override fun read(jsonReader: JsonReader): OffsetDateTime {
                            return OffsetDateTime.ofInstant(
                                Instant.parse(jsonReader.nextString()),
                                ZoneId.systemDefault()
                            )
                        }
                    }).create()
            )
        )
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .build()

    @Singleton
    @Provides
    fun provideApiService(
        retrofit: Retrofit,
    ): ApiService = retrofit.create()
}
