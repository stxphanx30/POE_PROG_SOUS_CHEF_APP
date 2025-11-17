package com.example.logintemp.data.user

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import at.favre.lib.crypto.bcrypt.BCrypt

/**
 * UserRepository : gère l'auth local (Room) et les interactions éventuelles avec FirebaseAuth.
 *
 * Important :
 *  - Pour synchroniser un utilisateur local vers Firebase (createUserWithEmailAndPassword)
 *    il faut le mot de passe en clair. En offline on sauvegarde l'utilisateur localement
 *    avec pendingSync = true et on attend que l'utilisateur fournisse son mot de passe
 *    pour appeler attemptSyncUserWithPassword.
 */
class UserRepository(
    private val userDao: UserDao,
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private val TAG = "UserRepository"

    /**
     * Register : tente d'abord de créer sur Firebase (si email non-null),
     * si cela échoue (p.ex. hors-ligne), on crée localement en pendingSync = true.
     *
     * Retourne Result<User> (local entity).
     */
    suspend fun register(
        username: String,
        email: String?,
        rawPassword: String,
        firstName: String? = null,
        lastName: String? = null
    ): Result<User> = withContext(Dispatchers.IO) {
        // username unique local check
        if (userDao.getUserByUsername(username) != null) {
            return@withContext Result.failure(Exception("Username already exists"))
        }

        // hash password (bcrypt)
        val hash = BCrypt.withDefaults().hashToString(12, rawPassword.toCharArray())

        // Try to create in Firebase if we have an email (best-effort)
        var firebaseUid: String? = null
        if (!email.isNullOrBlank()) {
            try {
                val authResult = firebaseAuth.createUserWithEmailAndPassword(email, rawPassword).await()
                firebaseUid = authResult.user?.uid
            } catch (e: Exception) {
                Log.w(TAG, "Firebase create failed (will fallback to local): ${e.message}")
                if (e is FirebaseAuthUserCollisionException) {
                    // collision : we'll still create local user (no firebaseUid)
                }
                firebaseUid = null
            }
        }

        // Create local entry (pendingSync = true when firebaseUid == null)
        val user = User(
            username = username,
            firstName = firstName ?: "",
            lastName = lastName ?: "",
            email = email ?: "",
            passwordHash = hash,
            firebaseUid = firebaseUid,
            pendingSync = firebaseUid == null
        )

        val id = userDao.insertUser(user)
        val inserted = userDao.getUserById(id.toInt())!!
        Result.success(inserted)
    }

    /**
     * Login local using username + password (bcrypt check).
     */
    suspend fun loginLocal(username: String, rawPassword: String): Result<User> =
        withContext(Dispatchers.IO) {
            val user = userDao.getUserByUsername(username)
                ?: return@withContext Result.failure(Exception("Invalid credentials"))

            val hash = user.passwordHash
                ?: return@withContext Result.failure(Exception("Invalid credentials"))

            val verify = BCrypt.verifyer().verify(rawPassword.toCharArray(), hash)
            if (!verify.verified) {
                return@withContext Result.failure(Exception("Invalid credentials"))
            }
            Result.success(user)
        }

    /**
     * Login using Firebase email/password. On success, ensure local record exists (insert or update).
     * Returns the local User instance matching firebaseUid.
     */
    suspend fun loginWithFirebaseEmail(email: String, rawPassword: String): Result<User> =
        withContext(Dispatchers.IO) {
            try {
                val authResult = firebaseAuth.signInWithEmailAndPassword(email, rawPassword).await()
                val firebaseUser = authResult.user ?: return@withContext Result.failure(Exception("Firebase login failed"))
                val uid = firebaseUser.uid
                // Try to find local user by firebaseUid
                val existing = userDao.getUserByFirebaseUid(uid)
                if (existing != null) {
                    return@withContext Result.success(existing)
                }
                // If no local user yet, create one (username from email local part)
                val usernameGuess = firebaseUser.email?.substringBefore('@') ?: ("user_${uid.take(6)}")
                // try to infer names from displayName if available
                val display = firebaseUser.displayName
                val (fName, lName) = if (!display.isNullOrBlank()) {
                    val parts = display.split(" ")
                    Pair(parts.firstOrNull() ?: "", parts.drop(1).joinToString(" "))
                } else Pair("", "")

                val newUser = User(
                    username = usernameGuess,
                    firstName = fName,
                    lastName = lName,
                    email = firebaseUser.email ?: "",
                    firebaseUid = uid,
                    pendingSync = false
                )
                val id = userDao.insertUser(newUser)
                val inserted = userDao.getUserById(id.toInt())!!
                Result.success(inserted)
            } catch (e: Exception) {
                Log.w(TAG, "Firebase login failed: ${e.message}")
                Result.failure(e)
            }
        }

    /**
     * Register or update local user info after a Firebase sign-in (Google / other provider).
     * Returns the local User object.
     */
    suspend fun registerOrUpdateFromFirebase(
        firebaseUid: String,
        email: String?,
        username: String?,
        displayName: String? = null
    ): User = withContext(Dispatchers.IO) {
        val existing = userDao.getUserByFirebaseUid(firebaseUid)
        // infer names from displayName if present
        val (firstName, lastName) = if (!displayName.isNullOrBlank()) {
            val parts = displayName.split(" ")
            Pair(parts.firstOrNull() ?: "", parts.drop(1).joinToString(" "))
        } else Pair("", "")

        if (existing != null) {
            val updated = existing.copy(
                email = email ?: existing.email,
                username = username ?: existing.username,
                firstName = if (existing.firstName.isBlank()) firstName else existing.firstName,
                lastName = if (existing.lastName.isBlank()) lastName else existing.lastName,
                pendingSync = false,
                updatedAt = System.currentTimeMillis()
            )
            userDao.updateUser(updated)
            return@withContext updated
        }

        val newUser = User(
            username = username ?: (email ?: "user"),
            firstName = firstName,
            lastName = lastName,
            email = email ?: "",
            firebaseUid = firebaseUid,
            pendingSync = false
        )
        val id = userDao.insertUser(newUser)
        userDao.getUserById(id.toInt())!!
    }

    /**
     * Attempt to synchronize a local user to Firebase, using the provided raw password.
     * Suitable for offline->online flow.
     */
    suspend fun attemptSyncUserWithPassword(userId: Int, rawPassword: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            val user = userDao.getUserById(userId) ?: return@withContext Result.failure(Exception("User not found"))
            val email = user.email
            if (email.isBlank()) {
                return@withContext Result.failure(Exception("User has no email to create Firebase account"))
            }

            return@withContext try {
                val authResult = firebaseAuth.createUserWithEmailAndPassword(email, rawPassword).await()
                val firebaseUid = authResult.user?.uid
                if (firebaseUid != null) {
                    userDao.markSynced(userId, firebaseUid, System.currentTimeMillis())
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Firebase returned null uid"))
                }
            } catch (e: Exception) {
                Log.w(TAG, "attemptSyncUserWithPassword failed: ${e.message}")
                Result.failure(e)
            }
        }

    /**
     * Try to sync all pending users using a provided callback to obtain plaintext passwords when available.
     */
    suspend fun syncPendingUsers(getPasswordForUser: suspend (userId: Int) -> String?) =
        withContext(Dispatchers.IO) {
            val pending = userDao.getPendingSyncUsers()
            pending.forEach { user ->
                val pwd = try { getPasswordForUser(user.id) } catch (t: Throwable) { null }
                if (!pwd.isNullOrEmpty()) {
                    try {
                        val authResult = firebaseAuth.createUserWithEmailAndPassword(user.email, pwd).await()
                        val uid = authResult.user?.uid
                        if (uid != null) {
                            userDao.markSynced(user.id, uid, System.currentTimeMillis())
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "syncPendingUsers: failed for user ${user.id}: ${e.message}")
                    }
                }
            }
        }

    /**
     * Update profile image URI (local only).
     */
    suspend fun updateProfileImage(userId: Int, uri: String?) = withContext(Dispatchers.IO) {
        userDao.updateProfileImage(userId, uri)
    }

    suspend fun getUserById(userId: Int): User? = withContext(Dispatchers.IO) {
        userDao.getUserById(userId)
    }

    suspend fun getProfileImage(userId: Int): String? = withContext(Dispatchers.IO) {
        userDao.getProfileImageUri(userId)
    }
}