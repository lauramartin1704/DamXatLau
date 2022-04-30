package com.example.damxat.Views.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.damxat.Adapter.RecyclerUserAdapter;
import com.example.damxat.Model.User;
import com.example.damxat.R;
import com.example.damxat.Views.Activities.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserFragment extends Fragment {

    RecyclerView recyclerUsers;
    FirebaseUser firebaseUser;
    DatabaseReference ref;

    public UserFragment(){
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View viewUsers = inflater.inflate(R.layout.fragment_user, container, false);

        ((MainActivity) getActivity()).getSupportActionBar().setTitle("Users");

        recyclerUsers = viewUsers.findViewById(R.id.recyclerMyXats);
        recyclerUsers.setLayoutManager(new LinearLayoutManager(getContext()));

        Log.i("testlaura", "aquí");

        //Llamamos a la funcion de usuarios
        getUsers();

        return viewUsers;
    }

    public void getUsers(){
        // Aquí obtenemos un usuario de firebase y la referencia
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        ref = FirebaseDatabase.getInstance().getReference("Users");

        ArrayList<User> arrayUsers = new ArrayList<>();

        //Añadimos un Listener a la referencia.
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                arrayUsers.clear();
                //De la colección de Usuarios cojemos cada item (child) y lo agregamos al modelo de usuario, Si la identificación del usuario no es igual a la uid del usuario de firebase, agregamos el usuario a nuestra lista de usuarios
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);

                    if(!user.getId().equals(firebaseUser.getUid())){
                        arrayUsers.add(user);
                    }
                }

                RecyclerUserAdapter adapter = new RecyclerUserAdapter(arrayUsers, getContext());
                recyclerUsers.setAdapter(adapter);
                //recyclerUsers.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}