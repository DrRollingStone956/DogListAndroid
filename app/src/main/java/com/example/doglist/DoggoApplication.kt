package com.example.doglist

import android.app.Application
import com.example.doglist.data.theme.AppContainer

class DoggoApplication : Application() {

    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
    }
}