package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.AppointmentEntity
import com.example.data.model.ResourceEntity
import com.example.data.model.UserEntity
import com.example.data.model.MessageEntity
import androidx.compose.foundation.BorderStroke
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.IRHCViewModel
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                IRHCApp()
            }
        }
    }
}

// Custom modern palette colors representing healing, Trust (Deep Teal) & Life, Compassion (Coral/Pink)
val IRHCTealLocal = Color(0xFF00796B)
val IRHCTealLightLocal = Color(0xFFE0F2F1)
val IRHCCoralLocal = Color(0xFFE57373)
val IRHCCoralDarkLocal = Color(0xFFC62828)
val IRHCBackgroundLocal = Color(0xFFFAFAFA)
val IRHCCardBgLocal = Color(0xFFFFFFFF)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun IRHCApp() {
    val context = LocalContext.current
    val viewModel: IRHCViewModel = viewModel()

    val currentUser by viewModel.currentUser.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val chatLoading by viewModel.chatLoading.collectAsState()

    val usersList by viewModel.users.collectAsState()
    val appointmentsList by viewModel.appointments.collectAsState()
    val messagesList by viewModel.messages.collectAsState()
    val resourcesList by viewModel.resources.collectAsState()

    var showAuthDialog by remember { mutableStateOf(false) }
    var isRegisterMode by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = IRHCBackgroundLocal,
        bottomBar = {
            IRHCBottomNavigation(
                currentScreen = currentScreen,
                onScreenSelected = { viewModel.setScreen(it) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header with App Bar & Role pill switches
            IRHCHeaderSection(
                currentUser = currentUser,
                onLogoutClick = {
                    viewModel.logout()
                    Toast.makeText(context, "Logged out successfully.", Toast.LENGTH_SHORT).show()
                },
                onLoginClick = {
                    isRegisterMode = false
                    showAuthDialog = true
                },
                onRoleSwitch = { role ->
                    viewModel.switchUserRole(role)
                    Toast.makeText(context, "Switched role to $role view for testing", Toast.LENGTH_SHORT).show()
                }
            )

            // Main View Contents with dynamic state swap
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (currentScreen) {
                    is Screen.Home -> {
                        IRHCHomeScreen(
                            resources = resourcesList,
                            currentUser = currentUser,
                            viewModel = viewModel
                        )
                    }
                    is Screen.Chat -> {
                        IRHCChatScreen(
                            messages = messagesList,
                            chatLoading = chatLoading,
                            onSendMessage = { viewModel.sendMessage(it) },
                            onClearChat = { viewModel.clearChatHistory() }
                        )
                    }
                    is Screen.Appointments -> {
                        IRHCBookingScreen(
                            appointments = appointmentsList.filter { 
                                currentUser != null && (it.userPhone == currentUser?.phone || currentUser?.role == "MANAGER_ADMIN")
                            },
                            viewModel = viewModel,
                            currentUser = currentUser,
                            onRequireLogin = {
                                isRegisterMode = false
                                showAuthDialog = true
                            }
                        )
                    }
                    is Screen.Dashboard -> {
                        IRHCDashboardScreen(
                            currentUser = currentUser,
                            users = usersList,
                            appointments = appointmentsList,
                            resources = resourcesList,
                            viewModel = viewModel,
                            onRequireLogin = {
                                isRegisterMode = false
                                showAuthDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    // Modal dialog for safe user Login & Registration
    if (showAuthDialog) {
        Dialog(onDismissRequest = { showAuthDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, IRHCTealLocal.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
                color = IRHCCardBgLocal,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (isRegisterMode) Icons.Default.Person else Icons.Default.Lock,
                        contentDescription = "Auth Logo",
                        tint = IRHCTealLocal,
                        modifier = Modifier
                            .size(56.dp)
                            .padding(bottom = 12.dp)
                    )

                    Text(
                        text = if (isRegisterMode) "Register IRHC Account" else "Confidential Portal Login",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = if (isRegisterMode) "Create a safe account to store records privately." else "Securely access your reproductive consultations.",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // Input states
                    var firstName by remember { mutableStateOf("") }
                    var middleName by remember { mutableStateOf("") }
                    var lastName by remember { mutableStateOf("") }
                    var age by remember { mutableStateOf("") }
                    var sex by remember { mutableStateOf("Female") }
                    var address by remember { mutableStateOf("") }
                    var email by remember { mutableStateOf("") }
                    var phone by remember { mutableStateOf("") }
                    var password by remember { mutableStateOf("") }

                    LazyColumn(
                        modifier = Modifier
                            .heightIn(max = 280.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("Phone Number *") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("phone_input")
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("PIN / Password *") },
                                leadingIcon = { Icon(Icons.Default.Settings, contentDescription = "Pass") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("password_input")
                            )
                        }

                        if (isRegisterMode) {
                            item {
                                OutlinedTextField(
                                    value = firstName,
                                    onValueChange = { firstName = it },
                                    label = { Text("First Name *") },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("first_name_input")
                                )
                            }
                            item {
                                OutlinedTextField(
                                    value = middleName,
                                    onValueChange = { middleName = it },
                                    label = { Text("Middle Name") },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            item {
                                OutlinedTextField(
                                    value = lastName,
                                    onValueChange = { lastName = it },
                                    label = { Text("Last Name") },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Sex: ", fontWeight = FontWeight.Bold, color = Color.Gray)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(selected = sex == "Female", onClick = { sex = "Female" })
                                        Text("Female", fontSize = 14.sp)
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(selected = sex == "Male", onClick = { sex = "Male" })
                                        Text("Male", fontSize = 14.sp)
                                    }
                                }
                            }
                            item {
                                OutlinedTextField(
                                    value = age,
                                    onValueChange = { age = it },
                                    label = { Text("Age *") },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            item {
                                OutlinedTextField(
                                    value = address,
                                    onValueChange = { address = it },
                                    label = { Text("Location Address") },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            item {
                                OutlinedTextField(
                                    value = email,
                                    onValueChange = { email = it },
                                    label = { Text("Email (Optional)") },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (isRegisterMode) {
                                val cleanAge = age.toIntOrNull() ?: 20
                                val newUser = UserEntity(
                                    firstName = firstName,
                                    middleName = middleName,
                                    lastName = lastName,
                                    phone = phone,
                                    password = password,
                                    sex = sex,
                                    age = cleanAge,
                                    address = address,
                                    email = email,
                                    role = "CLIENT"
                                )
                                viewModel.register(newUser) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    if (success) {
                                        isRegisterMode = false
                                    }
                                }
                            } else {
                                viewModel.login(phone, password) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    if (success) {
                                        showAuthDialog = false
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IRHCTealLocal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("auth_submit_btn")
                    ) {
                        Text(if (isRegisterMode) "Register Profile" else "Access Safe Portal", color = Color.White)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(
                        onClick = { isRegisterMode = !isRegisterMode }
                    ) {
                        Text(
                            text = if (isRegisterMode) "Already registered? Log in here" else "New to IRHC? Register a secure profile",
                            color = IRHCCoralDarkLocal,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IRHCHeaderSection(
    currentUser: UserEntity?,
    onLogoutClick: () -> Unit,
    onLoginClick: () -> Unit,
    onRoleSwitch: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
        colors = CardDefaults.cardColors(containerColor = IRHCTealLocal),
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "App Icon Logo",
                        tint = IRHCCoralLocal,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(end = 4.dp)
                    )
                    Column {
                        Text(
                            text = "IRHC Support",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "Reproductive Health Awareness",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                // Top user info and action
                if (currentUser != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = currentUser.firstName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Role: " + currentUser.role,
                                fontSize = 9.sp,
                                color = IRHCCoralLocal,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        IconButton(
                            onClick = onLogoutClick,
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White.copy(alpha = 0.15f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Log Out", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                } else {
                    Button(
                        onClick = onLoginClick,
                        colors = ButtonDefaults.buttonColors(containerColor = IRHCCoralLocal),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Log In / Register", fontSize = 12.sp, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Test Role Switcher Pill Bar - EXTREMELY USEFUL for easy platform demo!
            Text(
                text = "Demo Mode - View as different accounts instantaneously:",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 10.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val rolesList = listOf(
                    "CLIENT" to "Client (Hayyu)",
                    "PHYSICIAN" to "Dr. Zelalem",
                    "MANAGER_ADMIN" to "Manager (Damto)"
                )
                rolesList.forEach { (roleTag, labelName) ->
                    val isActive = currentUser?.role == roleTag
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(30.dp)
                            .testTag("switch_${roleTag.lowercase()}_tab")
                            .clickable { onRoleSwitch(roleTag) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isActive) IRHCCoralLocal else Color.White.copy(alpha = 0.12f)
                        ),
                        shape = RoundedCornerShape(15.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = labelName,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun FlowRowSimple(
    items: List<String>,
    selectedItem: String,
    onSelected: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(items) { cat ->
            val isChosen = cat == selectedItem
            Card(
                modifier = Modifier.clickable { onSelected(cat) },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isChosen) IRHCTealLocal else IRHCTealLightLocal
                )
            ) {
                Text(
                    text = cat,
                    color = if (isChosen) Color.White else IRHCTealLocal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun IRHCHomeScreen(
    resources: List<ResourceEntity>,
    currentUser: UserEntity?,
    viewModel: IRHCViewModel
) {
    var selectedCategory by remember { mutableStateOf("All") }
    var activeResourceForDetail by remember { mutableStateOf<ResourceEntity?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val categories = listOf(
        "All",
        "Contraception & Family Planning",
        "Maternal & Newborn Care",
        "Sexual Reproductive Health & STIs",
        "Adolescent Health & Hygiene"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.height(12.dp))

        // Categories selector scroll
        FlowRowSimple(
            items = categories,
            selectedItem = selectedCategory,
            onSelected = { selectedCategory = it }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Awareness card list
        val filtered = if (selectedCategory == "All") resources else resources.filter { it.category == selectedCategory }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Reproductive Health Awareness Hub",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = IRHCTealLocal
            )

            // If Admin, let them post new resources (FE3, FE6)
            if (currentUser?.role == "MANAGER_ADMIN") {
                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .size(32.dp)
                        .background(IRHCCoralLocal, CircleShape)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Article", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search Empty",
                        tint = Color.LightGray,
                        modifier = Modifier.size(48.dp)
                    )
                    Text("No resources available in this category.", color = Color.Gray, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filtered) { res ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { activeResourceForDetail = res }
                            .shadow(2.dp, shape = RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = IRHCCardBgLocal),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = res.category.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = IRHCCoralLocal
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = res.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = res.content,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 13.sp,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Published by: " + res.author,
                                    fontSize = 11.sp,
                                    color = IRHCTealLocal,
                                    fontWeight = FontWeight.Medium
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Play icon", tint = IRHCTealLocal, modifier = Modifier.size(14.dp))
                                    Text("Preview Media", fontSize = 11.sp, color = IRHCTealLocal, modifier = Modifier.padding(start = 2.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Article expanded detail dialog with media simulations (FE1, FE8)
    if (activeResourceForDetail != null) {
        val res = activeResourceForDetail!!
        var mediaPlaying by remember { mutableStateOf(false) }
        var playProgress by remember { mutableStateOf(0f) }

        LaunchedEffect(mediaPlaying) {
            if (mediaPlaying) {
                while (playProgress < 1f) {
                    delay(400)
                    playProgress += 0.05f
                }
                mediaPlaying = false
                playProgress = 0f
            }
        }

        Dialog(onDismissRequest = { activeResourceForDetail = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
                    .clip(RoundedCornerShape(20.dp)),
                color = IRHCCardBgLocal
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = res.category,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = IRHCCoralLocal
                        )
                        IconButton(onClick = { activeResourceForDetail = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close Detail")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = res.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Simulated Video/Audio Player (FE1, FE8)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!mediaPlaying) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.clickable { mediaPlaying = true }
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Play Video", tint = Color.White, modifier = Modifier.size(48.dp))
                                    Text("Play Educational Video Guide (6m 40s)", color = Color.White, fontSize = 12.sp)
                                    Text("Broadcast Offline/VPN Optimized", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(progress = playProgress, color = IRHCCoralLocal)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Streaming Video: Contraception Info...", color = Color.White, fontSize = 12.sp)
                                    TextButton(onClick = { mediaPlaying = false }) {
                                        Text("Pause", color = Color.White)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        item {
                            Text(
                                text = res.content,
                                fontSize = 14.sp,
                                color = Color.DarkGray,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Author: " + res.author,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = IRHCTealLocal
                        )

                        if (currentUser?.role == "MANAGER_ADMIN") {
                            Button(
                                onClick = {
                                    viewModel.deleteResource(res.id)
                                    activeResourceForDetail = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = IRHCCoralDarkLocal)
                            ) {
                                Text("Delete Resource", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // Add article dialog panel for Admin (FE6)
    if (showAddDialog) {
        var addTitle by remember { mutableStateOf("") }
        var addCategory by remember { mutableStateOf("Contraception & Family Planning") }
        var addContent by remember { mutableStateOf("") }
        var authorName by remember { mutableStateOf(currentUser?.fullName ?: "Administrator") }

        Dialog(onDismissRequest = { showAddDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(20.dp)),
                color = IRHCCardBgLocal
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Publish Awareness Content",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = addTitle,
                        onValueChange = { addTitle = it },
                        label = { Text("Article Title") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Basic Category dropdown simulation
                    Text("Select Category Category:", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.align(Alignment.Start))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val subCats = categories.filter { it != "All" }
                        items(subCats) { cat ->
                            val s = cat == addCategory
                            Card(
                                modifier = Modifier.clickable { addCategory = cat },
                                colors = CardDefaults.cardColors(containerColor = if (s) IRHCTealLocal else Color.LightGray.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 10.sp,
                                    color = if (s) Color.White else Color.Black,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = addContent,
                        onValueChange = { addContent = it },
                        label = { Text("Article Body Content") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { showAddDialog = false }) {
                            Text("Cancel", color = Color.Gray)
                        }

                        Button(
                            onClick = {
                                val nr = ResourceEntity(
                                    title = addTitle,
                                    category = addCategory,
                                    content = addContent,
                                    author = authorName
                                )
                                viewModel.uploadResource(nr) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    if (success) showAddDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IRHCTealLocal)
                        ) {
                            Text("Publish Now", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IRHCChatScreen(
    messages: List<MessageEntity>,
    chatLoading: Boolean,
    onSendMessage: (String) -> Unit,
    onClearChat: () -> Unit
) {
    var textInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Confidential Smart Counselor",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = IRHCTealLocal
                )
                Text(
                    text = "Encrypted VPN connection with AI & clinician pools",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }

            IconButton(
                onClick = onClearChat,
                modifier = Modifier
                    .size(36.dp)
                    .background(IRHCCoralLocal.copy(alpha = 0.15f), CircleShape)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Clear Chat", tint = IRHCCoralDarkLocal, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Chat bubble contents
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (messages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = "Safe chat",
                            tint = IRHCTealLocal.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "Confidential chat channel loaded. Type any reproductive health or family planning question below. Private & secure.",
                            color = Color.Gray,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages) { msg ->
                        val isUser = msg.senderRole == "CLIENT" || msg.senderRole == "MANAGER_ADMIN"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = 16.dp,
                                            topEnd = 16.dp,
                                            bottomStart = if (isUser) 16.dp else 4.dp,
                                            bottomEnd = if (isUser) 4.dp else 16.dp
                                        )
                                    )
                                    .background(
                                        if (isUser) IRHCTealLocal else Color.LightGray.copy(alpha = 0.35f)
                                    )
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                                    .widthIn(max = 270.dp)
                            ) {
                                Column {
                                    Text(
                                        text = msg.senderName,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isUser) Color.White.copy(alpha = 0.7f) else IRHCTealLocal
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = msg.messageText,
                                        fontSize = 14.sp,
                                        color = if (isUser) Color.White else Color.Black
                                    )
                                }
                            }
                        }
                    }

                    if (chatLoading) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.3f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = IRHCTealLocal)
                                        Text(" AI Doctor is reflecting...", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(start = 8.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Text send row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text("Ask confidential questions...", fontSize = 13.sp) },
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IRHCTealLocal,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            Spacer(modifier = Modifier.width(10.dp))

            FloatingActionButton(
                onClick = {
                    if (textInput.isNotBlank()) {
                        onSendMessage(textInput)
                        textInput = ""
                    }
                },
                containerColor = IRHCTealLocal,
                modifier = Modifier
                    .size(46.dp)
                    .testTag("send_msg_btn"),
                shape = CircleShape
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun IRHCBookingScreen(
    appointments: List<AppointmentEntity>,
    viewModel: IRHCViewModel,
    currentUser: UserEntity?,
    onRequireLogin: () -> Unit
) {
    val context = LocalContext.current

    var selectedPhysician by remember { mutableStateOf("Zelalem Abera (Msc)") }
    var selectedDate by remember { mutableStateOf("2026-05-25") }
    var selectedTime by remember { mutableStateOf("10:00 AM") }
    var inputReason by remember { mutableStateOf("") }

    if (currentUser == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                Icon(Icons.Default.Lock, contentDescription = "Secure", tint = IRHCCoralLocal, modifier = Modifier.size(56.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Secure Authorization Required", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                Text(
                    text = "Log in or register your confidential profile to schedule actual checkups, prenatal tests, or contraception consultations safely.",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                Button(
                    onClick = onRequireLogin,
                    colors = ButtonDefaults.buttonColors(containerColor = IRHCTealLocal)
                ) {
                    Text("Access Secure Login Portal", color = Color.White)
                }
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Request Consultation Appointment", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = IRHCTealLocal)
        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = IRHCCardBgLocal),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Select Specialist Physician:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val docs = listOf("Zelalem Abera (Msc)", "Gemechu Ragea (MD)")
                    docs.forEach { doc ->
                        val isS = doc == selectedPhysician
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedPhysician = doc },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isS) IRHCTealLightLocal else Color.LightGray.copy(alpha = 0.2f)
                            ),
                            border = if (isS) BorderStroke(1.5.dp, IRHCTealLocal) else null
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(doc, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                Text(
                                    text = if (doc.contains("Zelalem")) "Family planning, Impalnt spec." else "Maternal screening, Obstretic MD",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = selectedDate,
                        onValueChange = { selectedDate = it },
                        label = { Text("Date (YYYY-MM-DD)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = selectedTime,
                        onValueChange = { selectedTime = it },
                        label = { Text("Time (e.g. 10:00 AM)") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = inputReason,
                    onValueChange = { inputReason = it },
                    label = { Text("Medical Concern or Special Reasons") },
                    placeholder = { Text("E.g. family planning pill side effects, prenatal schedule...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val appt = AppointmentEntity(
                            userName = currentUser.fullName,
                            userPhone = currentUser.phone,
                            physicianName = selectedPhysician,
                            date = selectedDate,
                            time = selectedTime,
                            reason = inputReason,
                            status = "PENDING"
                        )
                        viewModel.bookAppointment(appt) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            if (success) {
                                inputReason = ""
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("book_appt_submit_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = IRHCTealLocal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Submit Appointment Request", color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("My Confidentially Scheduled Appointments", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)

        if (appointments.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No appointment requests found in your system profile.", color = Color.Gray, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 10.dp)
            ) {
                items(appointments) { app ->
                    var showReportDialog by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = IRHCCardBgLocal),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(app.physicianName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.DarkGray)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val pillColor = when (app.status) {
                                        "PENDING" -> Color(0xFFFBC02D)
                                        "APPROVED" -> IRHCTealLocal
                                        "COMPLETED" -> Color(0xFF4CAF50)
                                        else -> IRHCCoralLocal
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(pillColor.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(app.status, color = pillColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }

                                    IconButton(onClick = { viewModel.deleteAppointment(app.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text("Date/Time: " + app.date + " (" + app.time + ")", fontSize = 12.sp, color = Color.Gray)
                            Text("Reason: " + app.reason, fontSize = 12.sp, color = Color.DarkGray)

                            if (app.status == "COMPLETED") {
                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = { showReportDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = IRHCTealLocal),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text("View Prescription & Report", fontSize = 10.sp, color = Color.White)
                                }
                            }
                        }
                    }

                    if (showReportDialog) {
                        Dialog(onDismissRequest = { showReportDialog = false }) {
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(16.dp).clip(RoundedCornerShape(16.dp)),
                                color = IRHCCardBgLocal
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Prescription & Clinical Report", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.DarkGray)
                                        IconButton(onClick = { showReportDialog = false }) {
                                            Icon(Icons.Default.Close, contentDescription = "Close")
                                        }
                                    }
                                    Divider()
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("Consultation Reason: " + app.reason, fontSize = 12.sp, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Clinical Treatment Summary:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = IRHCTealLocal)
                                    Text(app.treatmentReport, fontSize = 13.sp, color = Color.DarkGray)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("Prescribed Treatment Medicine:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = IRHCCoralDarkLocal)
                                    Text(app.prescription, fontSize = 13.sp, color = Color.DarkGray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IRHCDashboardScreen(
    currentUser: UserEntity?,
    users: List<UserEntity>,
    appointments: List<AppointmentEntity>,
    resources: List<ResourceEntity>,
    viewModel: IRHCViewModel,
    onRequireLogin: () -> Unit
) {
    val context = LocalContext.current

    if (currentUser == null || (currentUser.role != "PHYSICIAN" && currentUser.role != "MANAGER_ADMIN")) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                Icon(Icons.Default.Lock, contentDescription = "Secure", tint = IRHCCoralLocal, modifier = Modifier.size(56.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text("Managerial Authorization Required", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                Text(
                    text = "This terminal is exclusively restricted for Physicians (such as Dr. Zelalem) or System Managers (such as Damto) to approve scheduling, clinical prescriptions, or compile monthly reports.",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                Button(
                    onClick = onRequireLogin,
                    colors = ButtonDefaults.buttonColors(containerColor = IRHCTealLocal)
                ) {
                    Text("Access Secure Login Portal", color = Color.White)
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Tip: You can instantly switch roles using the demo pill bar above!",
                    color = IRHCTealLocal,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        return
    }

    if (currentUser.role == "MANAGER_ADMIN") {
        // MANAGER / ADMIN view showing Monthly Report Statistics with simple visual charts (FE2)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("System Manager Dashboard", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = IRHCTealLocal)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(IRHCCoralLocal.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Active Admin: Damto", color = IRHCCoralDarkLocal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Quick Info stats
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(
                        "Total Clinics Patients" to users.filter { it.role == "CLIENT" }.size.toString(),
                        "Pending Appts" to appointments.filter { it.status == "PENDING" }.size.toString(),
                        "Completed Records" to appointments.filter { it.status == "COMPLETED" }.size.toString(),
                        "Awareness Articles" to resources.size.toString()
                    ).forEach { (lbl, valStr) ->
                        Card(
                            modifier = Modifier.weight(1f),
                            colors = CardDefaults.cardColors(containerColor = IRHCCardBgLocal),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(valStr, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = IRHCTealLocal)
                                Text(lbl, fontSize = 9.sp, color = Color.Gray, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            }

            // Simple visual canvas chart for monthly clinical metrics as mandated in guidelines! (FE2)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = IRHCCardBgLocal),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Monthly Cumulative Consultations (By Category)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                        Text("Graphical statistics of monthly clinic utilization", fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(14.dp))

                        // Custom visual drawing utilizing Android Canvas in compose, much lighter than heavy charts libraries
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        ) {
                            val vals = listOf(14f, 26f, 35f, 18f) // simulated metrics for Contraception, Maternal, STIs, Adolescent
                            val labels = listOf("Contrac.", "Matern.", "STIs", "Adoles.")
                            val maxV = 40f
                            val barWidth = 45.dp.toPx()
                            val spacing = 24.dp.toPx()

                            vals.forEachIndexed { i, v ->
                                val heightPct = v / maxV
                                val x = spacing + i * (barWidth + spacing)
                                val barHeight = size.height * heightPct

                                // Draw bar gradient
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(IRHCCoralLocal, IRHCTealLocal)
                                    ),
                                    topLeft = Offset(x, size.height - barHeight),
                                    size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                                )
                            }
                        }

                        // Labels
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            val labels = listOf("Contraception (14)", "Maternal (26)", "STIs/VCT (35)", "Adolescent (18)")
                            labels.forEach { lbl ->
                                Text(lbl, fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Register system accounts (register physicians or managers, FE10)
            item {
                var docFirst by remember { mutableStateOf("") }
                var docLast by remember { mutableStateOf("") }
                var docPhone by remember { mutableStateOf("") }
                var docPass by remember { mutableStateOf("") }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = IRHCCardBgLocal),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Add Account to Clinician / Staff Pool (FE10)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)

                        OutlinedTextField(
                            value = docFirst,
                            onValueChange = { docFirst = it },
                            label = { Text("Physician First Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = docLast,
                            onValueChange = { docLast = it },
                            label = { Text("Physician Last Name/Specialty") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = docPhone,
                            onValueChange = { docPhone = it },
                            label = { Text("Clinician Phone") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = docPass,
                            onValueChange = { docPass = it },
                            label = { Text("Access Password") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                val d = UserEntity(
                                    firstName = docFirst,
                                    middleName = "Dr.",
                                    lastName = docLast,
                                    phone = docPhone,
                                    password = docPass,
                                    sex = "Female",
                                    age = 35,
                                    address = "Addis Ababa Clinic",
                                    email = "",
                                    role = "PHYSICIAN"
                                )
                                viewModel.register(d) { s, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    if (s) {
                                        docFirst = ""; docLast = ""; docPhone = ""; docPass = ""
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IRHCTealLocal),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Enrol Physician Account", color = Color.White)
                        }
                    }
                }
            }

            // View clinic accounts list (FE2, FE3)
            item {
                Text("Registered Safe Users Profile Ledger", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            }

            items(users) { usr ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = IRHCCardBgLocal),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(usr.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.DarkGray)
                            Text("Phone: " + usr.phone + " | Access ID: IRHC-" + usr.id, fontSize = 12.sp, color = Color.Gray)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (usr.role == "PHYSICIAN") IRHCTealLocal.copy(alpha = 0.15f) else Color.LightGray)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(usr.role, color = if (usr.role == "PHYSICIAN") IRHCTealLocal else Color.DarkGray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    } else {
        // PHYSICIAN view: manage appointment workflow, approve, or compile clinical treatment histories! (FE3, FE12, FE13, FE14)
        val docAppts = appointments.filter { it.physicianName.contains(currentUser.firstName) }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Clinical Reception Console", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = IRHCTealLocal)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(IRHCTealLocal.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Active Physician: " + currentUser.firstName, color = IRHCTealLocal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item {
                Text("Assigned Consultation Tasks:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            }

            if (docAppts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No scheduled consultations found in your ledger.", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            } else {
                items(docAppts) { app ->
                    var showPrescribeDialog by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = IRHCCardBgLocal),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Patient: " + app.userName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.DarkGray)
                                Row {
                                    val colorB = when (app.status) {
                                        "PENDING" -> Color(0xFFFBC02D)
                                        "APPROVED" -> IRHCTealLocal
                                        "COMPLETED" -> Color(0xFF4CAF50)
                                        else -> IRHCCoralLocal
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(colorB.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(app.status, color = colorB, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text("Contact Number: " + app.userPhone, fontSize = 12.sp, color = Color.Gray)
                            Text("Scheduled Time: " + app.date + " (" + app.time + ")", fontSize = 12.sp, color = Color.Gray)
                            Text("Medical Concern: " + app.reason, fontSize = 12.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)

                            Spacer(modifier = Modifier.height(10.dp))

                            if (app.status == "PENDING") {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.updateAppointmentStatus(app.id, "APPROVED") },
                                        colors = ButtonDefaults.buttonColors(containerColor = IRHCTealLocal),
                                        modifier = Modifier.weight(1f).height(36.dp)
                                    ) {
                                        Text("Approve Request", color = Color.White, fontSize = 11.sp)
                                    }

                                    Button(
                                        onClick = { viewModel.updateAppointmentStatus(app.id, "REJECTED") },
                                        colors = ButtonDefaults.buttonColors(containerColor = IRHCCoralLocal),
                                        modifier = Modifier.weight(1f).height(36.dp)
                                    ) {
                                        Text("Decline", color = Color.White, fontSize = 11.sp)
                                    }
                                }
                            } else if (app.status == "APPROVED") {
                                Button(
                                    onClick = { showPrescribeDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = IRHCCoralDarkLocal),
                                    modifier = Modifier.fillMaxWidth().height(36.dp)
                                ) {
                                    Text("Add Treatment Summary & Prescribe (FE13)", color = Color.White, fontSize = 11.sp)
                                }
                            } else if (app.status == "COMPLETED") {
                                Divider(modifier = Modifier.padding(vertical = 4.dp))
                                Text("Treatment Report Saved:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IRHCTealLocal)
                                Text(app.treatmentReport, fontSize = 12.sp, color = Color.DarkGray)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Rx Medicine:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IRHCCoralDarkLocal)
                                Text(app.prescription, fontSize = 12.sp, color = Color.DarkGray)
                            }
                        }
                    }

                    // Dialog to write clinical checkups (FE13)
                    if (showPrescribeDialog) {
                        var repText by remember { mutableStateOf("") }
                        var rxText by remember { mutableStateOf("") }

                        Dialog(onDismissRequest = { showPrescribeDialog = false }) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                color = IRHCCardBgLocal
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Save Clinical Encounter (FE13)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.DarkGray)
                                        IconButton(onClick = { showPrescribeDialog = false }) {
                                            Icon(Icons.Default.Close, contentDescription = "Close")
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text("Patient Name: " + app.userName, fontSize = 12.sp, color = Color.Gray)
                                    Text("Initial complaint: " + app.reason, fontSize = 12.sp, color = Color.Gray)

                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = repText,
                                        onValueChange = { repText = it },
                                        label = { Text("Clinical Treatment Report Details") },
                                        placeholder = { Text("Encounter findings, counseling advice provided...") },
                                        modifier = Modifier.fillMaxWidth().height(80.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedTextField(
                                        value = rxText,
                                        onValueChange = { rxText = it },
                                        label = { Text("Medical Prescription (Rx)") },
                                        placeholder = { Text("Items, dosage instructions...") },
                                        modifier = Modifier.fillMaxWidth().height(80.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Button(
                                        onClick = {
                                            viewModel.completePhysicianConsultation(app.id, repText, rxText)
                                            showPrescribeDialog = false
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = IRHCTealLocal),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Save Encounter Records", color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IRHCBottomNavigation(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = IRHCTealLocal,
        modifier = Modifier.shadow(8.dp)
    ) {
        val navItems = listOf(
            Triple(Screen.Home, "Awareness", Icons.Default.Home),
            Triple(Screen.Chat, "Chat", Icons.Default.Email),
            Triple(Screen.Appointments, "Schedule", Icons.Default.DateRange),
            Triple(Screen.Dashboard, "Dashboard", Icons.Default.Settings)
        )

        navItems.forEach { (screen, label, icon) ->
            val isSelect = currentScreen::class == screen::class
            NavigationBarItem(
                selected = isSelect,
                onClick = { onScreenSelected(screen) },
                icon = { Icon(icon, contentDescription = label, tint = if (isSelect) IRHCTealLocal else Color.White.copy(alpha = 0.7f)) },
                label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelect) FontWeight.Bold else FontWeight.Normal) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.White,
                    selectedTextColor = Color.White,
                    unselectedTextColor = Color.White.copy(alpha = 0.7f)
                ),
                modifier = Modifier.testTag("nav_${label.lowercase()}_tab")
            )
        }
    }
}
