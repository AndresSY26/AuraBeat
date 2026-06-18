package com.example.sync

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import retrofit2.Response
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

interface FirestoreApi {
    @GET("v1/projects/{projectId}/databases/(default)/documents/sync_playlists/{syncCode}")
    suspend fun getDocument(
        @Path("projectId") projectId: String,
        @Path("syncCode") syncCode: String,
        @Query("key") apiKey: String? = null
    ): Response<FirestoreDocument>

    @PATCH("v1/projects/{projectId}/databases/(default)/documents/sync_playlists/{syncCode}")
    suspend fun updateDocument(
        @Path("projectId") projectId: String,
        @Path("syncCode") syncCode: String,
        @Body document: FirestoreDocument,
        @Query("key") apiKey: String? = null
    ): Response<FirestoreDocument>
}

object FirestoreClient {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    val api: FirestoreApi = Retrofit.Builder()
        .baseUrl("https://firestore.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(FirestoreApi::class.java)
}
