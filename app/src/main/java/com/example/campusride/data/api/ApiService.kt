package com.example.campusride.data.api

import com.example.campusride.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    // Authentication
    @POST("auth/register.php")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<UserResponse>>
    
    @POST("auth/login.php")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<UserResponse>>
    
    // Users
    @GET("users/get_user.php")
    suspend fun getUser(@Query("id") userId: String): Response<ApiResponse<UserResponse>>
    
    @PUT("users/update_user.php")
    suspend fun updateUser(@Body user: UpdateUserRequest): Response<ApiResponse<UserResponse>>
    
    // Rides
    @GET("rides/get_rides.php")
    suspend fun getRides(
        @Query("pickup") pickup: String? = null,
        @Query("destination") destination: String? = null,
        @Query("date") date: String? = null,
        @Query("search") search: String? = null
    ): Response<RidesApiResponse>
    
    @POST("rides/create_ride.php")
    suspend fun createRide(@Body ride: CreateRideRequest): Response<ApiResponse<RideResponse>>
    
    // Vehicles
    @GET("vehicles/get_vehicles.php")
    suspend fun getVehicles(@Query("userId") userId: String): Response<VehiclesApiResponse>
    
    @POST("vehicles/create_vehicle.php")
    suspend fun createVehicle(@Body vehicle: CreateVehicleRequest): Response<ApiResponse<VehicleResponse>>
    
    @PUT("vehicles/update_vehicle.php")
    suspend fun updateVehicle(@Body vehicle: UpdateVehicleRequest): Response<ApiResponse<VehicleResponse>>
    
    // Images
    @Multipart
    @POST("images/upload.php")
    suspend fun uploadImage(
        @Part("type") type: RequestBody,
        @Part image: MultipartBody.Part
    ): Response<ApiResponse<ImageUploadResponse>>
    
    // Chats
    @GET("chats/get_chats.php")
    suspend fun getChats(@Query("userId") userId: String): Response<ChatsApiResponse>
    
    @POST("chats/create_chat.php")
    suspend fun createChat(@Body data: Map<String, String>): Response<ApiResponse<ChatResponse>>
    
    @POST("chats/delete_chat.php")
    suspend fun deleteChat(@Body data: Map<String, String>): Response<ApiResponse<Any>>
    
    // Messages
    @GET("messages/get_messages.php")
    suspend fun getMessages(@Query("chatId") chatId: String): Response<MessagesApiResponse>
    
    @POST("messages/send_message.php")
    suspend fun sendMessage(@Body message: SendMessageRequest): Response<ApiResponse<MessageResponse>>
    
    @POST("messages/update_message.php")
    suspend fun updateMessage(@Body data: Map<String, String>): Response<ApiResponse<MessageResponse>>
    
    @POST("messages/delete_message.php")
    suspend fun deleteMessage(@Body data: Map<String, String>): Response<ApiResponse<Any>>
    
    // Bookings
    @POST("bookings/create_booking.php")
    suspend fun createBooking(@Body bookingData: Map<String, String>): Response<Map<String, Any>>

    @GET("bookings/get_bookings.php")
    suspend fun getBookingsByPassenger(@Query("passenger_id") passengerId: String): Response<List<Map<String, Any>>>

    @GET("bookings/get_bookings.php")
    suspend fun getBookingsByDriver(@Query("driver_id") driverId: String): Response<List<Map<String, Any>>>

    @POST("bookings/update_booking.php")
    suspend fun updateBookingStatus(@Query("id") bookingId: String, @Body data: Map<String, String>): Response<Map<String, Any>>
    
    // FCM Token
    @POST("users/save_fcm_token.php")
    suspend fun saveFCMToken(@Body data: Map<String, String>): Response<Map<String, Any>>
    
    // Notifications
    @GET("notifications/get_notifications.php")
    suspend fun getNotifications(@Query("userId") userId: String): Response<NotificationsApiResponse>
    
    @POST("notifications/mark_read.php")
    suspend fun markNotificationRead(@Body data: Map<String, String>): Response<Map<String, Any>>
}

