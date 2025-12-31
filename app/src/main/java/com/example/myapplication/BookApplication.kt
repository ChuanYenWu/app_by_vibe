package com.example.myapplication

import android.app.Application
import com.example.myapplication.data.AppContainer
import com.example.myapplication.data.AppDataContainer

class BookApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppDataContainer(this)
    }
}
