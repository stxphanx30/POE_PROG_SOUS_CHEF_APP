package com.example.logintemp.data.user

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Int): User?

    @Query("SELECT * FROM users WHERE firebase_uid = :firebaseUid LIMIT 1")
    suspend fun getUserByFirebaseUid(firebaseUid: String): User?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    @Query("UPDATE users SET profile_image_uri = :uri WHERE id = :userId")
    suspend fun updateProfileImage(userId: Int, uri: String?)

    @Query("SELECT profile_image_uri FROM users WHERE id = :userId")
    suspend fun getProfileImageUri(userId: Int): String?

    @Query("SELECT * FROM users ORDER BY created_at DESC")
    fun getAllFlow(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE pending_sync = 1")
    suspend fun getPendingSyncUsers(): List<User>

    @Query("""
        UPDATE users 
        SET pending_sync = 0, 
            firebase_uid = :firebaseUid, 
            updated_at = :updatedAt 
        WHERE id = :id
    """)
    suspend fun markSynced(
        id: Int,
        firebaseUid: String?,
        updatedAt: Long = System.currentTimeMillis()
    )
}