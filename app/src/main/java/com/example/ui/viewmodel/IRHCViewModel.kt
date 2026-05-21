package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.*
import com.example.data.network.GeminiContent
import com.example.data.network.GeminiNetworkClient
import com.example.data.network.GeminiPart
import com.example.data.repository.Repository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class Screen {
    object Home : Screen()
    object Chat : Screen()
    object Appointments : Screen()
    object Dashboard : Screen()
}

class IRHCViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = Repository(db)

    // Current State Management
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Flows extracted reactively from Room DB
    val users: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appointments: StateFlow<List<AppointmentEntity>> = repository.allAppointments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val messages: StateFlow<List<MessageEntity>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val resources: StateFlow<List<ResourceEntity>> = repository.allResources
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Chat loader state
    private val _chatLoading = MutableStateFlow(false)
    val chatLoading: StateFlow<Boolean> = _chatLoading.asStateFlow()

    init {
        // Startup tasks
        viewModelScope.launch {
            // Seed base entities
            repository.prepopulateIfEmpty()
            repository.populateResources()
            // Auto login as general test client to start with so they can explore immediately
            val defaultClient = repository.getUserByCredentials("0911234567", "password123")
            if (defaultClient != null) {
                _currentUser.value = defaultClient
            }
        }
    }

    fun setScreen(screen: Screen) {
        _currentScreen.value = screen
    }

    // Auth Actions
    fun login(phone: String, password: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByCredentials(phone, password)
            if (user != null) {
                _currentUser.value = user
                onResult(true, "Welcome back, ${user.fullName}!")
            } else {
                onResult(false, "Invalid phone or password credentials.")
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _currentScreen.value = Screen.Home
    }

    fun switchUserRole(role: String) {
        viewModelScope.launch {
            // Switch user quickly to test interfaces
            when (role) {
                "CLIENT" -> {
                    val client = repository.getUserByCredentials("0911234567", "password123")
                    if (client != null) _currentUser.value = client
                }
                "PHYSICIAN" -> {
                    val doc = repository.getUserByCredentials("0911000001", "password123")
                    if (doc != null) _currentUser.value = doc
                }
                "MANAGER_ADMIN" -> {
                    val mgr = repository.getUserByCredentials("0911000003", "password123")
                    if (mgr != null) _currentUser.value = mgr
                }
            }
        }
    }

    // Register User
    fun register(user: UserEntity, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            if (user.phone.isBlank() || user.password.isBlank() || user.firstName.isBlank()) {
                onResult(false, "Please fill in all mandatory fields.")
                return@launch
            }
            try {
                repository.registerUser(user)
                onResult(true, "Profile registered successfully! You can now log in.")
            } catch (e: Exception) {
                onResult(false, "Registration failed: ${e.localizedMessage}")
            }
        }
    }

    // Book Appointment
    fun bookAppointment(appointment: AppointmentEntity, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            if (appointment.physicianName.isBlank() || appointment.date.isBlank() || appointment.time.isBlank() || appointment.reason.isBlank()) {
                onResult(false, "Please fill in all details.")
                return@launch
            }
            try {
                repository.bookAppointment(appointment)
                onResult(true, "Your consultation appointment with ${appointment.physicianName} was submitted!")
            } catch (e: Exception) {
                onResult(false, "Booking error: ${e.localizedMessage}")
            }
        }
    }

    // Update status
    fun updateAppointmentStatus(id: Int, status: String) {
        viewModelScope.launch {
            repository.updateAppointmentStatus(id, status)
        }
    }

    // Physician complete appointment with report and prescription
    fun completePhysicianConsultation(id: Int, report: String, prescription: String) {
        viewModelScope.launch {
            repository.addConsultationReport(id, report, prescription)
        }
    }

    // Clear local chat
    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearChat()
        }
    }

    // Send Consultation Chat
    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val current = _currentUser.value
        val name = current?.fullName ?: "Guest Client"
        val role = current?.role ?: "CLIENT"

        viewModelScope.launch {
            // Save client text locally
            val userMsg = MessageEntity(
                senderName = name,
                senderRole = role,
                messageText = text,
                isAi = false
            )
            repository.insertMessage(userMsg)

            // Trigger AI Doctor
            _chatLoading.value = true

            // Gather recent context for the prompt
            val recentMessagesList = messages.value.takeLast(10)
            val geminiHistory = recentMessagesList.map {
                GeminiContent(parts = listOf(GeminiPart(text = "${it.senderName} (${it.senderRole}): ${it.messageText}")))
            }

            // Call Gemini REST Network Client
            val aiResponseHex = GeminiNetworkClient.consultGemini(text, geminiHistory)

            // Create Doctor's response entity
            val doctorMsg = MessageEntity(
                senderName = if (aiResponseHex.startsWith("Note:")) "System Notice" else "IRHC Smart Advisor (AI)",
                senderRole = "AI",
                messageText = aiResponseHex,
                isAi = true
            )
            repository.insertMessage(doctorMsg)
            _chatLoading.value = false
        }
    }

    // Admin Resource Actions
    fun uploadResource(resource: ResourceEntity, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            if (resource.title.isBlank() || resource.content.isBlank() || resource.category.isBlank()) {
                onResult(false, "Please satisfy title, category and content parameters.")
                return@launch
            }
            try {
                repository.saveResource(resource)
                onResult(true, "Educational awareness resource was uploaded successfully!")
            } catch (e: Exception) {
                onResult(false, "Upload error: ${e.localizedMessage}")
            }
        }
    }

    fun deleteResource(id: Int) {
        viewModelScope.launch {
            repository.deleteResource(id)
        }
    }

    fun deleteAppointment(id: Int) {
        viewModelScope.launch {
            repository.deleteAppointment(id)
        }
    }
}
