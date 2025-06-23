package com.example.trannguyenquyen_qe170062

import android.app.Application
import com.example.trannguyenquyen_qe170062.data.db.AppDatabase
import com.example.trannguyenquyen_qe170062.data.repository.StudentRepository

class MyApplication : Application() {
    private val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { StudentRepository(database.studentDao()) }
} 