package edu.temple.convoy

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.LocationServices
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

        setContent {
            ConvoyTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ConvoyApp(this@MainActivity)
                }
            }
        }
    }
}

@Composable
fun ConvoyApp(context: Context) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "initialScreen") {
        composable("initialScreen") {
            InitialScreen(navController = navController)
        }
        composable("createAccountScreen") {
            RegisterScreen(context = context, navController = navController)
        }
        composable("loginScreen") {
            LoginScreen(context = context, navController = navController)
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
fun RegisterScreen(context: Context, navController: NavController) {
    var username by remember {
        mutableStateOf("")
    }
    var firstname by remember {
        mutableStateOf("")
    }
    var lastname by remember {
        mutableStateOf("")
    }
    var password by remember {
        mutableStateOf("")
    }
    var confirmPassword by remember {
        mutableStateOf("")
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Register")
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text(text = "Username") }
        )
        OutlinedTextField(
            value = firstname,
            onValueChange = { firstname = it },
            label = { Text(text = "First Name") }
        )
        OutlinedTextField(
            value = lastname,
            onValueChange = { lastname = it },
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
            if (username.isEmpty() || firstname.isEmpty() || lastname.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(
                    context,
                    "Please fill in all fields",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (password != confirmPassword) {
                Toast.makeText(
                    context,
                    "Passwords do not match",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Helper.api.createAccount(
                    context,
                    User(
                        username,
                        firstname,
                        lastname
                    ),
                    password
                ) { response ->
                    if (Helper.api.isSuccess(response)) {
                        Helper.user.saveSessionData(
                            context,
                            response.getString("session_key")
                        )
                        Helper.user.saveUser(
                            context, User(
                                username,
                                firstname,
                                lastname
                            )
                        )
                    } else {
                        Toast.makeText(
                            context,
                            Helper.api.getErrorMessage(response),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                navController.navigate("mainScreen")
            }

        }) {
            Text(text = "Register")
        }
    }

}

@Composable
fun LoginScreen(context: Context, navController: NavController) {
    var username by remember {
        mutableStateOf("")
    }
    var password by remember {
        mutableStateOf("")
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Login")
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
                Toast.makeText(
                    context,
                    "Please fill in all fields",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Helper.api.login(
                    context,
                    User(
                        username,
                        null,
                        null
                    ),
                    password
                ) { response ->
                    if (Helper.api.isSuccess(response)) {
                        Helper.user.saveSessionData(
                            context,
                            response.getString("session_key")
                        )
                        Helper.user.saveUser(
                            context, User(
                                username,
                                null,
                                null
                            )
                        )
                    } else {
                        Toast.makeText(
                            context,
                            Helper.api.getErrorMessage(response),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    navController.navigate("mainScreen")
                }
            }
        }) {
            Text(text = "Login")
        }
    }
}

@Composable
fun MainScreen(navController: NavController) {
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
    )
}

//@Preview
//@Composable
//fun DefaultPreview() {
//    ConvoyTheme {
//        ConvoyApp()
//    }
//}