// Request/Response Models
data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String,
    val phone: String? = null,
    val affiliation: String? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class UpdateUserRequest(
    val id: String,
    val name: String? = null,
    val phone: String? = null,
    val affiliation: String? = null,
    val profileImageUrl: String? = null
)

data class CreateRideRequest(
    val driverId: String,
    val pickupLocation: String,
    val destination: String,
    val date: String,
    val time: String,
    val availableSeats: Int,
    val totalSeats: Int,
    val cost: String,
    val vehicleId: String? = null,
    val vehicleModel: String? = null,
    val preferences: List<String> = emptyList()
)

data class CreateVehicleRequest(
    val userId: String,
    val make: String,
    val model: String,
    val year: Int,
    val color: String,
    val licensePlate: String,
    val imageUrl: String? = null
)

data class UpdateVehicleRequest(
    val id: String,
    val make: String? = null,
    val model: String? = null,
    val year: Int? = null,
    val color: String? = null,
    val licensePlate: String? = null,
    val imageUrl: String? = null
)

data class SendMessageRequest(
    val chatId: String,
    val senderId: String,
    val receiverId: String,
    val text: String,
    val imageUrl: String? = null
)

data class ApiResponse<T>(
    val success: Boolean,
    val error: String? = null,
    val user: T? = null,
    val ride: T? = null,
    val vehicle: T? = null,
    val message: T? = null,
    val imageUrl: String? = null,
    val filename: String? = null
)

data class RidesApiResponse(
    val success: Boolean,
    val error: String? = null,
    val rides: List<RideResponse>? = null
)

data class VehiclesApiResponse(
    val success: Boolean,
    val error: String? = null,
    val vehicles: List<VehicleResponse>? = null
)

data class ChatsApiResponse(
    val success: Boolean,
    val error: String? = null,
    val chats: List<ChatResponse>? = null
)

data class MessagesApiResponse(
    val success: Boolean,
    val error: String? = null,
    val messages: List<MessageResponse>? = null
)

data class NotificationsApiResponse(
    val success: Boolean,
    val error: String? = null,
    val notifications: List<NotificationResponse>? = null,
    val unreadCount: Int = 0
)

data class NotificationResponse(
    val id: String,
    val userId: String,
    val type: String,
    val title: String,
    val message: String,
    val bookingId: String? = null,
    val rideId: String? = null,
    val isRead: Boolean = false,
    val createdAt: Long
)

data class UserResponse(
    val id: String,
    val email: String,
    val name: String,
    val phone: String? = null,
    val profileImageUrl: String? = null,
    val affiliation: String? = null,
    val verified: Boolean = false,
    val createdAt: Long
)

data class RideResponse(
    val id: String,
    val driverId: String,
    val driverName: String,
    val driverImageUrl: String? = null,
    val pickupLocation: String,
    val destination: String,
    val date: String,
    val time: String,
    val availableSeats: Int,
    val totalSeats: Int,
    val cost: String,
    val vehicleId: String? = null,
    val vehicleModel: String? = null,
    val preferences: List<String> = emptyList(),
    val createdAt: Long
)

data class VehicleResponse(
    val id: String,
    val userId: String,
    val make: String,
    val model: String,
    val year: Int,
    val color: String,
    val licensePlate: String,
    val imageUrl: String? = null,
    val createdAt: Long
)

data class ChatResponse(
    val id: String,
    val userId: String,
    val otherUserId: String,
    val otherUserName: String,
    val otherUserImageUrl: String? = null,
    val lastMessage: String? = null,
    val lastMessageTime: Long? = null,
    val unreadCount: Int = 0,
    val createdAt: Long
)

data class MessageResponse(
    val id: String,
    val chatId: String,
    val senderId: String,
    val receiverId: String,
    val text: String,
    val imageUrl: String? = null,
    val isRead: Boolean = false,
    val timestamp: Long
)

data class ImageUploadResponse(
    val imageUrl: String,
    val filename: String
)

