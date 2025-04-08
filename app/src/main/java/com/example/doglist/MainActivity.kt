package com.example.doglist


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.*
import coil3.compose.AsyncImage
import com.example.doglist.data.theme.color.DogListTheme
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.GET

interface DogApiService {
    @GET("breeds/image/random")
    suspend fun getRandomDogImage(): DogApiResponse
}

@Serializable
data class DogApiResponse(
    val message: String,
    val status: String
)

data class Dog(val name: String, val breed: String, val imageUrl: String? = null)

class DogViewModel : ViewModel() {
    var dogList = mutableStateListOf(
        Dog("Reks", "German Shepherd")
    )

    private val _randomDogImage = MutableStateFlow<String?>(null)
    val randomDogImage: StateFlow<String?> = _randomDogImage

    fun fetchRandomDogImage() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getRandomDogImage()
                _randomDogImage.value = response.message
            } catch (e: Exception) {

                _randomDogImage.value = null
            }
        }
    }
}

object RetrofitClient {
    private const val BASE_URL = "https://dog.ceo/api/"

    val apiService: DogApiService by lazy {
        val contentType = "application/json".toMediaType()
        val json = Json {
            ignoreUnknownKeys = true
        }

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(DogApiService::class.java)
    }
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
        composable("dogAdd") { DogAddScreen (navController, dogViewModel)}
        composable("settings") { SettingsScreen(navController) }
        composable("profile") { ProfileScreen(navController) }
        composable("dogDetail/{dogName}") { backStackEntry ->
            val dogName = backStackEntry.arguments?.getString("dogName") ?: ""
            DogDetailScreen( navController, dogName ,dogViewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DogListScreen(navController: NavController, dogViewModel: DogViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var likedDogs by remember { mutableStateOf(setOf<String>()) }
    val alert = remember { mutableStateOf("") }
    var searchResult by remember { mutableStateOf<List<Dog>>(emptyList()) }
    val dogListToShow = searchResult
    val sortedDogList = dogListToShow.sortedWith { dog1, dog2 ->
        when {
            likedDogs.contains(dog1.name) && !likedDogs.contains(dog2.name) -> -1
            !likedDogs.contains(dog1.name) && likedDogs.contains(dog2.name) -> 1
            else -> dog1.name.compareTo(dog2.name)
        }
    }

    val randomDogImage by dogViewModel.randomDogImage.collectAsState()

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
                    searchResult = dogViewModel.dogList.filter {
                        it.name.contains(searchQuery, ignoreCase = true) ||
                                it.breed.contains(searchQuery, ignoreCase = true)
                    }
                    alert.value = ""
                },
                navController = navController
            )
            Text(
                text = "\uD83D\uDC15: ${dogViewModel.dogList.size} \u2764: ${likedDogs.size}",
                fontSize = 16.sp,
                modifier = Modifier.padding(16.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                items(sortedDogList) { dog ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (randomDogImage != null) {
                            AsyncImage(
                                model = randomDogImage,
                                contentDescription = "Dog Image",
                                modifier = Modifier
                                    .size(50.dp)
                                    .padding(end = 8.dp),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text("Loading random dog image...", modifier = Modifier.size(50.dp))
                        }
                        DogItem(
                            dog = dog,
                            isLiked = likedDogs.contains(dog.name),
                            onLikeClick = {
                                likedDogs = if (likedDogs.contains(dog.name)) likedDogs - dog.name else likedDogs + dog.name
                            },
                            onDeleteClick = {
                                dogViewModel.dogList.remove(dog)
                                likedDogs = likedDogs - dog.name
                            },
                            navController = navController
                        )
                    }
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
fun DogItem(
    dog: Dog,
    isLiked: Boolean,
    onLikeClick: () -> Unit,
    onDeleteClick: () -> Unit,
    navController: NavController
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("dogDetail/${dog.name}") }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = dog.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = dog.breed, fontSize = 14.sp)
            }
            Row {
                IconButton(onClick = onLikeClick) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like"
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@Composable
fun SearchBar(searchQuery: String, onQueryChange: (String) -> Unit, navController: NavController) {
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
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = { navController.navigate("dogAdd") }, modifier = Modifier.padding(start = 8.dp)) {
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
fun DogDetailScreen(
    navController: NavController,
    dogName: String,
    dogViewModel: DogViewModel
) {
    val dog = dogViewModel.dogList.find { it.name == dogName }
    LaunchedEffect(true) {
        dogViewModel.fetchRandomDogImage()
    }
    val randomDogImage by dogViewModel.randomDogImage.collectAsState()
    if (dog == null) {
        Text("Dog not found")
        return
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Detale")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        dogViewModel.dogList.remove(dog)
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (randomDogImage != null) {
                AsyncImage(
                    model = randomDogImage,
                    contentDescription = "Dog Image",
                    modifier = Modifier.size(200.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text("Loading random dog image...")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "${dog.name}",
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "${dog.breed}",
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DogAddScreen(
    navController: NavController,
    dogViewModel: DogViewModel
) {
    var name by remember { mutableStateOf("") }
    var race by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(true) {
        dogViewModel.fetchRandomDogImage()
    }

    val randomDogImage by dogViewModel.randomDogImage.collectAsState()
    val newDog = Dog(name, race, randomDogImage)
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Dodaj Psa", fontSize = 20.sp)
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
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            if (randomDogImage != null) {
                AsyncImage(
                    model = randomDogImage,
                    contentDescription = "Dog Image",
                    modifier = Modifier.size(200.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text("Loading random dog image...")
            }
            Spacer(modifier = Modifier.height(40.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(20.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Imie") }
                )
            }
            Spacer(modifier = Modifier.height(15.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(20.dp)
            ) {
                OutlinedTextField(
                    value = race,
                    onValueChange = { race = it },
                    label = { Text("Rasa") }
                )
            }
            Spacer(modifier = Modifier.height(5.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(20.dp)
            ) {
                Text(
                    text = "Add",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clickable {
                            if (name.isNotBlank() && race.isNotBlank()) {
                                val newDog = Dog(name, race)
                                dogViewModel.dogList.add(newDog)
                                name = ""
                                race = ""
                            }
                        }
                )
            }
        }
    }
}