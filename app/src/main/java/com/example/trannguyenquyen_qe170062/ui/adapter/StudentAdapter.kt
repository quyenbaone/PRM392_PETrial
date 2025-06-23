package com.example.trannguyenquyen_qe170062.ui.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.example.trannguyenquyen_qe170062.databinding.ItemStudentBinding
import com.example.trannguyenquyen_qe170062.model.Student

class StudentAdapter(
    private val onStudentClick: (Student) -> Unit,
    private val onStudentDelete: (Student, Int) -> Unit
) : ListAdapter<Student, StudentAdapter.StudentViewHolder>(StudentDiffCallback()) {
    private val TAG = "StudentAdapter"
    
    // Keep a mutable copy of the current list for operations
    private var studentsList = mutableListOf<Student>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        Log.d(TAG, "Creating new ViewHolder")
        val binding = ItemStudentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StudentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        Log.d(TAG, "Binding ViewHolder at position $position")
        holder.bind(getItem(position))
    }
    
    override fun submitList(list: List<Student>?) {
        super.submitList(list)
        list?.let {
            Log.d(TAG, "Submitting list with ${list.size} items")
            studentsList = it.toMutableList()
        }
    }

    fun removeStudent(position: Int): Student {
        if (position < 0 || position >= studentsList.size) {
            Log.e(TAG, "Invalid position: $position, list size: ${studentsList.size}")
            throw IndexOutOfBoundsException("Invalid position: $position")
        }
        
        Log.d(TAG, "Removing student at position $position")
        val student = studentsList[position]
        val newList = studentsList.toMutableList()
        newList.removeAt(position)
        studentsList = newList
        submitList(newList)
        return student
    }

    inner class StudentViewHolder(
        private val binding: ItemStudentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    Log.d(TAG, "Item clicked at position $position")
                    onStudentClick(getItem(position))
            }
        }

            binding.buttonDelete.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    Log.d(TAG, "Delete button clicked at position $position")
                    val student = getItem(position)
                    onStudentDelete(student, position)
                }
            }
        }

        fun bind(student: Student) {
            Log.d(TAG, "Binding student: ${student.id}")
            binding.textName.text = "${student.first_name} ${student.last_name}"
            binding.textEmail.text = student.email
            binding.textId.text = "ID: ${student.id}"

            // Load avatar image with Glide
            Glide.with(binding.imageAvatar)
                .load(student.avatar)
                .apply(RequestOptions()
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image))
                .transform(CircleCrop())
                .into(binding.imageAvatar)
        }
    }

    class StudentDiffCallback : DiffUtil.ItemCallback<Student>() {
        override fun areItemsTheSame(oldItem: Student, newItem: Student): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Student, newItem: Student): Boolean {
            return oldItem == newItem
        }
    }
} 