package ru.netology.nmedia.repository

import com.google.gson.Gson
import okhttp3.*
import ru.netology.nmedia.dto.Post
import java.io.IOException
import java.util.concurrent.TimeUnit
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class PostRepositoryImpl : PostRepository {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()
    private val typeToken = object : com.google.gson.reflect.TypeToken<List<Post>>() {}

    companion object {
        private const val BASE_URL = "http://10.0.2.2:9999"
        private val jsonType = "application/json".toMediaType()
    }

    override fun getAll(callback: PostRepository.GetAllCallback) {
        val request: Request = Request.Builder()
            .url("$BASE_URL/api/posts")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val posts: List<Post> =
                    response.let { it.body?.string() ?: throw RuntimeException("body is null") }
                        .let { gson.fromJson(it, typeToken.type) }
                callback.onSuccess(posts)
            }

            override fun onFailure(call: Call, e: IOException) {
                callback.onError(e)
            }
        })
    }

    override fun likeById(id: Long, likedByMe: Boolean, callback: PostRepository.LikeCallback) {
        val request: Request = if (likedByMe) {
            Request.Builder()
                .delete()
                .url("$BASE_URL/api/posts/$id/likes")
                .build()
        } else {
            Request.Builder()
                .post("".toRequestBody())
                .url("$BASE_URL/api/posts/$id/likes")
                .build()
        }

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    callback.onError(IOException("Unexpected code ${response.code}"))
                } else {
                    response.let { it.body?.string() }
                        ?.let { callback.onSuccess(gson.fromJson(it, Post::class.java)) }
                        ?: callback.onError(RuntimeException("body is null"))
                }
            }
        })
    }

    override fun save(post: Post, callback: PostRepository.SaveCallback) {
        val request: Request = Request.Builder()
            .post(gson.toJson(post).toRequestBody(jsonType))
            .url("$BASE_URL/api/posts")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    callback.onError(IOException("Unexpected code ${response.code}"))
                } else {
                    callback.onSuccess()
                }
            }
        })
    }

    override fun removeById(id: Long, callback: PostRepository.RemoveCallback) {
        val request: Request = Request.Builder()
            .delete()
            .url("$BASE_URL/api/posts/$id")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    callback.onError(IOException("Unexpected code ${response.code}"))
                } else {
                    callback.onSuccess()
                }
            }
        })
    }
}

