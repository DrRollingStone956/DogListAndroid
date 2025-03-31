package com.example.doglist

import android.os.Bundle
import android.widget.TextView

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.example.doglist.ui.theme.DogListTheme
var errorsearch = Color.Unspecified

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            DogListTheme {

                DogApp()
            }
        }
    }

}

@Composable
fun DogApp() {
    val navController = rememberNavController()

    // Navigation setup
    NavHost(navController = navController, startDestination = "dogList") {
        composable("dogList") { DogListScreen(navController) }
        composable("settings") { SettingsScreen(navController) }
        composable("profile") { ProfileScreen(navController) }
        composable("dogDetail/{dogName}") { backStackEntry ->
            val dogName = backStackEntry.arguments?.getString("dogName") ?: ""
            DogDetailScreen(dogName, navController)
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DogListScreen(navController: NavController) {
    var dogList by remember { mutableStateOf(listOf("Reks","Azor","Łatka")) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var likedDogs by remember { mutableStateOf(setOf<String>()) }
    val alert = remember { mutableStateOf("") }
    val filteredDogList = if (isSearching) {
        dogList.filter { it.contains(searchQuery, ignoreCase = true) }
    } else {
        dogList
    }

    val sortedDogList = filteredDogList.sortedWith { dog1, dog2 ->
        when {
            likedDogs.contains(dog1) && !likedDogs.contains(dog2) -> -1
            !likedDogs.contains(dog1) && likedDogs.contains(dog2) -> 1
            else -> dog1.compareTo(dog2)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Doggos", fontSize = 20.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            SearchBar(
                searchQuery = searchQuery,
                onQueryChange = { searchQuery = it },
                onAddDog = {
                    if (it.isNotBlank() && !dogList.contains(it)) {

                        dogList = dogList + it
                        searchQuery = ""
                        errorsearch = Color.Unspecified


                    }else if(dogList.contains(it))
                        errorsearch = Color.Red
                        alert.value = "Piesek jest juz w bazie"
                },
                onSearchClick = {

                    isSearching = true
                }
            )

            Text(
                text = "\uD83D\uDC15: ${dogList.size} \u2764: ${likedDogs.size}",
                fontSize = 16.sp,
                modifier = Modifier.padding(16.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {

                if (sortedDogList.isEmpty()) {
                    Text("No matching dogs found", fontSize = 18.sp)
                } else {
                    sortedDogList.forEach { dog ->
                        DogItem(
                            dogName = dog,
                            isLiked = likedDogs.contains(dog),
                            onLikeClick = {
                                likedDogs = if (likedDogs.contains(dog)) {
                                    likedDogs - dog
                                } else {
                                    likedDogs + dog
                                }
                            },
                            onDeleteClick = {
                                dogList = dogList.filterNot { it == dog }
                                likedDogs = likedDogs - dog
                            }, navController = navController
                        )
                    }
                }
            }
            Text(

                modifier = Modifier.clickable { alert.value="" },
                text = alert.value
            )
        }
    }
}

@Composable
fun DogItem(dogName: String, isLiked: Boolean, onLikeClick: () -> Unit, onDeleteClick: () -> Unit, navController: NavController) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = "\uD83D\uDC15",
            fontSize = 24.sp,
            modifier = Modifier
                .padding(end = 10.dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xff65558f),
                            Color(0xf0eeb6e8)
                        )

                    )
                )
        )

        Text(
            text = dogName,
            fontSize = 18.sp,
            modifier = Modifier
                .weight(1f)
                .clickable {

                    navController.navigate("dogDetail/$dogName")
                }
        )

        IconButton(onClick = onLikeClick) {
            val icon = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder
            Icon(
                icon,
                contentDescription = "Fav",
                tint = Color(0xf0eeb6e8)

            )
        }

        IconButton(onClick = onDeleteClick) {
            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red)
        }
    }
}

@Composable
fun SearchBar(searchQuery: String, onQueryChange: (String) -> Unit, onAddDog: (String) -> Unit, onSearchClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            placeholder = { Text("Poszukaj lub dodaj pieska \uD83D\uDC15") },
            modifier = Modifier.weight(1f).background(errorsearch)
        )
        IconButton(onClick = onSearchClick) {
            Icon(Icons.Default.Search, contentDescription = "Wyszukaj")
        }
        IconButton(onClick = { onAddDog(searchQuery) }) {
            Icon(Icons.Default.Add, contentDescription = "Dodaj")
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Ustawienia", fontSize = 20.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Profil", fontSize = 20.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(Color.Gray, shape = CircleShape)
            )

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Jan Zizka",
                fontSize = 18.sp
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DogDetailScreen(dogName: String, navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Detale",
                            fontSize = 20.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Delete action */ }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    )
    { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Box(
                modifier = Modifier
                    .size(160.dp)
                    .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xff65558f),
                            Color(0xf0eeb6e8)
                        ),
                        )
                    )
                    .padding(16.dp)

            ) {
                Text(
                    text = "\uD83D\uDC15",
                    fontSize = 20.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            Text(
                text = "$dogName",
                fontSize = 18.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }

}

