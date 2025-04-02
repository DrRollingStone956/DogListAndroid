package com.example.doglist.data.theme

import com.example.doglist.data.network.DogsService
import com.example.doglist.model.DogPhoto


interface DogsPhotosRepository {
    suspend fun getRandomDogImage(): DogPhoto
}

class NetworkDogsPhotosRepository(
    private val dogsService: DogsService,
) : DogsPhotosRepository {

    override suspend fun getRandomDogImage(): DogPhoto = dogsService.getRandomDogImage()
}