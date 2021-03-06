package com.uc.saa1_0706011910003.fragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.ActivityOptions;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.uc.saa1_0706011910003.EditAccount;
import com.uc.saa1_0706011910003.Glovar;
import com.uc.saa1_0706011910003.LecturerData;
import com.uc.saa1_0706011910003.LecturerDetail;
import com.uc.saa1_0706011910003.R;
import com.uc.saa1_0706011910003.SplashScreen;
import com.uc.saa1_0706011910003.StarterActivity;
import com.uc.saa1_0706011910003.StudentRegister;
import com.uc.saa1_0706011910003.model.Lecturer;
import com.uc.saa1_0706011910003.model.Student;

public class AccountFragment extends Fragment {

    AlphaAnimation klik = new AlphaAnimation(1F, 0.6F);
    Button signout;
    Dialog dialog;
    Student student;
    TextView text_fname, text_email, text_nim, text_gender, text_age, text_address;
    DatabaseReference dbStudent,dbUser;
    ImageView edit_account;
    FirebaseAuth firebaseAuth;
    SharedPreferences userPref;
    SharedPreferences.Editor userEditor;

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();

        //ambil data student
        dbStudent = FirebaseDatabase.getInstance().getReference("student").child(firebaseAuth.getCurrentUser().getUid());
        dbUser = FirebaseDatabase.getInstance().getReference("student");

        dialog = Glovar.loadingDialog(getActivity());

        dbStudent.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
               if(snapshot.exists()) {
                   student = snapshot.getValue(Student.class);
                   setData();
               }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void setData(){
        //set text
        text_fname.setText(student.getName());
        text_email.setText(student.getEmail());
        text_nim.setText(student.getNim());
        text_gender.setText(student.getGender());
        text_age.setText(student.getAge());
        text_address.setText(student.getAddress());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //layout mana
        return inflater.inflate(R.layout.activity_account_fragment, container, false);


    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        text_fname = (TextView) getView().findViewById(R.id.name_frag_stud);
        text_email = (TextView) getView().findViewById(R.id.email_frag_stud);
        text_nim = (TextView) getView().findViewById(R.id.nim_frag_stud);
        text_gender = (TextView) getView().findViewById(R.id.gender_frag_stud);
        text_age = (TextView) getView().findViewById(R.id.age_frag_stud);
        text_address = (TextView) getView().findViewById(R.id.address_frag_stud);

        edit_account = (ImageView) getView().findViewById(R.id.edit_account);
        edit_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(getActivity(), EditAccount.class);
                in.putExtra("action", "edit_profile");
                in.putExtra("edit_data_profile", student);
                in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                getActivity().startActivity(in);
            }
        });

        userPref = getActivity().getSharedPreferences("user", getActivity().MODE_PRIVATE);
        userEditor = userPref.edit();

        signout = view.findViewById(R.id.button_signout_frag);
        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                v.startAnimation(klik);
                new AlertDialog.Builder(getActivity())
                        .setTitle("Confirmation")
                        .setIcon(R.drawable.logo2)
                        .setMessage("Are you sure to logout?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialogInterface, int i) {
                                dialog.show();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialog.cancel();

                                        dbUser.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("token").setValue("-");
                                        userEditor.putString("utoken","-");
                                        userEditor.commit();

                                        Toast.makeText(getActivity(), "Thank you!", Toast.LENGTH_SHORT).show();
                                        FirebaseAuth.getInstance().signOut();
                                                Intent intent = new Intent(getActivity(), StarterActivity.class);
                                                startActivity(intent);
                                                getActivity().finish();
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

}
