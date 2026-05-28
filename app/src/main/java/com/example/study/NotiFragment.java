package com.example.study;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class NotiFragment extends Fragment {
    private FloatingActionButton fabAdd;
    private RecyclerView rv;
    private TaskAdapter adapter;
    private List<StudyNotification> taskList;
    private DatabaseReference databaseReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_noti, container, false);

        fabAdd = view.findViewById(R.id.fabAddNotification);
        rv = view.findViewById(R.id.rvNotifications);

        taskList = new ArrayList<>();
        adapter = new TaskAdapter(taskList);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        if (FBReF.auth.getCurrentUser() != null) {
            String uid = FBReF.auth.getCurrentUser().getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference("Tasks").child(uid);

            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    taskList.clear();
                    for (DataSnapshot data : snapshot.getChildren()) {
                        StudyNotification task = data.getValue(StudyNotification.class);
                        if (task != null) {
                            taskList.add(task);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "eroer with dital", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            if (getContext() != null) {
                Toast.makeText(getContext(), "no user", Toast.LENGTH_SHORT).show();
            }
        }

        fabAdd.setOnClickListener(v -> showAddTaskDialog());

        return view;
    }

    private void showAddTaskDialog() {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_task, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        EditText etTitle = dialogView.findViewById(R.id.etTaskTitle);
        Button btnDate = dialogView.findViewById(R.id.btnPickDate);
        Button btnTime = dialogView.findViewById(R.id.btnPickTime);
        EditText etCategory = dialogView.findViewById(R.id.etCategory);
        EditText etDuration = dialogView.findViewById(R.id.etDuration);
        Spinner spinnerDiff = dialogView.findViewById(R.id.spinnerDifficulty);
        Button btnSave = dialogView.findViewById(R.id.btnSaveTask);

        String[] difficulties = {"easy", "medium", "hard", "difficult"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, difficulties);
        spinnerDiff.setAdapter(spinnerAdapter);

        final boolean[] isDateSelected = {false};
        final boolean[] isTimeSelected = {false};
        final int[] selectedYear = new int[1];
        final int[] selectedMonth = new int[1];
        final int[] selectedDay = new int[1];
        final int[] selectedHour = new int[1];
        final int[] selectedMinute = new int[1];

        btnDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(getContext(), (view, year, month, day) -> {
                btnDate.setText(day + "/" + (month + 1) + "/" + year);
                selectedYear[0] = year;
                selectedMonth[0] = month;
                selectedDay[0] = day;
                isDateSelected[0] = true;
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new TimePickerDialog(getContext(), (view, hour, minute) -> {
                btnTime.setText(String.format("%02d:%02d", hour, minute));
                selectedHour[0] = hour;
                selectedMinute[0] = minute;
                isTimeSelected[0] = true;
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
        });

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String date = btnDate.getText().toString().trim();
            String time = btnTime.getText().toString().trim();
            String category = etCategory.getText().toString().trim();
            String diff = spinnerDiff.getSelectedItem() != null ? spinnerDiff.getSelectedItem().toString() : "easy";
            String duration = etDuration.getText().toString().trim();

            if (title.isEmpty() || !isDateSelected[0]) {
                Toast.makeText(getContext(), "choose date and subject", Toast.LENGTH_LONG).show();
                return;
            }

            if (databaseReference == null) {
                Toast.makeText(getContext(), "eroer try agein", Toast.LENGTH_SHORT).show();
                return;
            }

            String id = databaseReference.push().getKey();
            StudyNotification task = new StudyNotification(id, title, date, time, category, diff, duration);

            if (id != null) {
                databaseReference.child(id).setValue(task).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        Toast.makeText(getContext(), "craite mission succesfol", Toast.LENGTH_SHORT).show();
                        if (isTimeSelected[0]) {
                            scheduleNotification(title, category, selectedYear[0], selectedMonth[0], selectedDay[0], selectedHour[0], selectedMinute[0]);
                        }
                        dialog.dismiss();
                    } else {
                        Toast.makeText(getContext(), "craite mission faild", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        dialog.show();
    }

    private void scheduleNotification(String title, String category, int year, int month, int day, int hour, int minute) {
        if (getContext() == null) return;

        Calendar targetCalendar = Calendar.getInstance();
        targetCalendar.set(Calendar.YEAR, year);
        targetCalendar.set(Calendar.MONTH, month);
        targetCalendar.set(Calendar.DAY_OF_MONTH, day);
        targetCalendar.set(Calendar.HOUR_OF_DAY, hour);
        targetCalendar.set(Calendar.MINUTE, minute);
        targetCalendar.set(Calendar.SECOND, 0);

        if (targetCalendar.before(Calendar.getInstance())) return;

        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getContext(), NotificationReceiver.class);
        intent.putExtra("title", title);
        intent.putExtra("category", category);

        int requestCode = (int) System.currentTimeMillis();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getContext(), requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetCalendar.getTimeInMillis(), pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, targetCalendar.getTimeInMillis(), pendingIntent);
            }
        }
    }
}