package com.example.trannguyenquyen_qe170062

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trannguyenquyen_qe170062.databinding.ActivityMainBinding
import com.example.trannguyenquyen_qe170062.databinding.DialogAddStudentBinding
import com.example.trannguyenquyen_qe170062.model.Student
import com.example.trannguyenquyen_qe170062.ui.adapter.StudentAdapter
import com.example.trannguyenquyen_qe170062.ui.viewmodel.StudentViewModel
import com.example.trannguyenquyen_qe170062.ui.viewmodel.StudentViewModelFactory
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: StudentViewModel
    private lateinit var adapter: StudentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        setupSwipeToDelete()
        
        Log.d(TAG, "MainActivity created")
    }

    private fun setupViewModel() {
        val repository = (application as MyApplication).repository
        val factory = StudentViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[StudentViewModel::class.java]
        Log.d(TAG, "ViewModel setup completed")
    }

    private fun setupRecyclerView() {
        adapter = StudentAdapter(
            onStudentClick = { student ->
                Toast.makeText(this, "Đã chọn: ${student.first_name} ${student.last_name}", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Student clicked: ${student.id}")
            },
            onStudentDelete = { student, _ ->
                // Show confirmation dialog before deletion
                AlertDialog.Builder(this)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc muốn xóa sinh viên ${student.first_name} ${student.last_name}?")
                    .setPositiveButton("Xóa") { _, _ ->
                        deleteStudent(student)
                    }
                    .setNegativeButton("Hủy", null)
                    .show()
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
        Log.d(TAG, "RecyclerView setup completed")
    }

    private fun setupObservers() {
        // Observe students from ViewModel
        viewModel.students.observe(this) { students ->
            Log.d(TAG, "Students list updated: ${students.size} students")
            adapter.submitList(students)
            
            // Show empty view if list is empty
            if (students.isEmpty()) {
                binding.emptyView.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.emptyView.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
            }
        }

        viewModel.loading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.swipeRefreshLayout.isRefreshing = isLoading
            Log.d(TAG, "Loading state: $isLoading")
        }

        viewModel.error.observe(this) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                Log.e(TAG, "Error: $it")
                viewModel.clearError()
            }
        }
        
        viewModel.isSortedByName.observe(this) { isSorted ->
            val buttonText = if (isSorted) "Bỏ sắp xếp" else "Sắp xếp theo tên"
            binding.sortButton.text = buttonText
            Log.d(TAG, "Sort button text updated to: $buttonText")
        }
        
        viewModel.newStudentsCount.observe(this) { count ->
            if (count > 0) {
                Snackbar.make(
                    binding.root,
                    "Đã thêm $count sinh viên mới từ API",
                    Snackbar.LENGTH_LONG
                ).show()
                Log.d(TAG, "$count new students added from API")
            }
        }
    }

    private fun setupClickListeners() {
        binding.addButton.setOnClickListener {
            showAddStudentDialog()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            showRefreshConfirmationDialog()
        }
        
        binding.sortButton.setOnClickListener {
            Log.d(TAG, "Sort button clicked, toggling sort state")
            viewModel.toggleSortByName()
            val isSorted = viewModel.isSortedByName.value ?: false
            val message = if (isSorted) "Danh sách đã được sắp xếp theo tên" else "Đã bỏ sắp xếp"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showRefreshConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Tải dữ liệu từ API")
            .setMessage("Dữ liệu từ API sẽ được thêm vào danh sách hiện tại. Bạn có muốn tiếp tục?")
            .setPositiveButton("Đồng ý") { _, _ ->
                viewModel.fetchStudents()
                Toast.makeText(this, "Đang tải dữ liệu từ API...", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Manual refresh triggered")
            }
            .setNegativeButton("Hủy") { _, _ ->
                binding.swipeRefreshLayout.isRefreshing = false
            }
            .show()
    }

    private fun setupSwipeToDelete() {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: androidx.recyclerview.widget.RecyclerView,
                viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
                target: androidx.recyclerview.widget.RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(
                viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
                direction: Int
            ) {
                val position = viewHolder.adapterPosition
                if (position != androidx.recyclerview.widget.RecyclerView.NO_POSITION) {
                    val studentToDelete = adapter.currentList[position]

                    // Show confirmation dialog before deletion
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc muốn xóa sinh viên ${studentToDelete.first_name} ${studentToDelete.last_name}?")
                        .setPositiveButton("Xóa") { _, _ ->
                            adapter.removeStudent(position) // Remove from adapter
                            deleteStudent(studentToDelete)  // Handle deletion logic
                        }
                        .setNegativeButton("Hủy") { _, _ ->
                            adapter.notifyItemChanged(position) // Reset swipe action
                        }
                        .setCancelable(false)
                        .show()
                }
            }

        }

        ItemTouchHelper(swipeHandler).attachToRecyclerView(binding.recyclerView)
        Log.d(TAG, "Swipe-to-delete setup completed")
    }
    
    private fun deleteStudent(student: Student) {
        Log.d(TAG, "Deleting student: ${student.id}")
        viewModel.removeStudent(student)
        
        Snackbar.make(
            binding.root,
            "Đã xóa ${student.first_name} ${student.last_name}",
            Snackbar.LENGTH_LONG
        ).setAction("Hoàn tác") {
            viewModel.addStudent(student)
            Toast.makeText(this@MainActivity, "Đã hoàn tác", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Undo delete for student: ${student.id}")
        }.show()
    }

    private fun showAddStudentDialog() {
        val dialogBinding = DialogAddStudentBinding.inflate(LayoutInflater.from(this))
        
        AlertDialog.Builder(this)
            .setTitle("Thêm Sinh Viên Mới")
            .setView(dialogBinding.root)
            .setPositiveButton("Thêm") { _, _ ->
                val firstName = dialogBinding.editFirstName.text.toString().trim()
                val lastName = dialogBinding.editLastName.text.toString().trim()
                val email = dialogBinding.editEmail.text.toString().trim()
                
                if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
                    Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                val newStudent = Student(
                    id = System.currentTimeMillis().toInt(),
                    first_name = firstName,
                    last_name = lastName,
                    email = email,
                    avatar = "https://reqres.in/img/faces/${(1..12).random()}-image.jpg"
                )
                
                Log.d(TAG, "Adding new student from dialog: $newStudent")
                viewModel.addStudent(newStudent)
                Toast.makeText(this, "Đã thêm sinh viên mới", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun addSampleStudent() {
        val newStudent = Student(
            id = System.currentTimeMillis().toInt(), // Generate unique ID
            email = "sinhvien${System.currentTimeMillis()}@example.com",
            first_name = "Sinh viên",
            last_name = "Mới",
            avatar = "https://reqres.in/img/faces/1-image.jpg"
        )
        
        Log.d(TAG, "Adding new sample student with ID: ${newStudent.id}")
        viewModel.addStudent(newStudent)
        Toast.makeText(this, "Đã thêm sinh viên mẫu mới", Toast.LENGTH_SHORT).show()
    }
}