package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val sex: String,
    val age: Int,
    val address: String,
    val email: String,
    val phone: String,
    val password: String,
    val role: String // "CLIENT", "PHYSICIAN", "MANAGER_ADMIN"
) : Serializable {
    val fullName: String get() = "$firstName $middleName $lastName".trim()
}

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userName: String,
    val userPhone: String,
    val physicianName: String,
    val date: String,
    val time: String,
    val reason: String,
    val status: String, // "PENDING", "APPROVED", "REJECTED"
    val treatmentReport: String = "",
    val prescription: String = ""
) : Serializable

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderName: String,
    val senderRole: String, // "CLIENT", "PHYSICIAN", "AI"
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isAi: Boolean = false
) : Serializable

@Entity(tableName = "resources")
data class ResourceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // e.g. "Contraception & Family Planning", "Maternal Care", "STIs & HIV", "Adolescent Health"
    val content: String,
    val videoUrl: String = "",
    val author: String = "IRHC Health Ministry"
) : Serializable
