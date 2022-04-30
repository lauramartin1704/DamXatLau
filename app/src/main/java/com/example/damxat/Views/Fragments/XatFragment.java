package com.example.damxat.Views.Fragments;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import static com.example.damxat.Notificacions.Constants.retrofit;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.damxat.Adapter.RecyclerXatAdapter;
import com.example.damxat.Model.User;
import com.example.damxat.Model.Xat;
import com.example.damxat.Model.XatGroup;
import com.example.damxat.Notificacions.ApiInterface;
import com.example.damxat.Notificacions.NotificationModel;
import com.example.damxat.Notificacions.PushNotification;
import com.example.damxat.Notificacions.ResponseModel;
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
import java.util.HashMap;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class XatFragment extends Fragment {

    DatabaseReference ref;
    View view;
    FirebaseUser firebaseUser;
    String userid;
    Bundle bundle;
    Boolean isXatUser;
    ArrayList<Xat> arrayXats;
    ArrayList<String> arrayUsers;

    int RecordAudioRequestCode=1;


    private EditText txtMessage;
    private String userToken;

    XatGroup group;
    String groupName;

    public XatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_xat, container, false);
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions((Activity) getContext(),new String[]{Manifest.permission.RECORD_AUDIO}, RecordAudioRequestCode);
            }
        }
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        bundle = getArguments();

        if(bundle.getString("type").equals("xatuser")){
            isXatUser = true;
            getUserXat();
        }else{
            isXatUser = false;
            groupName = bundle.getString("group");
            ((MainActivity) getActivity()).getSupportActionBar().setTitle(groupName);
            readGroupMessages(groupName);
        }
        // Permisos para grabar
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, RecordAudioRequestCode);
            }
        }


        ImageButton btnMessage = view.findViewById(R.id.btnMessage);
        txtMessage = view.findViewById(R.id.txtMessage);
        ImageButton btn= view.findViewById(R.id.btnMicro);

        //Boton para enviar el mensaje con el texto
        btnMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = txtMessage.getText().toString();
                userToken = "fCfslGvqT6CYmG_ubPHGsC:APA91bGGfEVLdHa6i6c8hr-8K3ztS7DtWcSl45BvxhD-n_qvIThLK-5kE3V6lkr-_z9u2kch9CqJKNEKF-ShdydIeMTWVXnpMUanjmGSPvk42PQeLyczl-rXOTdosTy1LM5BhOBMwLlu";

                if(!msg.isEmpty()){
                    sendMessage(firebaseUser.getUid(), msg, isXatUser);
                    sendNotification(firebaseUser.getDisplayName(), msg, userToken);
                }else{
                    Toast.makeText(getContext(), "You can't send empty message", Toast.LENGTH_SHORT).show();
                }
                txtMessage.setText("");
            }
        });

        // This button allows you to send a voice message
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn.setImageResource(R.drawable.ic_voice_foreground);
                Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hola, que tal?");
                startActivityForResult(speechRecognizerIntent, RecordAudioRequestCode);
            }
        });

        return view;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RecordAudioRequestCode && grantResults.length > 0 ){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RecordAudioRequestCode && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result=data.getStringArrayListExtra( RecognizerIntent.EXTRA_RESULTS );
            if(!result.get(0).isEmpty()){
                sendMessage(firebaseUser.getUid(), result.get(0), isXatUser);

            }else{
                Toast.makeText(getContext(), "You can't send empty message", Toast.LENGTH_SHORT).show();
            }
            txtMessage.setText("");
        }
    }

    //Obtencion de mensajes de usuario
    public void getUserXat(){
        if(getArguments()!=null) {
            userid = bundle.getString("user");

            //Obtencion de la referencia de usuario
            ref = FirebaseDatabase.getInstance().getReference("Users").child(userid);

            //Evento de conexión
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    //Objeto user declarado
                    User user = dataSnapshot.getValue(User.class);

                    //el titilo lo cambiamos por el nombre de usuario
                    ((MainActivity) getActivity()).getSupportActionBar().setTitle(user.getUsername());
                    userToken = user.getToken();
                    //recogemos los mensajes llamando al metodo
                    readUserMessages();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }
    public void sendNotification(String title, String missatge, String token) {
        NotificationModel notification = new NotificationModel(title, missatge, "");
        PushNotification pushNotification = new PushNotification(token, notification);
        ApiInterface apiCall = retrofit.create(ApiInterface.class);
        Call<ResponseModel> call = apiCall.postNotification(pushNotification);

        call.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
            }

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {

            }
        });
    }


    public void sendMessage(String sender, String message, boolean isXatUser){
        //Comprovacion de que tipo de chat es
        if(isXatUser==true){
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

            String receiver = userid;
            Xat xat = new Xat(sender, receiver, message);
            ref.child("Xats").push().setValue(xat);

            //crear retrofit amb url...

        }else{
            ref = FirebaseDatabase.getInstance().getReference("Groups").child(groupName);

            Xat xat = new Xat(sender, message);

            if(arrayXats==null) {
                arrayXats = new ArrayList<Xat>();
                arrayXats.add(xat);
            }else{
                arrayXats.add(xat);
            }

            if(group.getUsers()==null){
                arrayUsers = new ArrayList<String>();
                arrayUsers.add(firebaseUser.getUid());
            }else{
                if(!group.getUsers().contains(firebaseUser.getUid())){
                    arrayUsers.add(firebaseUser.getUid());
                }
            }

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("xats", arrayXats);
            hashMap.put("users", arrayUsers);
            ref.updateChildren(hashMap);
        }
    }

    public void readUserMessages(){
        arrayXats = new ArrayList<>();

        //Referencia de Chats en firebase
        ref = FirebaseDatabase.getInstance().getReference("Xats");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                arrayXats.clear();

                //para cada item guadrdamos en la clase de char
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Xat xat = postSnapshot.getValue(Xat.class);
                    //Si lo que recibimos es correcto entra
                    if(xat.getReceiver().equals(userid) && xat.getSender().equals(firebaseUser.getUid()) ||
                            xat.getReceiver().equals(firebaseUser.getUid()) && xat.getSender().equals(userid)){
                        arrayXats.add(xat);
                        Log.i("logTest",xat.getMessage());
                    }
                }

                //Si es correcto lo pasamos al recycler
                updateRecycler();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("damxat", "Failed to read value.", error.toException());
            }
        });
    }


    public void readGroupMessages(String groupName){

        //Obtenció xat de Grup
        ref = FirebaseDatabase.getInstance().getReference("Groups").child(groupName);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                group = dataSnapshot.getValue(XatGroup.class);

                arrayXats = group.getXats();

                if(arrayXats!=null) {
                    updateRecycler();
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("damxat", "Failed to read value.", error.toException());
            }
        });
    }

    public void updateRecycler(){
        RecyclerView recyclerView = view.findViewById(R.id.recyclerXat);
        RecyclerXatAdapter adapter = new RecyclerXatAdapter(arrayXats, getContext());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }
}