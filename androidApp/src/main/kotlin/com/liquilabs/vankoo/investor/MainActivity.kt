package com.liquilabs.vankoo.investor

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {

    private val appHost = VankooAppHost()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            appHost.Content()
        }
    }

    /**
     * La actividad es singleTop, así que un enlace que llega con la app abierta no
     * crea otra instancia: aterriza aquí, y hay que pasárselo a quien sabe navegar.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        appHost.onNewIntent(intent)
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
