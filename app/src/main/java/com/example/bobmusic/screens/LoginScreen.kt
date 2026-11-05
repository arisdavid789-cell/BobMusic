package com.example.bobmusic.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    existingName: String,
    isLoading: Boolean,
    errorMessage: String?,
    onLogin: (url: String, user: String, pass: String, name: String) -> Unit
) {
    var url by remember { mutableStateOf("http://") }
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var name by remember { mutableStateOf(existingName) }

    var localError by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(errorMessage) { localError = errorMessage }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.22f),
                        MaterialTheme.colorScheme.background
                    ),
                    center = Offset(0f, 0f),
                    radius = 1800f
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 22.dp,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = if (name.isBlank()) "Welcome" else "Welcome, $name",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                    )

                    AnimatedVisibility(
                        visible = localError != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.errorContainer)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = localError ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; localError = null },
                        label = { Text("What's your name?") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        ),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it; localError = null },
                        label = { Text("Server URL") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        ),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = user,
                        onValueChange = { user = it; localError = null },
                        label = { Text("Username") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        ),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = pass,
                        onValueChange = { pass = it; localError = null },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        ),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp)
                    )

                    Surface(
                        onClick = { onLogin(url, user, pass, name) },
                        enabled = url.isNotBlank() && user.isNotBlank() && pass.isNotBlank() && !isLoading,
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primary,
                        shadowElevation = 10.dp,
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    "Connect",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}