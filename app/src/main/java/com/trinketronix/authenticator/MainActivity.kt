package com.trinketronix.authenticator

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.trinketronix.authenticator.ui.theme.AuthenticatorTheme

class MainActivity : ComponentActivity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private val viewModel: SignInViewModel by viewModels()

    /**
     * onCreate from Activity Lifecycle
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // esto es parte de las funcionalidades del JetPack Compose
        // los set contents de jetpack compose solo reciben fuciones o blockes hechos con jetpack compose
        setContent {
            AuthenticatorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    SignInScreen(viewModel, googleSignInClient)
                }
            }
        }
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)!!
            viewModel.signInWithGoogle(account.idToken!!) { success ->
                if (success) {
                    // Navigate to next screen
                } else {
                    // Show error message
                }
            }
        } catch (e: ApiException) {
            Log.w("MainActivity", "Google sign in failed", e)
        }
    }
    companion object{
        const val RC_SIGN_IN: Int = 16
    }

}

@Composable
fun SignInScreen(
    onGoogleSignIn: () -> Unit,
    onEmailSignIn: (String, String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = onGoogleSignIn) {
            Text(text = "Login with Google")
        }

        Spacer(modifier = Modifier.height(16.dp))

        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )

        Button(onClick = { onEmailSignIn(email, password) }) {
            Text(text = "Login with Email")
        }
    }
}


@Composable
fun SignInScreen(viewModel: SignInViewModel, googleSignInClient: GoogleSignInClient) {

    val context = LocalContext.current as Activity

    SignInScreen(
        onGoogleSignIn = {
            val signInIntent = googleSignInClient.signInIntent
            context.startActivityForResult(signInIntent, MainActivity.RC_SIGN_IN)
        },
        onEmailSignIn = { email, password ->
            viewModel.signInWithEmail(email, password) { success ->
                if (success) {
                    // Navigate to next screen
                } else {
                    // Show error message
                }
            }
        }
    )
}




@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AuthenticatorTheme {
        Greeting("Android")
    }
}

