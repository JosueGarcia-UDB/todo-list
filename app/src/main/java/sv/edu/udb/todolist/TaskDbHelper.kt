package sv.edu.udb.todolist

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues
import android.database.Cursor

class TaskDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "tasks.db"
        private const val DATABASE_VERSION = 2 // Incrementar versión para migración

        const val TABLE_NAME = "tasks"
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_IMPORTANCE = "importance"
        const val COLUMN_IS_CHECKED = "isChecked"
        const val COLUMN_DUE_DATE = "dueDate" // Nueva columna para fecha de vencimiento
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT,
                $COLUMN_CATEGORY TEXT,
                $COLUMN_IMPORTANCE TEXT,
                $COLUMN_IS_CHECKED INTEGER,
                $COLUMN_DUE_DATE TEXT
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // Insertar tarea
    fun insertTask(task: Task) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, task.name)
            put(COLUMN_CATEGORY, task.category)
            put(COLUMN_IMPORTANCE, task.importance)
            put(COLUMN_IS_CHECKED, if (task.isChecked) 1 else 0)
            put(COLUMN_DUE_DATE, task.dueDate)
        }
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    // Obtener todas las tareas
    fun getAllTasks(): MutableList<Task> {
        val tasks = mutableListOf<Task>()
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM $TABLE_NAME", null)
        if (cursor.moveToFirst()) {
            do {
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
                val category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY))
                val importance = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMPORTANCE))
                val isChecked = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_CHECKED)) == 1
                val dueDate = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DUE_DATE))
                tasks.add(Task(name, category, importance, isChecked, dueDate))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return tasks
    }

    // Eliminar tarea por nombre (puedes mejorar esto usando un ID único)
    fun deleteTask(task: Task) {
        val db = writableDatabase
        db.delete(TABLE_NAME, "$COLUMN_NAME = ?", arrayOf(task.name))
        db.close()
    }

    // Actualizar tarea (por nombre)
    fun updateTask(task: Task) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_IS_CHECKED, if (task.isChecked) 1 else 0)
            put(COLUMN_DUE_DATE, task.dueDate)
        }
        db.update(TABLE_NAME, values, "$COLUMN_NAME = ?", arrayOf(task.name))
        db.close()
    }
} 