package com.example.trannguyenquyen_qe170062.model

import java.io.Serializable
import com.example.trannguyenquyen_qe170062.data.db.StudentEntity

data class Student(
    val id: Int = 0,
    val email: String = "",
    val first_name: String = "",
    val last_name: String = "",
    val avatar: String = ""
) : Serializable {
    fun toEntity(): StudentEntity {
        return StudentEntity(
            id = id,
            email = email,
            firstName = first_name,
            lastName = last_name,
            avatar = avatar
        )
    }
    
    override fun toString(): String {
        return "Student(id=$id, email='$email', first_name='$first_name', last_name='$last_name')"
    }
} 