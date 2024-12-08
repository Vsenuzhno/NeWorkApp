package ru.netology.nework.entity

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.Coords
import ru.netology.nework.dto.UserPreview

class CoordsConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromCoords(coords: Coords?): String? {
        return if (coords != null) gson.toJson(coords) else null
    }

    @TypeConverter
    fun toCoords(coords: String?): Coords? {
        return if (coords != null) gson.fromJson(coords, Coords::class.java) else null
    }
}

class MentionIdsConverter {
    private val gson = Gson()
    private val typeToken = object : TypeToken<List<Int>>() {}.type

    @TypeConverter
    fun fromMentionIds(mentionIds: List<Int>?): String? {
        return if (mentionIds != null) gson.toJson(mentionIds) else null
    }

    @TypeConverter
    fun toMentionIds(mentionIds: String?): List<Int>? {
        return if (mentionIds != null) gson.fromJson(mentionIds, typeToken) else null
    }

}

class AttachmentConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromAttachment(attachment: Attachment?): String? {
        return if (attachment != null) gson.toJson(attachment) else null
    }

    @TypeConverter
    fun toAttachment(attachment: String?): Attachment? {
        return if (attachment != null) gson.fromJson(
            attachment,
            Attachment::class.java
        ) else null
    }
}

class UsersConverter {
    private val gson = Gson()
    private val typeToken = object : TypeToken<Map<String, UserPreview>>() {}.type

    @TypeConverter
    fun fromAttachment(users: Map<String, UserPreview>?): String? {
        return if (users != null) gson.toJson(users) else null
    }

    @TypeConverter
    fun toAttachment(users: String?): Map<String, UserPreview>? {
        return if (users != null) gson.fromJson(users, typeToken) else null
    }
}