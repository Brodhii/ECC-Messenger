package network

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

// FIX: ApiService.kt sebelumnya kosong — definisi endpoint API
interface ApiService {

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<ResponseBody>

    @FormUrlEncoded
    @POST("register")
    suspend fun register(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<ResponseBody>

    @FormUrlEncoded
    @POST("messages/send")
    suspend fun sendMessage(
        @Field("content") content: String
    ): Response<ResponseBody>

    @POST("messages")
    suspend fun getMessages(): Response<ResponseBody>
}
