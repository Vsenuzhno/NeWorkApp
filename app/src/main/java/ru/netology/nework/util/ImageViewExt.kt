package ru.netology.nework.util

import android.view.LayoutInflater
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.android.material.progressindicator.CircularProgressIndicator
import ru.netology.nework.R

fun ImageView.loadAvatar(url: String?) {
    val inflater = LayoutInflater.from(context)
    val progressView = inflater.inflate(R.layout.progress_indicator, null)
    val drawable =
        progressView.findViewById<CircularProgressIndicator>(R.id.progressIndicator).indeterminateDrawable

    Glide.with(this)
        .load(url)
        .placeholder(drawable)
        .error(R.drawable.ic_account_circle_24)
        .timeout(10_000)
        .circleCrop()
        .into(this)
}

fun ImageView.loadImage(url: String?) {
    val inflater = LayoutInflater.from(context)
    val progressView = inflater.inflate(R.layout.progress_indicator, null)
    val drawable =
        progressView.findViewById<CircularProgressIndicator>(R.id.progressIndicator).indeterminateDrawable

    Glide.with(this)
        .load(url)
        .placeholder(drawable)
        .error(R.drawable.ic_error_100dp)
        .timeout(10_000)
        .centerCrop()
        .into(this)
}