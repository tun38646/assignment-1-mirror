package edu.temple.convoy

import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import edu.temple.convoy.ui.theme.ConvoyTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Testing AccountManager functions
        val accountManager = AccountManager(this@MainActivity)
//        accountManager.register("user", "first", "last", "123")
//        accountManager.login("user", "123")

        val convoyManager = ConvoyManager(this@MainActivity)
//        convoyManager.create("user", "12345")

        setContent {
            ConvoyTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ConvoyApp(accountManager)
                }
            }
        }
    }
}

@Composable
fun ConvoyApp(accountManager: AccountManager) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "initialScreen") {
        composable("initialScreen") {
            InitialScreen(navController = navController)
        }
        composable("createAccountScreen") {
            RegisterScreen(navController = navController, accountManager = accountManager)
        }
        composable("loginScreen") {
            LoginScreen(navController = navController, accountManager = accountManager)
        }
        composable("mainScreen") {
            MainScreen(navController = navController)
        }
    }
}

@Composable
fun InitialScreen(navController: NavController) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Welcome to Convoy", Modifier.padding(bottom = 16.dp), fontSize = 32.sp)
        Button(onClick = { navController.navigate("createAccountScreen") }) {
            Text(text = "Create an Account")
        }
        Button(onClick = { navController.navigate("loginScreen") }) {
            Text(text = "Log In")
        }
    }
}

@Composable
fun RegisterScreen(navController: NavController, accountManager: AccountManager) {
    var username by remember {
        mutableStateOf("")
    }
    var firstName by remember {
        mutableStateOf("")
    }
    var lastName by remember {
        mutableStateOf("")
    }
    var password by remember {
        mutableStateOf("")
    }
    var confirmPassword by remember {
        mutableStateOf("")
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Register")
        Text(text = errorMessage, color = Color.Red)
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text(text = "Username") }
        )
        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text(text = "First Name") }
        )
        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text(text = "Last Name") }
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(text = "Password") }
        )
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text(text = "Confirm Password") }
        )
        Button(onClick = {
            if (username.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                errorMessage = "Please fill in all fields."
            } else if (password != confirmPassword) {
                errorMessage = "Passwords do not match."
            } else {
                accountManager.register(username, firstName, lastName, password)
                navController.navigate("mainScreen")
            }

        }) {
            Text(text = "Register")
        }
    }

}

@Composable
fun LoginScreen(navController: NavController, accountManager: AccountManager) {
    var username by remember {
        mutableStateOf("")
    }
    var password by remember {
        mutableStateOf("")
    }

    var errorMessage by remember {
        mutableStateOf("")
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Login")
        Text(text = errorMessage, color = Color.Red)
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text(text = "Username") }
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(text = "Password") }
        )
        Button(onClick = {
            if (username.isEmpty() || password.isEmpty()) {
                errorMessage = "Please fill in all fields."
            } else {
                accountManager.login(username, password)
                navController.navigate("mainScreen")
            }
        }) {
            Text(text = "Login")
        }
    }
}

@Composable
fun MainScreen(navController: NavController) {
    val locationPermissionRequest = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Precise location access granted.
            }

            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Only approximate location access granted.
            }

            else -> {
                // No location access granted.
            }
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    MapComponent()
    Row(
        modifier = Modifier.padding(bottom = 32.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        FloatingActionButton(onClick = { /*TODO*/ }) {
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Start Convoy")
        }
        FloatingActionButton(onClick = { /*TODO*/ }) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Join Convoy")
        }
        FloatingActionButton(onClick = { /*TODO*/ }) {
            Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Leave Convoy")
        }
    }
}

@Composable
fun MapComponent() {
    val singapore = LatLng(1.35, 103.87)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(singapore, 10f)
    }
    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        Marker(
            state = MarkerState(position = singapore),
            title = "Singapore",
            snippet = "Marker in Singapore"
        )
    }
}

//@Preview
//@Composable
//fun DefaultPreview() {
//    ConvoyTheme {
//        ConvoyApp()
//    }
//}
