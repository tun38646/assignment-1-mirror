package edu.temple.convoy

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import edu.temple.convoy.ui.theme.ConvoyTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    var serviceIntent: Intent? = null
    val convoyViewModel: ConvoyViewModel by lazy {
        ViewModelProvider(this)[ConvoyViewModel::class.java]
    }

    var locationHandler = object : Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) {
            convoyViewModel.setLocation(msg.obj as LatLng)
        }
    }

    var serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, iBinder: IBinder) {
            (iBinder as LocationService.LocationBinder).setHandler(locationHandler)
        }

        override fun onServiceDisconnected(componentName: ComponentName) {}
    }

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

        createNotificationChannel()
        serviceIntent = Intent(this, LocationService::class.java)

//        convoyViewModel.getConvoyId().observe(this) {
//            if (!it.isNullOrEmpty())
//                supportActionBar?.title = "Convoy - $it"
//            else
//                supportActionBar?.title = "Convoy"
//        }

        Helper.user.getConvoyId(this)?.run {
            convoyViewModel.setConvoyId(this)
            startLocationService()
        }

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), 1
            )
        }
    }

    private fun createNotificationChannel() {
        val channel =
            NotificationChannel("default", "Active Convoy", NotificationManager.IMPORTANCE_HIGH)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun startLocationService() {
        serviceIntent?.let { bindService(it, serviceConnection, BIND_AUTO_CREATE) };
        startService(serviceIntent)
    }

    private fun stopLocationService() {
        unbindService(serviceConnection)
        stopService(serviceIntent)
    }

    @Composable
    fun ConvoyApp(context: Context) {
        var startDestination = "initialScreen"
        Helper.user.getSessionKey(context)?.run {
            startDestination = "mainScreen"
        }

        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = startDestination) {
            composable("initialScreen") {
                InitialScreen(context = context, navController = navController)
            }
            composable("createAccountScreen") {
                RegisterScreen(context = context, navController = navController)
            }
            composable("loginScreen") {
                LoginScreen(context = context, navController = navController)
            }
            composable("mainScreen") {
                MainScreen(context = context, navController = navController)
            }
        }
    }

    @Composable
    fun InitialScreen(context: Context, navController: NavController) {
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainScreen(context: Context, navController: NavController) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    colors = topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    title = {
                        Text(
                            text = "Convoy App",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    actions = {
                        IconButton(onClick = {
                            Helper.user.getSessionKey(context)?.let {
                                Helper.api.logout(
                                    context,
                                    User(
                                        Helper.user.get(context).username,
                                        null,
                                        null
                                    ),
                                    it
                                ) { response ->
                                    if (Helper.api.isSuccess(response)) {
                                        Helper.user.clearSessionData(context)
                                    } else {
                                        Toast.makeText(
                                            context,
                                            Helper.api.getErrorMessage(response),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    navController.navigate("loginScreen")
                                }
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = "Logout"
                            )
                        }
                    }
                )
            },
            floatingActionButton = {
                Column {
                    FloatingActionButton(onClick = {
                        Helper.api.createConvoy(
                            context, Helper.user.get(context), Helper.user.getSessionKey(context)!!
                        ) { response ->
                            if (Helper.api.isSuccess(response)) {
                                convoyViewModel.setConvoyId(response.getString("convoy_id"))
                                Helper.user.saveConvoyId(
                                    context,
                                    convoyViewModel.getConvoyId().value!!
                                )
                                startLocationService()
                            } else {
                                Toast.makeText(
                                    context,
                                    Helper.api.getErrorMessage(response),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Start Convoy")
                    }
                    FloatingActionButton(onClick = {
                        Log.d("Convoy Id", convoyViewModel.getConvoyId().value!!)
                        AlertDialog.Builder(context).setTitle("Close Convoy")
                            .setMessage("Are you sure you want to close the convoy?")
                            .setPositiveButton(
                                "Yes"
                            ) { _, _ ->
                                Helper.api.closeConvoy(
                                    context,
                                    Helper.user.get(context),
                                    Helper.user.getSessionKey(context)!!,
                                    convoyViewModel.getConvoyId().value!!
                                ) { response ->
                                    if (Helper.api.isSuccess(response)) {
                                        convoyViewModel.setConvoyId("")
                                        Helper.user.clearConvoyId(context)
                                        stopLocationService()
                                    } else
                                        Toast.makeText(
                                            context,
                                            Helper.api.getErrorMessage(response),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                }
                            }
                            .setNegativeButton("Cancel") { p0, _ -> p0.cancel() }
                            .show()
                    }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "End Convoy")
                    }

                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                MapComponent()
            }
        }
    }

    @SuppressLint("MissingPermission")
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
}

//@Preview
//@Composable
//fun DefaultPreview() {
//    ConvoyTheme {
//        ConvoyApp()
//    }
//}
