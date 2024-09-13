package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.*
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.SingleLiveEvent

private val emptyPost = Post(
    id = 0,
    authorId = 0,
    author = "",
    content = "",
    published = 0L,
    likedByMe = false,
    likes = 0,
    attachment = null
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository = PostRepositoryImpl()

    private val _data = MutableLiveData(FeedModel())
    val data: LiveData<FeedModel>
        get() = _data

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val _edited = MutableLiveData(emptyPost)
    val edited: LiveData<Post> get() = _edited

    private val _retrySnackbar = SingleLiveEvent<Pair<String, Long?>>()
    val retrySnackbar: LiveData<Pair<String, Long?>>
        get() = _retrySnackbar

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
                _retrySnackbar.postValue("Ошибка загрузки постов, попробуйте снова" to null)
            }
        })
    }

    fun likeById(id: Long) {
        val old = _data.value?.posts.orEmpty()
        val likedByMe = old.find { it.id == id }?.likedByMe ?: return

        repository.likeById(id, likedByMe, object : PostRepository.LikeCallback {
            override fun onSuccess(postFromServer: Post) {
                _data.postValue(
                    _data.value?.copy(
                        posts = old.map {
                            if (it.id == id) postFromServer else it
                        }
                    )
                )
            }

            override fun onError(exception: Exception) {
                _data.postValue(_data.value?.copy(posts = old))
                _retrySnackbar.postValue("Ошибка лайка, попробуйте снова" to id)
            }
        })
    }

    fun save() {
        val post = _edited.value ?: return
        repository.save(post, object : PostRepository.SaveCallback {
            override fun onSuccess() {
                _postCreated.postValue(Unit)
                loadPosts()
            }

            override fun onError(exception: Exception) {
                _retrySnackbar.postValue("Ошибка сохранения поста, попробуйте снова" to null)
            }
        })
    }

    fun edit(post: Post) {
        _edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (_edited.value?.content == text) return
        _edited.value = _edited.value?.copy(content = text)
    }

    fun removeById(id: Long) {
        val old = _data.value?.posts.orEmpty()
        _data.postValue(_data.value?.copy(posts = old.filter { it.id != id }))

        repository.removeById(id, object : PostRepository.RemoveCallback {
            override fun onSuccess() {
            }

            override fun onError(exception: Exception) {
                _data.postValue(_data.value?.copy(posts = old))
                _retrySnackbar.postValue("Ошибка удаления поста, попробуйте снова" to id)
            }
        })
    }
}
