package com.uc.saa1_0706011910003.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.uc.saa1_0706011910003.AddCourse;
import com.uc.saa1_0706011910003.CourseData;
import com.uc.saa1_0706011910003.Glovar;
import com.uc.saa1_0706011910003.R;
import com.uc.saa1_0706011910003.model.Course;
import com.uc.saa1_0706011910003.model.Student;

import java.util.ArrayList;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CardViewViewHolder> {


    private Context context;
    DatabaseReference dbCourse;
    DatabaseReference dbStudent;
    DatabaseReference dbCourses;
    Dialog dialog;
    int pos = 0;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    private ArrayList<Course> listCourse;
    private ArrayList<Course> getListCourse() {
        return listCourse;
    }
    public void setListCourse(ArrayList<Course> listCourse) {
        this.listCourse = listCourse;
    }
    public CourseAdapter(Context context) {
        this.context = context;
    }

    AlphaAnimation klik = new AlphaAnimation(1F, 0.6F);

    @NonNull
    @Override
    public CourseAdapter.CardViewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //ke layout mana
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_course_adapter, parent, false);
        return new CourseAdapter.CardViewViewHolder(view);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull final CourseAdapter.CardViewViewHolder holder, int position) {
        //set text yg show di card
        final Course course = getListCourse().get(position);
        holder.crSubject.setText(course.getSubject());
        holder.crDay.setText(course.getDay());
        holder.crStart.setText(course.getStart());
        holder.crEnd.setText(course.getEnd());
        holder.crLecturer.setText(course.getLecturer());

        //button edit di klik menuju edit course
        holder.button_edit.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                v.startAnimation(klik);
                Intent in = new Intent(context, AddCourse.class);
                //yg dikirim value (namanya)
                in.putExtra("action", "edit_data_course");
                in.putExtra("edit_data_course", course);
                in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(in);
//                finish();
            }
        });
        //button delete, ada alert
        holder.button_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(klik);
                new AlertDialog.Builder(context)
                        .setTitle("Confirmation")
                        .setIcon(R.drawable.logo2)
                        .setMessage("Are you sure you want to delete  "+course.getSubject()+" ?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialogInterface, int i) {
                                dialog.show();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialog.cancel();
                                        checkCourse(course.getId());
                                        //ambil id, lalu delete
                                        dbCourse.child(course.getId()).removeValue(new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                                Intent in = new Intent(context, CourseData.class);
                                                in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                Toast.makeText(context, "Delete success!", Toast.LENGTH_SHORT).show();
                                                context.startActivity(in);
                                                ((Activity)context).finish();
                                                dialogInterface.cancel();
                                            }
                                        });

                                    }
                                }, 2000);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .create()
                        .show();
            }
        });
    }


    @Override
    public int getItemCount() {
        return getListCourse().size();
    }

    class CardViewViewHolder extends RecyclerView.ViewHolder{
        TextView crSubject, crDay, crStart, crEnd, crLecturer;
        ImageView button_edit, button_delete;

        CardViewViewHolder(View itemView) {
            super(itemView);
            crSubject = itemView.findViewById(R.id.course_subject);
            crDay = itemView.findViewById(R.id.days_course);
            crStart = itemView.findViewById(R.id.time_start_course);
            crEnd = itemView.findViewById(R.id.time_end_course);
            crLecturer = itemView.findViewById(R.id.lect_course);

            //akses ke path apa
            dbCourse = FirebaseDatabase.getInstance().getReference("course");
            dbStudent = FirebaseDatabase.getInstance().getReference("student");

            dialog = Glovar.loadingDialog(context);

            button_edit = itemView.findViewById(R.id.edit_course);
            button_delete = itemView.findViewById(R.id.delete_course);
        }

    }

    //cascade, ketika parent kebuang maka course yg sudah di take student akan ikut terbuang
    public void checkCourse(final String check){
        dbStudent.addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //ambil data dari firebase, berupa datasnapshot
                for (DataSnapshot stud : snapshot.getChildren()){
                    //path akses kemana
                    dbCourses = dbStudent.child(stud.getValue(Student.class).getUid()).child("courses");
                    dbCourses.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            //baca id satu" dari course yg sudah di take student
                            for (DataSnapshot cr : snapshot.getChildren()){
                                cr.getValue(Course.class).getId();
                                if (check.equals(cr.getValue(Course.class).getId())){
                                    dbCourses.child(cr.getValue(Course.class).getId()).removeValue(new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                            Log.d("yipii", check);
                                        }
                                    });
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}
