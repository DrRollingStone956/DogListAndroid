package com.example.doglist.data.network

import com.example.doglist.model.DogPhoto
import retrofit2.http.GET

interface DogsService {

    @GET("image/random")
    suspend fun getRandomDogImage(): DogPhoto
}