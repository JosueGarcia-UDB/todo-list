package sv.edu.udb.todolist

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AlertDialog
import android.app.DatePickerDialog
import androidx.activity.enableEdgeToEdge
import java.util.Calendar

class MainActivity : AppCompatActivity(), TaskAdapter.OnTaskDeleteListener {

    private lateinit var tasksListView: ListView
    private lateinit var newTaskEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var importanceSpinner: Spinner
    private lateinit var addTaskButton: ImageButton
    private lateinit var filterCategorySpinner: Spinner
    private lateinit var addDueDateButton: ImageButton
    private var dueDate: String? = null

    private val tasks = mutableListOf<Task>()
    private lateinit var adapter: TaskAdapter
    private lateinit var dbHelper: TaskDbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        enableEdgeToEdge()

        tasksListView = findViewById(R.id.tasksListView)
        newTaskEditText = findViewById(R.id.newTaskEditText)
        categorySpinner = findViewById(R.id.categorySpinner)
        importanceSpinner = findViewById(R.id.importanceSpinner)
        addTaskButton = findViewById(R.id.addTaskButton)
        filterCategorySpinner = findViewById(R.id.filterCategorySpinner)
        addDueDateButton = findViewById(R.id.addDueDateButton)

        dbHelper = TaskDbHelper(this)
        loadTasksFromDb()

        // Configurar el adaptador para la lista de tareas
        adapter = TaskAdapter(this, tasks, this)
        tasksListView.adapter = adapter

        // Configurar los spinners
        setupSpinners()
        setupFilterCategorySpinner()

        // Configurar el listener del botón para agregar tareas
        addTaskButton.setOnClickListener {
            addTask()
        }

        addDueDateButton.setOnClickListener {
            showDatePickerDialog()
        }
    }

    private fun loadTasksFromDb() {
        tasks.clear()
        tasks.addAll(dbHelper.getAllTasks())
    }

    private fun setupSpinners() {
        // Adaptador para el spinner de categorías
        ArrayAdapter.createFromResource(
            this,
            R.array.categories_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = adapter
        }

        // Adaptador para el spinner de importancia
        ArrayAdapter.createFromResource(
            this,
            R.array.importance_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            importanceSpinner.adapter = adapter
        }

        val importanceColors = resources.getIntArray(R.array.importance_colors)

        importanceSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                (parent.getChildAt(0) as? TextView)?.apply {
                    setTextColor(importanceColors[position])
                    typeface = Typeface.DEFAULT_BOLD
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
    }

    private fun setupFilterCategorySpinner() {
        // El array ya incluye "Todas" como primera opción
        filterCategorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedCategory = parent.getItemAtPosition(position).toString()
                filterTasksByCategory(selectedCategory)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun filterTasksByCategory(category: String) {
        val allTasks = dbHelper.getAllTasks()
        tasks.clear()
        if (category == "Todas") {
            tasks.addAll(allTasks)
        } else {
            tasks.addAll(allTasks.filter { it.category == category })
        }
        adapter.notifyDataSetChanged()
    }

    private fun addTask() {
        val taskName = newTaskEditText.text.toString()
        val category = categorySpinner.selectedItem.toString()
        val importance = importanceSpinner.selectedItem.toString()

        if (taskName.isNotEmpty()) {
            val newTask = Task(taskName, category, importance, false, dueDate)
            dbHelper.insertTask(newTask)
            tasks.add(newTask)
            adapter.notifyDataSetChanged()
            newTaskEditText.text.clear()
            dueDate = null // Reiniciar la fecha de vencimiento
            Toast.makeText(this, "Tarea agregada", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Por favor, ingrese una tarea", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onTaskDelete(task: Task) {
        // Mostrar AlertDialog de confirmación
        AlertDialog.Builder(this)
            .setTitle("Confirmar eliminación")
            .setMessage("¿Eliminar esta tarea?")
            .setPositiveButton("Sí") { _, _ ->
                dbHelper.deleteTask(task)
                tasks.remove(task)
                adapter.notifyDataSetChanged()
                Toast.makeText(this, "Tarea eliminada", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No", null)
            .show()
    }

    override fun onTaskCheckedChanged(task: Task) {
        dbHelper.updateTask(task)
    }

    override fun onTaskEdit(task: Task) {
        // Inflar el layout del diálogo
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_task, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.editTaskNameEditText)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.editCategorySpinner)
        val importanceSpinner = dialogView.findViewById<Spinner>(R.id.editImportanceSpinner)
        val dueDateEditText = dialogView.findViewById<EditText>(R.id.editDueDateEditText)

        // Llenar los campos con los valores actuales
        nameEditText.setText(task.name)
        dueDateEditText.setText(task.dueDate ?: "")

        // Configurar los spinners con los mismos arrays
        ArrayAdapter.createFromResource(
            this,
            R.array.categories_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = adapter
            val pos = adapter.getPosition(task.category)
            if (pos >= 0) categorySpinner.setSelection(pos)
        }
        ArrayAdapter.createFromResource(
            this,
            R.array.importance_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            importanceSpinner.adapter = adapter
            val pos = adapter.getPosition(task.importance)
            if (pos >= 0) importanceSpinner.setSelection(pos)
        }

        // Selector de fecha
        dueDateEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val parts = dueDateEditText.text.toString().split("/")
            if (parts.size == 3) {
                calendar.set(Calendar.DAY_OF_MONTH, parts[0].toInt())
                calendar.set(Calendar.MONTH, parts[1].toInt() - 1)
                calendar.set(Calendar.YEAR, parts[2].toInt())
            }
            val datePicker = DatePickerDialog(this,
                { _, year, month, dayOfMonth ->
                    val dateStr = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                    dueDateEditText.setText(dateStr)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        // Mostrar el diálogo
        AlertDialog.Builder(this)
            .setTitle("Editar tarea")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val newName = nameEditText.text.toString()
                val newCategory = categorySpinner.selectedItem.toString()
                val newImportance = importanceSpinner.selectedItem.toString()
                val newDueDate = dueDateEditText.text.toString()
                if (newName.isNotEmpty()) {
                    // Actualizar la tarea
                    task.name = newName
                    task.category = newCategory
                    task.importance = newImportance
                    task.dueDate = newDueDate
                    dbHelper.updateTask(task)
                    adapter.notifyDataSetChanged()
                    Toast.makeText(this, "Tarea actualizada", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                dueDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                Toast.makeText(this, "Fecha de finalización: $dueDate", Toast.LENGTH_SHORT).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }
}