package com.example.trannguyenquyen_qe170062.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.trannguyenquyen_qe170062.model.Student

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey val id: Int,
    val email: String,
    val firstName: String,
    val lastName: String,
    val avatar: String,
    val sort_order: Int = 0
) {
    fun toStudent(): Student {
        return Student(
            id = id,
            email = email,
            first_name = firstName,
            last_name = lastName,
            avatar = avatar
        )
    }
    
    override fun toString(): String {
        return "StudentEntity(id=$id, email='$email', firstName='$firstName', lastName='$lastName', sort_order=$sort_order)"
    }
    
    companion object {
        fun fromStudent(student: Student, sortOrder: Int = 0): StudentEntity {
            return StudentEntity(
                id = student.id,
                email = student.email,
                firstName = student.first_name,
                lastName = student.last_name,
                avatar = student.avatar,
                sort_order = sortOrder
            )
        }
    }
} 