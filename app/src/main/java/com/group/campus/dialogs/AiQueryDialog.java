package com.group.campus.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.group.campus.R;
import com.group.campus.adapters.AIChatAdapter;
import com.group.campus.models.Messages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AiQueryDialog extends DialogFragment {

    private RecyclerView chatRv;

    private AIChatAdapter adapter;

    @Nullable
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_ai_query, null);
        dialog.setContentView(view);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.white);

        EditText etQuery = view.findViewById(R.id.et_ai_query);
        Button btnSend = view.findViewById(R.id.btn_ai_send);
        Button btnClose = view.findViewById(R.id.btn_ai_close);
        chatRv = view.findViewById(R.id.rv_chat);

        adapter = new AIChatAdapter(new ArrayList<>());

        chatRv.setAdapter(adapter);
        chatRv.setLayoutManager(new LinearLayoutManager(requireContext()));

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

       if (auth.getUid() == null) {
           throw new RuntimeException("No auth");
       }
        List<Messages> messages = new ArrayList<>();
        DocumentReference docRef = db.collection("users").document(auth.getUid());
        docRef.collection("messages")
                                .orderBy("createTime")
                                        .get()
                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            for (DocumentSnapshot doc : task.getResult().getDocuments()) {
                                                                Messages message = doc.toObject(Messages.class);
                                                                if (message != null) {
                                                                    messages.add(message);
                                                                }
                                                            }

                                                            adapter.setMessages(messages);
                                                            chatRv.scrollToPosition(adapter.getItemCount() - 1);
                                                        }
                                                    }
                                                });


        final String[] newDocId = new String[1];
                Messages msgs = new Messages();
        btnSend.setOnClickListener(v -> {
            String query = etQuery.getText().toString().trim();
            if (!query.isEmpty()) {
                Map<String, Object> msg = new HashMap<>();
                msgs.setPrompt(query);
                msg.put("prompt", query);
                docRef.collection("messages").add(msg)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                etQuery.setText("");
                                adapter.addMessage(msgs);
                                chatRv.scrollToPosition(adapter.getItemCount() - 1);
                                documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                                        if (error != null) {
                                            Log.e("TAG", "onEvent: Failed to fetch ai response: ", error.getCause());
                                            return;
                                        }
                                        if (value != null && value.exists()) {
                                            String response = value.getString("response");
                                            if (response != null && !response.isEmpty()) {
                                                msgs.setResponse(response);
                                                adapter.updateMessageNow(msgs);
                                                chatRv.scrollToPosition(adapter.getItemCount() - 1);
                                            }
                                        }
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e("TAG", "onFailure: Failed to add!", e);
                            }
                        });

            }
        });

        btnClose.setOnClickListener(v -> dismiss());

        return dialog;
    }

    public static AiQueryDialog newInstance() {
        return new AiQueryDialog();
    }
}
