package ru.netology.nework.activity

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.paging.filter
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import ru.netology.nework.R
import ru.netology.nework.adapter.OnPostInteractionListener
import ru.netology.nework.adapter.PostAdapter
import ru.netology.nework.adapter.PostViewHolder
import ru.netology.nework.auth.AuthState
import ru.netology.nework.databinding.FragmentPostsBinding
import ru.netology.nework.dto.Post
import ru.netology.nework.util.AppConst
import ru.netology.nework.util.StringArg
import ru.netology.nework.viewmodel.AuthViewModel
import ru.netology.nework.viewmodel.PostViewModel

@AndroidEntryPoint
class PostsFragment : Fragment() {
    private lateinit var binding: FragmentPostsBinding
    private val authViewModel: AuthViewModel by activityViewModels()
    private val postViewModel: PostViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentPostsBinding.inflate(inflater, container, false)
        val parentNavController = parentFragment?.parentFragment?.findNavController()

        var token: AuthState? = null
        authViewModel.dataAuth.observe(viewLifecycleOwner) { state ->
            token = state
        }

        val userId = arguments?.getInt(AppConst.USER_ID)

        val postAdapter = PostAdapter(object : OnPostInteractionListener {
            override fun onLikePost(post: Post) {
                if (token?.id != 0 && token?.id.toString().isNotEmpty()) {
                    postViewModel.likePost(post)
                } else {
                    parentNavController?.navigate(R.id.loginFragment)
                }
            }

            override fun onRemovePost(post: Post) {
                postViewModel.removePost(post)
            }

            override fun onEditPost(post: Post) {
                postViewModel.editPost(post)
                parentNavController?.navigate(
                    R.id.newPostFragment,
                    bundleOf("editPost" to post.content)
                )
            }

            override fun onCardPost(post: Post) {
                postViewModel.openPost(post)
                parentNavController?.navigate(R.id.detailPostFragment)
            }

            override fun onSharePost(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }
                val shareIntent =
                    Intent.createChooser(intent, getString(R.string.share))
                startActivity(shareIntent)
            }
        })

        binding.listPosts.adapter = postAdapter
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                postViewModel.data.collectLatest {
                    if (userId != null) {
                        postAdapter.submitData(it.filter { post ->
                            post is Post && post.authorId == userId
                        })
                    } else {
                        postAdapter.submitData(it)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            postAdapter.loadStateFlow.collectLatest {
                binding.swipeRefreshPosts.isRefreshing =
                    it.refresh is LoadState.Loading
                if (it.append is LoadState.Error
                    || it.prepend is LoadState.Error
                    || it.refresh is LoadState.Error
                ) {
                    Snackbar.make(
                        binding.root,
                        R.string.connection_error,
                        Snackbar.LENGTH_LONG
                    ).setAnchorView(R.id.button_new_post)
                        .show()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                suspendCancellableCoroutine {
                    it.invokeOnCancellation {
                        (0..<binding.listPosts.childCount)
                            .map(binding.listPosts::getChildAt)
                            .map(binding.listPosts::getChildViewHolder)
                            .filterIsInstance<PostViewHolder>()
                            .onEach(PostViewHolder::stopPlayer)
                    }
                }
            }
        }

        binding.swipeRefreshPosts.setOnRefreshListener {
            postAdapter.refresh()
        }

        binding.buttonNewPost.isVisible = userId == null
        binding.buttonNewPost.setOnClickListener {
            if (token?.id != 0 && token?.id.toString().isNotEmpty()) {
                parentNavController?.navigate(R.id.action_mainFragment_to_newPostFragment)
            } else {
                parentNavController?.navigate(R.id.action_mainFragment_to_loginFragment)
            }
        }

        return binding.root
    }

    companion object {
        var Bundle.textArg by StringArg
    }
}
