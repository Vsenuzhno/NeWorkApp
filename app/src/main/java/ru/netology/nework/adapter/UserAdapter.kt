package ru.netology.nework.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.databinding.CardUserBinding
import ru.netology.nework.dto.DiffCallback
import ru.netology.nework.dto.FeedItem
import ru.netology.nework.dto.UserResponse
import ru.netology.nework.util.loadAvatar

interface OnUserInteractionListener {
    fun onCardUser(userResponse: UserResponse)
    fun onSelectUser(userResponse: UserResponse)
}

class UserAdapter(
    private val onUserInteractionListener: OnUserInteractionListener,
    private val selectUser: Boolean,
    private val selectedUsers: List<Int>? = null,

    ) : PagingDataAdapter<FeedItem, UserViewHolder>(DiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = CardUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding, onUserInteractionListener, selectUser)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val item = getItem(position) as UserResponse
        holder.bind(if (selectedUsers?.firstOrNull { it == item.id } == null) item else item.copy(
            selected = true
        ))
    }
}

class UserViewHolder(
    private val binding: CardUserBinding,
    private val onUserInteractionListener: OnUserInteractionListener,
    private val selectUser: Boolean,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(user: UserResponse) {
        with(binding) {
            authorName.text = user.name
            authorLogin.text = user.login

            authorAvatar.loadAvatar(user.avatar)

            checkBox.isVisible = selectUser
            checkBox.isChecked = user.selected
            checkBox.setOnClickListener {
                onUserInteractionListener.onSelectUser(user)
            }

            cardUser.setOnClickListener {
                onUserInteractionListener.onCardUser(user)
            }
        }
    }
}