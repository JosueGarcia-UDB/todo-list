package sv.edu.udb.todolist

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat

class TaskAdapter(context: Context, tasks: List<Task>, private val listener: OnTaskDeleteListener) :
    ArrayAdapter<Task>(context, 0, tasks) {

    interface OnTaskDeleteListener {
        fun onTaskDelete(task: Task)
        // Nueva función para notificar el cambio de estado
        fun onTaskCheckedChanged(task: Task)
        fun onTaskEdit(task: Task) // Nuevo método para editar tarea
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(R.layout.list_item_task, parent, false)
        }

        val task = getItem(position)

        val taskNameTextView = itemView!!.findViewById<TextView>(R.id.taskNameTextView)
        val taskDetailsTextView = itemView.findViewById<TextView>(R.id.taskDetailsTextView)
        val taskCheckBox = itemView.findViewById<CheckBox>(R.id.taskCheckBox)
        val deleteTaskButton = itemView.findViewById<ImageButton>(R.id.deleteTaskButton)
        val editTaskButton = itemView.findViewById<ImageButton>(R.id.editTaskButton)

        taskNameTextView.text = task?.name
        taskDetailsTextView.text = "${task?.category} - ${task?.importance}"

        // Set background color based on importance
        val importanceColor = when (task?.importance) {
            "Importante" -> R.drawable.list_item_border_red
            "Algo importante" -> R.drawable.list_item_border_yellow
            else -> R.drawable.list_item_border_gray
        }
        itemView.setBackgroundResource(importanceColor)

        // Handle checkbox state
        taskCheckBox.isChecked = task?.isChecked ?: false
        taskNameTextView.paintFlags = if (taskCheckBox.isChecked) {
            taskNameTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            taskNameTextView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        // Evitar múltiples listeners
        taskCheckBox.setOnCheckedChangeListener(null)
        taskCheckBox.isChecked = task?.isChecked ?: false
        taskCheckBox.setOnCheckedChangeListener { _, isChecked ->
            task?.isChecked = isChecked
            taskNameTextView.paintFlags = if (isChecked) {
                taskNameTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                taskNameTextView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
            if (task != null) {
                listener.onTaskCheckedChanged(task)
            }
        }

        // Handle delete button click
        deleteTaskButton.setOnClickListener {
            if (task != null) {
                listener.onTaskDelete(task)
            }
        }

        // Handle edit button click
        editTaskButton.setOnClickListener {
            if (task != null) {
                listener.onTaskEdit(task)
            }
        }

        return itemView
    }
}