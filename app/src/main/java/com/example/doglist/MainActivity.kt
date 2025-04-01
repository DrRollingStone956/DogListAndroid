package com.example.doglist


import androidx.compose.runtime.mutableStateListOf
import android.os.Bundle
import androidx.compose.foundation.lazy.items
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.doglist.ui.theme.DogListTheme

var errorsearch = Color.Unspecified

class DogViewModel : ViewModel() {
    var dogList = mutableStateListOf("Reks", "Azor", "Latka")
}

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
    val dogViewModel: DogViewModel = viewModel()

    NavHost(navController = navController, startDestination = "dogList") {
        composable("dogList") { DogListScreen(navController, dogViewModel) }
        composable("settings") { SettingsScreen(navController) }
        composable("profile") { ProfileScreen(navController) }
        composable("dogDetail/{dogName}") { backStackEntry ->
            val dogName = backStackEntry.arguments?.getString("dogName") ?: ""
            DogDetailScreen(dogName, navController, dogViewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DogListScreen(navController: NavController, dogViewModel: DogViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var likedDogs by remember { mutableStateOf(setOf<String>()) }
    val alert = remember { mutableStateOf("") }
    var searchResult by remember { mutableStateOf<List<String>>(emptyList()) }
    val dogListToShow = if (isSearching) searchResult else dogViewModel.dogList
    val sortedDogList = dogListToShow.sortedWith { dog1, dog2 ->
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
                onQueryChange = {
                    searchQuery = it
                    if (it.isBlank()) {
                        isSearching = false
                        searchResult = dogViewModel.dogList
                    }
                    errorsearch = Color.Unspecified
                    alert.value = ""
                   },
                onAddDog = {
                    if (it.isNotBlank() && !dogViewModel.dogList.contains(it)) {
                        dogViewModel.dogList.add(it)
                        searchQuery = ""
                    } else if (dogViewModel.dogList.contains(it)) {
                        errorsearch = Color.Red
                        alert.value = "piesek juz zostal dodany"
                    }
                },
                onSearchClick = {
                    isSearching = true
                    searchResult = if (searchQuery.isBlank()) dogViewModel.dogList else
                        dogViewModel.dogList.filter { it.contains(searchQuery, ignoreCase = true) }
                }
            )
            Text(
                text = "\uD83D\uDC15: ${dogViewModel.dogList.size} \u2764: ${likedDogs.size}",
                fontSize = 16.sp,
                modifier = Modifier.padding(16.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                items(sortedDogList) { dog ->
                    DogItem(
                        dogName = dog,
                        isLiked = likedDogs.contains(dog),
                        onLikeClick = {
                            likedDogs = if (likedDogs.contains(dog)) likedDogs - dog else likedDogs + dog
                        },
                        onDeleteClick = {
                            dogViewModel.dogList.remove(dog)
                            likedDogs = likedDogs - dog
                        },
                        navController = navController
                    )
                }
            }
            Text(
                text = alert.value,
                modifier = Modifier.clickable { alert.value = "" }
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
            Icon(Icons.Default.Search, contentDescription = "Search")
        }
        IconButton(onClick = { onAddDog(searchQuery) }) {
            Icon(Icons.Default.Add, contentDescription = "Add")
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
fun DogDetailScreen(dogName: String, navController: NavController, dogViewModel: DogViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Detale", fontSize = 20.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        dogViewModel.dogList.remove(dogName)
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xff65558f), Color(0xf0eeb6e8))
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
            Text(text = dogName, fontSize = 18.sp, modifier = Modifier.padding(top = 8.dp))
        }
    }
}
