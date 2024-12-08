package ru.netology.nework.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "postKey")
data class PostRemoteKeyEntity(
    @PrimaryKey
    val type: KeyType,
    val id: Int
)