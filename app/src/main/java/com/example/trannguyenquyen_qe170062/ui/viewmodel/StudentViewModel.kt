package com.example.trannguyenquyen_qe170062.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trannguyenquyen_qe170062.data.repository.StudentRepository
import com.example.trannguyenquyen_qe170062.model.Student
import kotlinx.coroutines.launch

class StudentViewModel(
    private val repository: StudentRepository
) : ViewModel() {
    private val TAG = "StudentViewModel"

    // Lấy danh sách sinh viên từ repository
    val students: LiveData<List<Student>> = repository.allStudents

    // Lấy trạng thái sắp xếp từ repository
    val isSortedByName: LiveData<Boolean> = MutableLiveData<Boolean>().apply {
        value = repository.isSortedByName()
    }

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _newStudentsCount = MutableLiveData<Int>()
    val newStudentsCount: LiveData<Int> get() = _newStudentsCount

    init {
        fetchStudents()
    }

    // Chuyển đổi trạng thái sắp xếp
    fun toggleSortByName() {
        Log.d(TAG, "ViewModel toggling sort state")
        val newSortState = repository.toggleSortByName()
        (isSortedByName as MutableLiveData).value = newSortState
        Log.d(TAG, "Sort state toggled to: $newSortState")
    }

    fun fetchStudents() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            
            try {
                Log.d(TAG, "Fetching students from API")
                val result = repository.fetchStudentsFromApi()
                val newCount = result?.second ?: 0
                _newStudentsCount.value = newCount
                Log.d(TAG, "Fetch result: ${result?.first?.size ?: 0} students, $newCount new")
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching students", e)
                _error.value = "Lỗi khi tải dữ liệu từ mạng: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun addStudent(student: Student) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Adding student: $student")
                repository.insert(student)
            } catch (e: Exception) {
                Log.e(TAG, "Error adding student", e)
                _error.value = "Lỗi khi thêm sinh viên: ${e.message}"
            }
        }
    }

    fun removeStudent(student: Student) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Removing student: $student")
                repository.delete(student)
            } catch (e: Exception) {
                Log.e(TAG, "Error removing student", e)
                _error.value = "Lỗi khi xóa sinh viên: ${e.message}"
            }
        }
    }

    fun updateStudent(student: Student) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Updating student: $student")
                repository.update(student)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating student", e)
                _error.value = "Lỗi khi cập nhật sinh viên: ${e.message}"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
} 