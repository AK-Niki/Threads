package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import ru.netology.nmedia.dto.Post

interface PostRepository {
    fun getAll(callback: GetAllCallback)
    fun likeById(id: Long, callback: LikeCallback)
    fun save(post: Post, callback: SaveCallback)
    fun removeById(id: Long, callback: RemoveCallback)

    interface GetAllCallback {
        fun onSuccess(posts: List<Post>)
        fun onError(exception: Exception)
    }

    interface LikeCallback {
        fun onSuccess()
        fun onError(exception: Exception)
    }

    interface SaveCallback {
        fun onSuccess()
        fun onError(exception: Exception)
    }

    interface RemoveCallback {
        fun onSuccess()
        fun onError(exception: Exception)
    }
}

