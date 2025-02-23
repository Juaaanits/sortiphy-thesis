package com.reviewers.sortiphy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SettingsFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private Uri imageUri;
    private ImageView profileImageView;
    private FirebaseFirestore db;
    private String userId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        db = FirebaseFirestore.getInstance();
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        userId = sharedPreferences.getString("USER_ID", null);

        requestPermissions();

        Button btnGallery = view.findViewById(R.id.btnGallery);
        profileImageView = view.findViewById(R.id.profileImageView);

        btnGallery.setOnClickListener(v -> openGallery());

        return view;
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 101);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (requestCode == PICK_IMAGE_REQUEST) {
                imageUri = data.getData();
            } else if (requestCode == CAMERA_REQUEST && data.getExtras() != null) {
                imageUri = data.getData();
            }

            if (imageUri != null) {
                uploadImageToCloudinary(imageUri);
            }
        }
    }

    private void uploadImageToCloudinary(Uri fileUri) {

        String cloudName = "dzmympsci";
        String uploadPreset = "unsigned_upload";
        String uploadUrl = "https://api.cloudinary.com/v1_1/" + cloudName + "/image/upload";

        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(fileUri);
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            String encodedImage = Base64.encodeToString(bytes, Base64.DEFAULT);

            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", "image.png",
                            RequestBody.create(MediaType.parse("image/png"), bytes)) // Directly send bytes
                    .addFormDataPart("upload_preset", uploadPreset)
                    .build();

            Request request = new Request.Builder()
                    .url(uploadUrl)
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Upload Failed", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        try {
                            JSONObject json = new JSONObject(responseData);
                            String imageUrl = json.getString("secure_url");

                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "Upload Successful!", Toast.LENGTH_SHORT).show();
                                Glide.with(getContext()).load(imageUrl).into(profileImageView);

                                if (userId != null) {
                                    Map<String, Object> updateData = new HashMap<>();
                                    updateData.put("displayPhoto", imageUrl);

                                    db.collection("users").document(userId)
                                            .update(updateData)
                                            .addOnSuccessListener(aVoid ->
                                                    Toast.makeText(getContext(), "Profile photo updated!", Toast.LENGTH_SHORT).show())
                                            .addOnFailureListener(e ->
                                                    Toast.makeText(getContext(), "Failed to update Firestore", Toast.LENGTH_SHORT).show());
                                } else {
                                    Toast.makeText(getContext(), "User ID is null", Toast.LENGTH_SHORT).show();
                                }

                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
