package com.savexa.dialer

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*

class MainActivity : ComponentActivity() {

    private val roleLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SavexaTheme {
                SavexaApp(
                    onDefaultDialer = {
                        requestDialerRole()
                    },
                    onCall = { number ->
                        val intent = Intent(
                            Intent.ACTION_DIAL,
                            Uri.fromParts("tel", number, null)
                        )
                        startActivity(intent)
                    }
                )
            }
        }
    }

    private fun requestDialerRole() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            val manager =
                getSystemService(Context.ROLE_SERVICE)
                    as RoleManager

            if (manager.isRoleAvailable(RoleManager.ROLE_DIALER) &&
                !manager.isRoleHeld(RoleManager.ROLE_DIALER)
            ) {
                roleLauncher.launch(
                    manager.createRequestRoleIntent(
                        RoleManager.ROLE_DIALER
                    )
                )
            }
        }
    }
}

private val SavexaBlue = Color(0xFF2457C5)
private val SavexaBackground = Color(0xFFF5F6FA)

@Composable
fun SavexaTheme(content: @Composable () -> Unit) {

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = SavexaBlue,
            background = SavexaBackground,
            surface = Color.White,
            onSurface = Color(0xFF171923)
        ),
        content = content
    )
}

enum class DialerTab {
    RECENTS,
    CONTACTS,
    KEYPAD,
    SETTINGS
}

@Composable
fun SavexaApp(
    onDefaultDialer: () -> Unit,
    onCall: (String) -> Unit
) {

    var selectedTab by remember {
        mutableStateOf(DialerTab.KEYPAD)
    }

    var phoneNumber by remember {
        mutableStateOf("")
    }

    Scaffold(
        containerColor = SavexaBackground,

        bottomBar = {

            NavigationBar(
                containerColor = Color.White
            ) {

                val tabs = listOf(
                    DialerTab.RECENTS,
                    DialerTab.CONTACTS,
                    DialerTab.KEYPAD,
                    DialerTab.SETTINGS
                )

                tabs.forEach { tab ->

                    val icon = when (tab) {
                        DialerTab.RECENTS ->
                            Icons.Default.History

                        DialerTab.CONTACTS ->
                            Icons.Default.Contacts

                        DialerTab.KEYPAD ->
                            Icons.Default.Dialpad

                        DialerTab.SETTINGS ->
                            Icons.Default.Settings
                    }

                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = {
                            selectedTab = tab
                        },
                        icon = {
                            Icon(icon, contentDescription = null)
                        },
                        label = {
                            Text(tab.name.lowercase().replaceFirstChar {
                                it.uppercase()
                            })
                        }
                    )
                }
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {

            Text(
                text = when (selectedTab) {
                    DialerTab.RECENTS -> "Recent calls"
                    DialerTab.CONTACTS -> "Contacts"
                    DialerTab.KEYPAD -> "Phone"
                    DialerTab.SETTINGS -> "Settings"
                },
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(20.dp))

            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    (
                        slideInHorizontally(
                            spring(
                                stiffness =
                                    Spring.StiffnessMediumLow
                            )
                        ) { it / 5 } + fadeIn()
                    ) togetherWith (
                        slideOutHorizontally(
                            spring(
                                stiffness =
                                    Spring.StiffnessMediumLow
                            )
                        ) { -it / 6 } + fadeOut()
                    )
                },
                label = "SavexaPageTransition"
            ) { tab ->

                when (tab) {

                    DialerTab.KEYPAD -> {

                        SavexaKeypad(
                            number = phoneNumber,
                            onNumberChange = {
                                phoneNumber = it
                            },
                            onCall = {
                                if (phoneNumber.isNotBlank()) {
                                    onCall(phoneNumber)
                                }
                            }
                        )
                    }

                    DialerTab.RECENTS -> {
                        EmptyScreen(
                            "Recent calls",
                            "Your call history will appear here."
                        )
                    }

                    DialerTab.CONTACTS -> {
                        EmptyScreen(
                            "Contacts",
                            "Your contacts will appear here."
                        )
                    }

                    DialerTab.SETTINGS -> {
                        Column {
                            Text("Default phone app")
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = onDefaultDialer
                            ) {
                                Text("Set as default dialer")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SavexaKeypad(
    number: String,
    onNumberChange: (String) -> Unit,
    onCall: () -> Unit
) {

    val keys = listOf(
        listOf("1" to "", "2" to "ABC", "3" to "DEF"),
        listOf("4" to "GHI", "5" to "JKL", "6" to "MNO"),
        listOf("7" to "PQRS", "8" to "TUV", "9" to "WXYZ"),
        listOf("*" to "", "0" to "+", "#" to "")
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(Modifier.height(22.dp))

        Text(
            text = number.ifEmpty { "Enter number" },
            fontSize = 32.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1
        )

        Spacer(Modifier.height(22.dp))

        keys.forEach { row ->

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceEvenly
            ) {

                row.forEach { (digit, letters) ->

                    KeyButton(
                        digit = digit,
                        letters = letters,
                        onClick = {
                            onNumberChange(number + digit)
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(15.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Spacer(Modifier.size(60.dp))

            FilledIconButton(
                onClick = onCall,
                modifier = Modifier.size(68.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = SavexaBlue
                )
            ) {
                Icon(
                    Icons.Default.Call,
                    contentDescription = "Call",
                    modifier = Modifier.size(30.dp)
                )
            }

            TextButton(
                onClick = {
                    if (number.isNotEmpty()) {
                        onNumberChange(number.dropLast(1))
                    }
                }
            ) {
                Text("Delete")
            }
        }

        TextButton(
            onClick = {
                onNumberChange("")
            }
        ) {
            Text("Clear number")
        }
    }
}

@Composable
fun KeyButton(
    digit: String,
    letters: String,
    onClick: () -> Unit
) {

    val scale = remember {
        Animatable(1f)
    }

    Surface(
        modifier = Modifier
            .padding(vertical = 5.dp)
            .size(78.dp)
            .clip(CircleShape)
            .clickable {

                onClick()
            },
        color = Color.White,
        shape = CircleShape
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                digit,
                fontSize = 29.sp,
                fontWeight = FontWeight.Medium
            )

            if (letters.isNotEmpty()) {
                Text(
                    letters,
                    fontSize = 9.sp,
                    letterSpacing = 1.4.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun EmptyScreen(
    title: String,
    subtitle: String
) {

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        Text(
            title,
            fontSize = 25.sp,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            subtitle,
            color = Color.Gray
        )
    }
}
