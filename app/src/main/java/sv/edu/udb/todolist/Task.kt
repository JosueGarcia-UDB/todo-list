package sv.edu.udb.todolist

data class Task(
    val name: String,
    val category: String,
    val importance: String,
    var isChecked: Boolean = false
)