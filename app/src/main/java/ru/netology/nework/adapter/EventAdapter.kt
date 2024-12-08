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
import ru.netology.nework.databinding.CardEventBinding
import ru.netology.nework.dto.AttachmentType
import ru.netology.nework.dto.DiffCallback
import ru.netology.nework.dto.Event
import ru.netology.nework.dto.EventType
import ru.netology.nework.dto.FeedItem
import ru.netology.nework.util.loadAvatar
import ru.netology.nework.util.loadImage
import java.time.format.DateTimeFormatter

interface OnEventInteractionListener {
    fun onLikeEvent(event: Event)
    fun onRemoveEvent(event: Event)
    fun onEditEvent(event: Event)
    fun onCardEvent(event: Event)
    fun onShareEvent(event: Event)
}

class EventAdapter(
    private val onEventInteractionListener: OnEventInteractionListener,
) : PagingDataAdapter<FeedItem, EventViewHolder>(DiffCallback()) {

    override fun onViewRecycled(holder: EventViewHolder) {
        super.onViewRecycled(holder)
        holder.releasePlayer()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding =
            CardEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding, onEventInteractionListener, parent.context)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val item = getItem(position) as Event
        holder.bind(item)
    }
}

class EventViewHolder(
    private val binding: CardEventBinding,
    private val onEventInteractionListener: OnEventInteractionListener,
    private val context: Context,
) : RecyclerView.ViewHolder(binding.root) {

    private var player: ExoPlayer? = null

    fun bind(event: Event) {
        with(binding) {
            authorAvatar.loadAvatar(event.authorAvatar)
            authorName.text = event.author
            datePublication.text =
                event.published.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
            textContent.text = event.content
            eventType.text = event.type.toString()
            eventDate.text = event.datetime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
            buttonLike.text = event.likeOwnerIds.size.toString()
            buttonLike.isChecked = event.likedByMe

            buttonPlayEvent.isVisible = event.type == EventType.ONLINE
            buttonOption.isVisible = event.ownedByMe

            fun setAttachmentVisibility(
                imageContentVisible: Boolean = false,
                videoContentVisible: Boolean = false,
            ) {
                imageContent.isVisible = imageContentVisible
                videoContent.isVisible = videoContentVisible
            }

            when (event.attachment?.type) {
                AttachmentType.IMAGE -> {
                    imageContent.loadImage(event.attachment.url)
                    setAttachmentVisibility(imageContentVisible = true)
                }

                AttachmentType.VIDEO -> {
                    player = ExoPlayer.Builder(context).build().apply {
                        setMediaItem(MediaItem.fromUri(event.attachment.url))
                    }
                    videoContent.player = player
                    setAttachmentVisibility(videoContentVisible = true)
                }

                null -> {
                    releasePlayer()
                    setAttachmentVisibility()
                }
            }

            buttonUsers.text = event.speakerIds.size.toString()

            buttonOption.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.options_content_menu)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.delete -> {
                                onEventInteractionListener.onRemoveEvent(event)
                                true
                            }

                            R.id.edit -> {
                                onEventInteractionListener.onEditEvent(event)
                                true
                            }

                            else -> false
                        }
                    }
                    gravity = Gravity.END
                }
                    .show()
            }

            buttonLike.setOnClickListener {
                onEventInteractionListener.onLikeEvent(event)
            }

            buttonShare.setOnClickListener {
                onEventInteractionListener.onShareEvent(event)
            }

            cardEvent.setOnClickListener {
                onEventInteractionListener.onCardEvent(event)
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