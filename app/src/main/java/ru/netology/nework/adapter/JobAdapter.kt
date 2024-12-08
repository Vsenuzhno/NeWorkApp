package ru.netology.nework.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nework.R
import ru.netology.nework.databinding.CardJobBinding
import ru.netology.nework.dto.Job
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

interface OnJobInteractionListener {
    fun onRemoveJob(job: Job)
}

class JobAdapter(private val listener: OnJobInteractionListener) :
    ListAdapter<Job, JobViewHolder>(JobDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobViewHolder {
        val binding = CardJobBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(
            binding,
            listener,
            parent.context
        )
    }

    override fun onBindViewHolder(holder: JobViewHolder, position: Int) {
        getItem(position).let {
            holder.bind(it)
        }
    }
}

class JobViewHolder(
    private val binding: CardJobBinding,
    private val listener: OnJobInteractionListener,
    private val context: Context,
) : RecyclerView.ViewHolder(binding.root) {
    private val emptyOffsetDateTime = OffsetDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC)

    fun bind(job: Job) {
        binding.apply {
            jobName.text = job.name
            jobPosition.text = job.position
            jobLink.text = job.link

            jobStartEnd.text = buildString {
                append(job.start.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
                append(" - ")
                append(
                    if (job.finish.year == emptyOffsetDateTime.year) context.getString(R.string.present_time) else job.finish.format(
                        DateTimeFormatter.ofPattern("dd.MM.yyyy")
                    )
                )
            }
            jobLink.isVisible = job.link != null && job.link != ""
            buttonRemoveJob.isVisible = job.ownedByMe
            buttonRemoveJob.setOnClickListener { listener.onRemoveJob(job) }
        }
    }
}

class JobDiffCallback : DiffUtil.ItemCallback<Job>() {
    override fun areItemsTheSame(oldItem: Job, newItem: Job): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Job, newItem: Job): Boolean = oldItem == newItem
}