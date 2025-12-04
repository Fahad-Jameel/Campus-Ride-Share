package com.example.campusride.data.repository

import android.content.Context
import com.example.campusride.data.api.ApiService
import com.example.campusride.data.api.RetrofitClient
import com.example.campusride.data.api.*
import com.example.campusride.data.database.CampusRideDatabase
import com.example.campusride.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class UserRepository(context: Context) {
    private val apiService: ApiService = RetrofitClient.apiService
    private val db = CampusRideDatabase.getDatabase(context)
    private val userDao = db.userDao()
    
    suspend fun register(email: String, password: String, name: String, phone: String?, affiliation: String?): Result<User> {
        return try {
            val response = apiService.register(RegisterRequest(email, password, name, phone, affiliation))
            if (response.isSuccessful && response.body()?.success == true) {
                val userResponse = response.body()?.user
                if (userResponse != null) {
                    val user = userResponse.toUser()
                    userDao.insertUser(user)
                    Result.success(user)
                } else {
                    Result.failure(Exception("Registration failed: No user data"))
                }
            } else {
                Result.failure(Exception(response.body()?.error ?: "Registration failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body()?.success == true) {
                val userResponse = response.body()?.user
                if (userResponse != null) {
                    val user = userResponse.toUser()
                    userDao.insertUser(user)
                    Result.success(user)
                } else {
                    Result.failure(Exception("Login failed: No user data"))
                }
            } else {
                Result.failure(Exception(response.body()?.error ?: "Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUserById(userId: String): Flow<User?> = userDao.getUserById(userId)
    
    suspend fun syncUserFromServer(userId: String): Result<User> {
        return try {
            val response = apiService.getUser(userId)
            if (response.isSuccessful && response.body()?.success == true) {
                val userResponse = response.body()?.user
                if (userResponse != null) {
                    val user = userResponse.toUser().copy(lastSyncedAt = System.currentTimeMillis())
                    userDao.insertUser(user)
                    Result.success(user)
                } else {
                    Result.failure(Exception("User not found"))
                }
            } else {
                Result.failure(Exception(response.body()?.error ?: "Failed to fetch user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateUser(user: User): Result<User> {
        return try {
            val response = apiService.updateUser(UpdateUserRequest(
                id = user.id,
                name = user.name,
                phone = user.phone,
                affiliation = user.affiliation,
                profileImageUrl = user.profileImageUrl
            ))
            if (response.isSuccessful && response.body()?.success == true) {
                val userResponse = response.body()?.user
                if (userResponse != null) {
                    val updatedUser = userResponse.toUser().copy(lastSyncedAt = System.currentTimeMillis())
                    userDao.insertUser(updatedUser)
                    Result.success(updatedUser)
                } else {
                    Result.failure(Exception("Update failed"))
                }
            } else {
                Result.failure(Exception(response.body()?.error ?: "Update failed"))
            }
        } catch (e: Exception) {
            // Save locally even if sync fails
            userDao.insertUser(user)
            Result.failure(e)
        }
    }
    
    private fun UserResponse.toUser(): User {
        return User(
            id = this.id,
            email = this.email,
            name = this.name,
            phone = this.phone,
            profileImageUrl = this.profileImageUrl,
            affiliation = this.affiliation,
            verified = this.verified,
            createdAt = this.createdAt
        )
    }
}

