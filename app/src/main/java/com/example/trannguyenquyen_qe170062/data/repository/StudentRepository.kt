package com.example.trannguyenquyen_qe170062.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.example.trannguyenquyen_qe170062.data.api.RetrofitInstance
import com.example.trannguyenquyen_qe170062.data.db.StudentDao
import com.example.trannguyenquyen_qe170062.data.db.StudentEntity
import com.example.trannguyenquyen_qe170062.model.Student
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StudentRepository(private val studentDao: StudentDao) {
    private val TAG = "StudentRepository"

    // Trạng thái sắp xếp
    private val _isSortedByName = MutableLiveData<Boolean>(false)
    
    // Lấy danh sách sinh viên dựa trên trạng thái sắp xếp
    private val _allStudents = MediatorLiveData<List<Student>>()
    val allStudents: LiveData<List<Student>> = _allStudents
    
    // Source variables to track active sources
    private var defaultSource: LiveData<List<StudentEntity>>? = null
    private var sortedSource: LiveData<List<StudentEntity>>? = null
    
    init {
        updateStudentSource()
    }
    
    private fun updateStudentSource() {
        // Remove existing sources if they exist
        if (defaultSource != null) {
            _allStudents.removeSource(defaultSource!!)
        }
        if (sortedSource != null) {
            _allStudents.removeSource(sortedSource!!)
        }
        
        if (_isSortedByName.value == true) {
            Log.d(TAG, "Getting sorted students from DAO")
            sortedSource = studentDao.getAllStudentsSortedByName()
            _allStudents.addSource(sortedSource!!) { entities ->
                _allStudents.value = entities.map { it.toStudent() }
            }
        } else {
            Log.d(TAG, "Getting default order students from DAO")
            defaultSource = studentDao.getAllStudents()
            _allStudents.addSource(defaultSource!!) { entities ->
                _allStudents.value = entities.map { it.toStudent() }
            }
        }
    }

    // Chuyển đổi trạng thái sắp xếp
    fun toggleSortByName(): Boolean {
        val currentValue = _isSortedByName.value ?: false
        val newValue = !currentValue
        Log.d(TAG, "Repository toggling sort state from $currentValue to $newValue")
        _isSortedByName.value = newValue
        updateStudentSource()
        return newValue
    }
    
    // Lấy trạng thái sắp xếp hiện tại
    fun isSortedByName(): Boolean {
        return _isSortedByName.value ?: false
    }

    // Fetch students from API and store in database
    // Trả về cặp (danh sách sinh viên, số lượng sinh viên mới)
    suspend fun fetchStudentsFromApi(): Pair<List<Student>, Int>? {
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.api.getStudents()
                Log.d(TAG, "API Response: $response")
                
                // Chuyển đổi dữ liệu API thành đối tượng Student
                val apiStudents = response.data.map { apiStudent ->
                    Student(
                        id = apiStudent.id,
                        email = apiStudent.email,
                        first_name = apiStudent.first_name,
                        last_name = apiStudent.last_name,
                        avatar = apiStudent.avatar
                    )
                }
                
                // Lấy danh sách sinh viên hiện tại từ cơ sở dữ liệu
                val existingStudentIds = studentDao.getAllStudentIds()
                Log.d(TAG, "Existing student IDs: $existingStudentIds")
                
                // Chỉ thêm các sinh viên chưa tồn tại trong cơ sở dữ liệu
                val newStudents = apiStudents.filter { apiStudent ->
                    !existingStudentIds.contains(apiStudent.id)
                }
                
                val newStudentsCount = newStudents.size
                Log.d(TAG, "Adding $newStudentsCount new students from API")
                
                // Thêm các sinh viên mới vào cơ sở dữ liệu
                newStudents.forEach { student ->
                    val entity = StudentEntity.fromStudent(student)
                    Log.d(TAG, "Inserting new API student: $entity")
                    studentDao.insert(entity)
                }
                
                // Trả về cặp (danh sách sinh viên, số lượng sinh viên mới)
                Pair(apiStudents, newStudentsCount)
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching students from API", e)
                null
            }
        }
    }

    // Insert a student
    suspend fun insert(student: Student) {
        withContext(Dispatchers.IO) {
            try {
                val entity = StudentEntity.fromStudent(student)
                Log.d(TAG, "Inserting student: $entity")
                studentDao.insert(entity)
            } catch (e: Exception) {
                Log.e(TAG, "Error inserting student", e)
                throw e
            }
        }
    }

    // Delete a student
    suspend fun delete(student: Student) {
        withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Deleting student with ID: ${student.id}")
                studentDao.delete(student.id)
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting student", e)
                throw e
            }
        }
    }

    // Update a student
    suspend fun update(student: Student) {
        withContext(Dispatchers.IO) {
            try {
                val entity = StudentEntity.fromStudent(student)
                Log.d(TAG, "Updating student: $entity")
                studentDao.update(entity)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating student", e)
                throw e
            }
        }
    }
} 