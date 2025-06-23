package com.example.trannguyenquyen_qe170062.data.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY sort_order ASC")
    fun getAllStudents(): LiveData<List<StudentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(student: StudentEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(students: List<StudentEntity>)

    @Update
    suspend fun update(student: StudentEntity)
    
    @Query("DELETE FROM students WHERE id = :studentId")
    suspend fun delete(studentId: Int)
    
    @Query("DELETE FROM students")
    suspend fun deleteAll()
    
    // Thêm truy vấn để lấy sinh viên theo thứ tự tên
    @Query("SELECT * FROM students ORDER BY firstName || ' ' || lastName ASC")
    fun getAllStudentsSortedByName(): LiveData<List<StudentEntity>>
    
    // Lấy danh sách ID sinh viên hiện có
    @Query("SELECT id FROM students")
    suspend fun getAllStudentIds(): List<Int>
} 