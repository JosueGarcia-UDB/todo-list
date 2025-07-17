package sv.edu.udb.todolist

data class Task(
    var name: String,
    var category: String,
    var importance: String,
    var isChecked: Boolean = false,
    var dueDate: String? = null // Fecha de vencimiento (opcional)
)