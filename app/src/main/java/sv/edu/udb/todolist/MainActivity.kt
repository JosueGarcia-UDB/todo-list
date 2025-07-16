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

class MainActivity : AppCompatActivity(), TaskAdapter.OnTaskDeleteListener {

    private lateinit var tasksListView: ListView
    private lateinit var newTaskEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var importanceSpinner: Spinner
    private lateinit var addTaskButton: ImageButton

    private val tasks = mutableListOf<Task>()
    private lateinit var adapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tasksListView = findViewById(R.id.tasksListView)
        newTaskEditText = findViewById(R.id.newTaskEditText)
        categorySpinner = findViewById(R.id.categorySpinner)
        importanceSpinner = findViewById(R.id.importanceSpinner)
        addTaskButton = findViewById(R.id.addTaskButton)

        // Configurar el adaptador para la lista de tareas
        adapter = TaskAdapter(this, tasks, this)
        tasksListView.adapter = adapter

        // Configurar los spinners
        setupSpinners()

        // Configurar el listener del botón para agregar tareas
        addTaskButton.setOnClickListener {
            addTask()
        }

        // Agregar algunas tareas solo de ejemplo
        addSampleTasks()
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

    private fun addTask() {
        val taskName = newTaskEditText.text.toString()
        val category = categorySpinner.selectedItem.toString()
        val importance = importanceSpinner.selectedItem.toString()

        if (taskName.isNotEmpty()) {
            val newTask = Task(taskName, category, importance)
            tasks.add(newTask)
            adapter.notifyDataSetChanged()
            newTaskEditText.text.clear()
            Toast.makeText(this, "Tarea agregada", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Por favor, ingrese una tarea", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addSampleTasks() {
        tasks.add(Task("Comprar leche", "Compras", "Importante"))
        tasks.add(Task("Hacer la tarea de matemáticas", "Educación", "Algo importante"))
        tasks.add(Task("Llamar al doctor", "Salud", "No importante"))
        adapter.notifyDataSetChanged()
    }

    override fun onTaskDelete(task: Task) {
        tasks.remove(task)
        adapter.notifyDataSetChanged()
        Toast.makeText(this, "Tarea eliminada", Toast.LENGTH_SHORT).show()
    }
}