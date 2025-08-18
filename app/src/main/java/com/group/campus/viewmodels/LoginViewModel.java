package com.group.campus.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.group.campus.dtos.LoginRequest;
import com.group.campus.dtos.LoginResponse;
import com.group.campus.service.UserService;
import com.group.campus.utils.ApiClient;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginViewModel extends ViewModel {
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isSuccessful = new MutableLiveData<>();

    private FirebaseAuth fAuth;

    public LiveData<Boolean> getSuccessState() { return isSuccessful; }
    public LiveData<String> getErrorMsg() { return errorMessage; }
    public LiveData<Boolean> getLoading() { return loading; }

    public void login(String regNo, String password) {
        if (regNo.isEmpty() && password.isEmpty()) {
            errorMessage.setValue("Fill all the fields");
            return;
        }

        String email = regNo + "@college.edu";

        loading.setValue(true);

        fAuth = FirebaseAuth.getInstance();

        fAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        loading.setValue(false);
                        if (task.isSuccessful()) {
                            isSuccessful.setValue(true);
                        } else {
                            isSuccessful.setValue(false);
                            errorMessage.setValue(
                                    task.getException() != null ? task.getException().toString() : "Sign In Failed"
                            );
                        }
                    }
                });
    }

}
