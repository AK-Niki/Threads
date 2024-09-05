package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.*
import ru.netology.nmedia.util.SingleLiveEvent


class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository = PostRepositoryImpl()
    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data
    val edited = MutableLiveData(Post(
        id = 0L,
        author = "",
        content = "",
        published = "",
        likedByMe = false,
        authorAvatar = ""
    ))
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        loadPosts()
    }

    fun loadPosts() {
        _data.postValue(FeedModel(loading = true))
        repository.getAll(object : PostRepository.GetAllCallback {
            override fun onSuccess(posts: List<Post>) {
                _data.postValue(FeedModel(posts = posts, empty = posts.isEmpty()))
            }

            override fun onError(exception: Exception) {
                _data.postValue(FeedModel(error = true))
                showRetrySnackbar("Ошибка загрузки")
            }
        })
    }

    fun save() {
        edited.value?.let {
            repository.save(it, object : PostRepository.SaveCallback {
                override fun onSuccess(body: Post) {
                    _postCreated.postValue(Unit)
                    loadPosts()
                }

                override fun onError(exception: Exception) {
                    _data.postValue(FeedModel(error = true))
                    showRetrySnackbar("Ошибка создания поста")
                }
            })
        }
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) return
        edited.value = edited.value?.copy(content = text)
    }

    fun likeById(id: Long) {
        val old = _data.value?.posts.orEmpty()
        val likedByMe = old.find { it.id == id }?.likedByMe ?: return
        repository.likeById(id, likedByMe, object : PostRepository.LikeCallback {
            override fun onSuccess(postFromServer: Post) {
                _data.postValue(_data.value?.copy(posts = old.map {
                    if (it.id == id) postFromServer else it
                }))
            }

            override fun onError(exception: Exception) {
                _data.postValue(_data.value?.copy(posts = old))
                showRetrySnackbar("Ошибка обновления лайка", id)
            }
        })
    }

    fun removeById(id: Long) {
        val old = _data.value?.posts.orEmpty()
        _data.postValue(_data.value?.copy(posts = _data.value?.posts.orEmpty()
            .filter { it.id != id }))
        repository.removeById(id, object : PostRepository.RemoveCallback {
            override fun onSuccess() {}

            override fun onError(exception: Exception) {
                _data.postValue(_data.value?.copy(posts = old))
                showRetrySnackbar("Ошибка - не удалось удалить", id)
            }
        })
    }


    private val _retrySnackbar = SingleLiveEvent<Pair<String, Long?>>()
    val retrySnackbar: LiveData<Pair<String, Long?>> = _retrySnackbar

    fun showRetrySnackbar(message: String, postId: Long? = null) {
        _retrySnackbar.postValue(message to postId)
    }

}
