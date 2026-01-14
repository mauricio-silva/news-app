package com.example.news_app.presentation.auth

import android.app.Activity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.news_app.R

@Composable
fun AuthGateScreen(
    onAuthorized: () -> Unit
) {
    val ctx = LocalContext.current
    val activity = ctx as FragmentActivity

    var status by remember { mutableStateOf("Checking biometric capability…") }
    var showRetry by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val canAuth = BiometricManager.from(ctx).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        )
        if (canAuth == BiometricManager.BIOMETRIC_SUCCESS) {
            status = "Biometric required"
            authenticate(
                activity,
                onSuccess = onAuthorized,
                onFailure = {
                    status = it
                    showRetry = true
                }
            )
        } else {
            onAuthorized()
        }
    }

    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(R.string.biometric_auth_security_check),
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.height(12.dp))
            Text(status, style = MaterialTheme.typography.bodyMedium)
            if (showRetry) {
                Spacer(Modifier.height(16.dp))
                Button(onClick = {
                    showRetry = false
                    status = "Biometric required"
                    authenticate(
                        activity,
                        onSuccess = onAuthorized,
                        onFailure = {
                            status = it
                            showRetry = true
                        }
                    )
                }) { Text(stringResource(R.string.retry)) }
            }
        }
    }
}

private fun authenticate(
    activity: FragmentActivity,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    val executor = ContextCompat.getMainExecutor(activity)
    val prompt = BiometricPrompt(
        activity, executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                onFailure(errString.toString())
            }

            override fun onAuthenticationFailed() {
                onFailure("Authentication failed. Try again.")
            }
        }
    )

    val info = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Unlock News App")
        .setSubtitle("Authenticate to continue")
        .setNegativeButtonText("Cancel")
        .build()

    prompt.authenticate(info)
}