package com.example.todo_list_v3;

import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private Button addTask;
    private ListView listViewTasks;
    private List<Task> taskList;
    private TaskDatabaseHelper dbHelper;
    private TaskAdapter taskAdapter;
    private TextView emptyTaskView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emptyTaskView = (TextView)findViewById(R.id.emptyText);

        dbHelper = new TaskDatabaseHelper(this);
        addTask = findViewById(R.id.buttonAddTask);
        listViewTasks = findViewById(R.id.listViewTasks);

        addTask.setOnClickListener(v -> showAddTaskDialog());

        loadTasks();
    }

    private void loadTasks() {
        taskList = dbHelper.getAllTasks();
        taskAdapter = new TaskAdapter(taskList);
        listViewTasks.setAdapter(taskAdapter);
        if ((taskList.size() == 0)) {
            emptyTaskView.setText("there are no task to do !");
        } else {
            emptyTaskView.setText("");
        }
    }
    public static String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    private void showAddTaskDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_task, null);
        EditText editTextTaskTitle = dialogView.findViewById(R.id.editTextTaskTitle);
        EditText editTextTaskDescription = dialogView.findViewById(R.id.editTextTaskDescription);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle("Add Task")
                .setPositiveButton("Save", (dialogInterface, i) -> {
                    String title = editTextTaskTitle.getText().toString();
                    String description = editTextTaskDescription.getText().toString();
                    Task newTask = new Task(0, title, description, false,getCurrentTimestamp());
                    dbHelper.addTask(newTask);
                    loadTasks();
                    showToast("Task added");
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    private void showEditTaskDialog(Task taskToEdit) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_task, null);
        EditText editTextTaskTitle = dialogView.findViewById(R.id.editTextTaskTitle);
        EditText editTextTaskDescription = dialogView.findViewById(R.id.editTextTaskDescription);

        editTextTaskTitle.setText(taskToEdit.getTitle());
        editTextTaskDescription.setText(taskToEdit.getDescription());

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle("Edit Task")
                .setPositiveButton("Save", (dialogInterface, i) -> {
                    String title = editTextTaskTitle.getText().toString();
                    String description = editTextTaskDescription.getText().toString();
                    taskToEdit.setTitle(title);
                    taskToEdit.setDescription(description);
                    dbHelper.updateTask(taskToEdit);
                    loadTasks();
                    showToast("Task updated");
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private class TaskAdapter extends ArrayAdapter<Task> {
        public TaskAdapter(List<Task> tasks) {
            super(MainActivity.this, R.layout.list_item_task, tasks);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_task, parent, false);
            }

            Task currentTask = getItem(position);

            CheckBox checkBoxCompleted = convertView.findViewById(R.id.checkBoxCompleted);
            TextView textViewTaskTitle = convertView.findViewById(R.id.textViewTaskTitle);
            TextView textViewTaskDescription = convertView.findViewById(R.id.textViewTaskDescription);
            Button buttonEdit = convertView.findViewById(R.id.buttonEdit);
            Button buttonDelete = convertView.findViewById(R.id.buttonDelete);
            TextView textViewTaskTimestamp = convertView.findViewById(R.id.textViewTaskTimestamp);
            textViewTaskTimestamp.setText("created at: "+currentTask.getTimestamp());


            checkBoxCompleted.setChecked(currentTask.isCompleted());
            textViewTaskTitle.setText(currentTask.getTitle());
            textViewTaskDescription.setText(currentTask.getDescription());

            checkBoxCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
                currentTask.setCompleted(isChecked);
                dbHelper.updateTask(currentTask);
                showToast(isChecked ? "Task completed" : "Task marked as incomplete");
            });

            buttonEdit.setOnClickListener(v -> showEditTaskDialog(currentTask));

            buttonDelete.setOnClickListener(v -> {
                dbHelper.deleteTask(currentTask.getId());
                loadTasks();
                showToast("Task deleted");
            });

            return convertView;
        }
    }
}
