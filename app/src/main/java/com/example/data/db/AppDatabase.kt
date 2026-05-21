package com.example.data.db

import android.content.Context
import androidx.room.*
import com.example.data.model.UserEntity
import com.example.data.model.AppointmentEntity
import com.example.data.model.MessageEntity
import com.example.data.model.ResourceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE phone = :phone AND password = :password LIMIT 1")
    suspend fun getUserByCredentials(phone: String, password: String): UserEntity?

    @Query("SELECT * FROM users WHERE role = 'PHYSICIAN'")
    suspend fun getPhysicians(): List<UserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUser(id: Int)
}

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments ORDER BY id DESC")
    fun getAllAppointmentsFlow(): Flow<List<AppointmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: AppointmentEntity): Long

    @Update
    suspend fun updateAppointment(appointment: AppointmentEntity)

    @Query("UPDATE appointments SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Int, status: String)

    @Query("UPDATE appointments SET treatmentReport = :report, prescription = :prescription, status = 'COMPLETED' WHERE id = :id")
    suspend fun addConsultationReport(id: Int, report: String, prescription: String)

    @Query("DELETE FROM appointments WHERE id = :id")
    suspend fun deleteAppointment(id: Int)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    fun getAllMessagesFlow(): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Query("DELETE FROM messages")
    suspend fun clearChat()
}

@Dao
interface ResourceDao {
    @Query("SELECT * FROM resources ORDER BY id DESC")
    fun getAllResourcesFlow(): Flow<List<ResourceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResource(resource: ResourceEntity): Long

    @Query("DELETE FROM resources WHERE id = :id")
    suspend fun deleteResource(id: Int)
}

@Database(
    entities = [
        UserEntity::class,
        AppointmentEntity::class,
        MessageEntity::class,
        ResourceEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract val userDao: UserDao
    abstract val appointmentDao: AppointmentDao
    abstract val messageDao: MessageDao
    abstract val resourceDao: ResourceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "irhc_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
