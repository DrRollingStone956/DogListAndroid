package com.example.doglist.data.theme.ui.screens.DogDetails


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.doglist.model.DogPhoto


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DogDetailsScreen(
    route: DogPhoto,
    uiState: DogDetailsViewModel.UiState,
    retryAction: () -> Unit,
    navController: NavController,
    onDeleteDog: (String) -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Details")
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        onDeleteDog(route.message)
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { inner ->
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(inner)){
            Text(
                text = route.message,
                fontSize = 32.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            ContentScreen(uiState, retryAction)
        }
    }
}

@Composable
fun ContentScreen(uiState: DogDetailsViewModel.UiState, retryAction: () -> Unit, modifier: Modifier = Modifier) {
    when(uiState) {
        is DogDetailsViewModel.UiState.Loading -> LoadingScreen(
            modifier = modifier
        )
        is DogDetailsViewModel.UiState.Error -> ErrorScreen(
            retryAction = retryAction, modifier = modifier
        )
        is DogDetailsViewModel.UiState.Success -> {
            AsyncImage(
                model = ImageRequest.Builder(context = LocalContext.current)
                    .data(uiState.photo.message)
                    .crossfade(true)
                    .build(),
                contentDescription = null
            )
        }

    }
}

@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    CircularProgressIndicator(
        modifier = Modifier.width(64.dp),
        color = MaterialTheme.colorScheme.secondary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
    )
}

@Composable
fun ErrorScreen(retryAction: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            imageVector = Icons.Default.Warning, contentDescription = ""
        )
        Text(text = "Failed", modifier = Modifier.padding(16.dp))
        Button(onClick = retryAction) {
            Image(imageVector = Icons.Default.Refresh, contentDescription = null)
        }
    }
}