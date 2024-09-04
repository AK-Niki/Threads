package ru.netology.nmedia.repository

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.netology.nmedia.api.ApiClient
import ru.netology.nmedia.dto.Post
import java.io.IOException

class PostRepositoryImpl : PostRepository {
    private val apiService = ApiClient.apiService

    override fun getAll(callback: PostRepository.GetAllCallback) {
        apiService.getAll().enqueue(object : Callback<List<Post>> {
            override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                if (response.isSuccessful) {
                    callback.onSuccess(response.body().orEmpty())
                } else {
                    callback.onError(IOException("Error getting posts: ${response.code()}"))
                }
            }

            override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                callback.onError(IOException("Network Error", t))
            }
        })
    }

    override fun save(post: Post, callback: PostRepository.SaveCallback) {
        apiService.save(post).enqueue(object : Callback<Post> {
            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                if (response.isSuccessful) {
                    callback.onSuccess(response.body()!!)
                } else {
                    callback.onError(IOException("Error saving post: ${response.code()}"))
                }
            }

            override fun onFailure(call: Call<Post>, t: Throwable) {
                callback.onError(IOException("Network Error", t))
            }
        })
    }

    override fun removeById(id: Long, callback: PostRepository.RemoveCallback) {
        apiService.removeById(id).enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    callback.onSuccess()
                } else {
                    callback.onError(IOException("Error removing post: ${response.code()}"))
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                callback.onError(IOException("Network Error", t))
            }
        })
    }

    override fun likeById(id: Long, likedByMe: Boolean, callback: PostRepository.LikeCallback) {
        val call = if (likedByMe) {
            apiService.unlikeById(id)
        } else {
            apiService.likeById(id)
        }

        call.enqueue(object : Callback<Post> {
            override fun onResponse(call: Call<Post>, response: Response<Post>) {
                if (response.isSuccessful) {
                    callback.onSuccess(response.body()!!)
                } else {
                    callback.onError(IOException("Error liking/unliking post: ${response.code()}"))
                }
            }

            override fun onFailure(call: Call<Post>, t: Throwable) {
                callback.onError(IOException("Network Error", t))
            }
        })
    }
}

