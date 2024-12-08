package ru.netology.nework.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "eventKey")
data class EventRemoteKeyEntity(
    @PrimaryKey
    val type: KeyType,
    val id: Int
)
