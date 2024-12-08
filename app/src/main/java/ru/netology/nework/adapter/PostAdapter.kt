package ru.netology.nework.adapter

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.R
import ru.netology.nework.databinding.CardPostBinding
import ru.netology.nework.dto.AttachmentType
import ru.netology.nework.dto.DiffCallback
import ru.netology.nework.dto.FeedItem
import ru.netology.nework.dto.Post
import ru.netology.nework.util.loadAvatar
import ru.netology.nework.util.loadImage
import java.time.format.DateTimeFormatter

interface OnPostInteractionListener {
    fun onLikePost(post: Post)
    fun onRemovePost(post: Post)
    fun onEditPost(post: Post)
    fun onCardPost(post: Post)
    fun onSharePost(post: Post)
}

class PostAdapter(
    private val onPostInteractionListener: OnPostInteractionListener,
) : PagingDataAdapter<FeedItem, PostViewHolder>(DiffCallback()) {

    override fun onViewRecycled(holder: PostViewHolder) {
        super.onViewRecycled(holder)
        holder.releasePlayer()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding =
            CardPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onPostInteractionListener, parent.context)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val item = getItem(position) as Post
        holder.bind(item)
    }
}

class PostViewHolder(
    private val binding: CardPostBinding,
    private val onPostInteractionListener: OnPostInteractionListener,
    private val context: Context,
) : RecyclerView.ViewHolder(binding.root) {

    private var player: ExoPlayer? = null

    fun bind(post: Post) {
        with(binding) {
            authorAvatar.loadAvatar(post.authorAvatar)
            authorName.text = post.author
            datePublication.text =
                post.published.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
            textContent.text = post.content
            buttonLike.text = post.likeOwnerIds.size.toString()
            buttonLike.isChecked = post.likedByMe

            fun setAttachmentVisibility(
                imageContentVisible: Boolean = false,
                videoContentVisible: Boolean = false,
            ) {
                imageContent.isVisible = imageContentVisible
                videoContent.isVisible = videoContentVisible
            }

            when (post.attachment?.type) {
                AttachmentType.IMAGE -> {
                    imageContent.loadImage(post.attachment.url)
                    setAttachmentVisibility(imageContentVisible = true)
                }

                AttachmentType.VIDEO -> {
                    player = ExoPlayer.Builder(context).build().apply {
                        setMediaItem(MediaItem.fromUri(post.attachment.url))
                    }
                    videoContent.player = player
                    setAttachmentVisibility(videoContentVisible = true)
                }

                null -> {
                    releasePlayer()
                    setAttachmentVisibility()
                }
            }

            buttonLike.setOnClickListener {
                onPostInteractionListener.onLikePost(post)
            }

            buttonShare.setOnClickListener {
                onPostInteractionListener.onSharePost(post)
            }

            buttonOption.isVisible = post.ownedByMe
            buttonOption.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_content_menu)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.delete -> {
                                onPostInteractionListener.onRemovePost(post)
                                true
                            }

                            R.id.edit -> {
                                onPostInteractionListener.onEditPost(post)
                                true
                            }

                            else -> false
                        }
                    }
                    gravity = Gravity.END
                }
                    .show()
            }

            binding.cardPost.setOnClickListener {
                onPostInteractionListener.onCardPost(post)
            }
        }
    }

    fun releasePlayer() {
        player?.apply {
            stop()
            release()
        }
    }

    fun stopPlayer() {
        player?.stop()
    }
}