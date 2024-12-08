package ru.netology.nework.model

import android.net.Uri
import ru.netology.nework.dto.AttachmentType
import java.io.File

data class AttachmentModel(
    val attachmentType: AttachmentType,
    val uri: Uri,
    val file: File
)
