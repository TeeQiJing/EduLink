package com.dellmau.edulink.fragments;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.dellmau.edulink.R;


import org.apache.commons.compress.archivers.dump.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public class CourseOutlineFragment extends Fragment {

    private static final int PICK_FILE_REQUEST = 1;
    private static final String TAG = "CourseOutlineFragment"; // For Logcat
    private ProgressBar progressBar;
    private TextView resultText;

    private TableLayout uploadedTable, updatedTable;

    public CourseOutlineFragment() {
        super(R.layout.fragment_course_outline); // Set the layout file
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Button uploadButton = view.findViewById(R.id.upload_button);
        progressBar = view.findViewById(R.id.progress_bar);
        resultText = view.findViewById(R.id.result_text);
        uploadedTable = view.findViewById(R.id.uploaded_table);
        updatedTable = view.findViewById(R.id.updated_table);
        // Request permission to write to external storage (Android 6.0+)
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        uploadButton.setOnClickListener(v -> openFileChooser());
    }

    // Open file chooser to select Excel file
    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); // Filter for Excel files only
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }


    // Handle file selection result
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == getActivity().RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            try {
                uploadFile(fileUri);  // Delegate the file upload to the uploadFile method
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Error reading the file", Toast.LENGTH_SHORT).show();
            }
        }
    }


    // Upload the file to the backend (Flask server)
    private void uploadFile(Uri fileUri) {
        try {
            InputStream inputStream = getContext().getContentResolver().openInputStream(fileUri);
            File file = new File(getContext().getCacheDir(), "uploaded_file.xlsx"); // Temporary file
            OutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            inputStream.close();
            outputStream.close();

            RequestBody requestBody = RequestBody.create(MultipartBody.FORM, file);
            MultipartBody.Part part = MultipartBody.Part.createFormData("course_outline_file", file.getName(), requestBody);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://fd50-132-237-184-254.ngrok-free.app") // Update this URL to your Flask server URL
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            ApiService apiService = retrofit.create(ApiService.class);
            Call<ResponseBody> call = apiService.uploadCourseOutline(part);

            progressBar.setVisibility(View.VISIBLE);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        try {
                            // Check if the response body is not null
                            if (response.body() == null) {
                                Log.e(TAG, "Response body is null.");
                                Toast.makeText(getContext(), "Failed to download file: No data received", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // Download the updated course outline file to phone storage
                            File downloadedFile = downloadFileToStorage(response.body());

                            // Log the file path to check if it's correctly saved
                            Log.d(TAG, "File downloaded to: " + downloadedFile.getAbsolutePath());

                            // Show uploaded data in uploadedTable
                            displayCourseOutline(new FileInputStream(new File(getContext().getCacheDir(), "uploaded_file.xlsx")), true); // Show uploaded courses in uploaded table

                            // Show updated data in updatedTable
                            displayCourseOutline(new FileInputStream(downloadedFile), false); // Show updated courses in updated table

                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e(TAG, "Download failed: " + e.getMessage(), e);
                            Toast.makeText(getContext(), "Download failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Upload failed with code: " + response.code() + " and message: " + response.message());
                        Toast.makeText(getContext(), "Upload failed", Toast.LENGTH_SHORT).show();
                    }
                }


                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Request failed: " + t.getMessage(), t);
                    Toast.makeText(getContext(), "Request failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "File upload failed: " + e.getMessage(), e);
            Toast.makeText(getContext(), "File upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    // Function to download the file to storage
    private File downloadFileToStorage(ResponseBody responseBody) throws IOException {
        // Define the path where the file will be saved
        File outputFile = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "UpdatedCourseOutline.xlsx");

        // Log the file path to check
        Log.d(TAG, "Saving file to: " + outputFile.getAbsolutePath());

        // Create the input stream from the response body and output stream to the file
        InputStream inputStream = responseBody.byteStream();
        OutputStream outputStream = new FileOutputStream(outputFile);

        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        outputStream.flush();
        outputStream.close();
        inputStream.close();

        // Return the downloaded file
        return outputFile;
    }

    private void displayCourseOutline(InputStream inputStream, boolean isUploaded) {
        try {
            Workbook workbook = WorkbookFactory.create(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            TableLayout tableLayout = isUploaded ? uploadedTable : updatedTable;
            tableLayout.removeAllViews();

            for (Row row : sheet) {
                TableRow tableRow = new TableRow(getContext());
                for (Cell cell : row) {
                    TextView textView = new TextView(getContext());
                    textView.setText(cell.toString());
                    textView.setPadding(16, 16, 16, 16);
                    tableRow.addView(textView);
                }
                tableLayout.addView(tableRow);
            }

            // Show the TableLayout after populating the data
            tableLayout.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error displaying courses: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Method to display Excel data in the appropriate TableLayout
    private void showExcelData(File file, boolean isUploaded) {
        try {
            FileInputStream fis = new FileInputStream(file);
            Workbook workbook = WorkbookFactory.create(fis);
            Sheet sheet = workbook.getSheetAt(0); // Get the first sheet

            // Choose the appropriate table based on whether it's uploaded or updated
            TableLayout tableLayout = isUploaded ? uploadedTable : updatedTable;

            // Clear any previous table data
            tableLayout.removeAllViews();

            // Iterate through rows and display the data
            for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                Row row = sheet.getRow(i);

                TableRow tableRow = new TableRow(getContext());
                TextView courseName = new TextView(getContext());
                courseName.setText(row.getCell(0).getStringCellValue());
                tableRow.addView(courseName);

                TextView credit = new TextView(getContext());
                credit.setText(String.valueOf((int) row.getCell(1).getNumericCellValue())); // Assuming credit is numeric
                tableRow.addView(credit);

                TextView semester = new TextView(getContext());
                semester.setText(String.valueOf((int) row.getCell(2).getNumericCellValue())); // Assuming semester is numeric
                tableRow.addView(semester);

                tableLayout.addView(tableRow); // Add the row to the TableLayout
            }
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to read Excel file", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can now perform the file download
            } else {
                // Permission denied, notify the user that they need to grant storage access
                Toast.makeText(getContext(), "Permission to write to storage is required", Toast.LENGTH_SHORT).show();
            }
        }
    }


    // Retrofit API interface for file upload
    interface ApiService {
        @Multipart
        @POST("/generate_updated_courses")
        Call<ResponseBody> uploadCourseOutline(@Part MultipartBody.Part file);
    }
}
//public class CourseOutlineFragment extends Fragment {
//
//    private static final int PICK_FILE_REQUEST = 1;
//    private static final String TAG = "CourseOutlineFragment"; // For Logcat
//    private ProgressBar progressBar;
//    private TextView resultText, descriptionText;
//
//    private TableLayout uploadedTable, updatedTable;
//
//    public CourseOutlineFragment() {
//        super(R.layout.fragment_course_outline); // Set the layout file
//    }
//
//    @Override
//    public void onViewCreated(View view, Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        Button uploadButton = view.findViewById(R.id.upload_button);
//        progressBar = view.findViewById(R.id.progress_bar);
//        resultText = view.findViewById(R.id.result_text);
//        descriptionText = view.findViewById(R.id.description_text);
//        uploadedTable = view.findViewById(R.id.uploaded_table);
//        updatedTable = view.findViewById(R.id.updated_table);
//
//        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
//        }
//
//        uploadButton.setOnClickListener(v -> openFileChooser());
//    }
//
//    private void openFileChooser() {
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//        startActivityForResult(intent, PICK_FILE_REQUEST);
//    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == PICK_FILE_REQUEST && resultCode == getActivity().RESULT_OK && data != null) {
//            Uri fileUri = data.getData();
//            try {
//                uploadFile(fileUri);
//            } catch (Exception e) {
//                e.printStackTrace();
//                Toast.makeText(getContext(), "Error reading the file", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    private void uploadFile(Uri fileUri) {
//        try {
//            InputStream inputStream = getContext().getContentResolver().openInputStream(fileUri);
//            File file = new File(getContext().getCacheDir(), "uploaded_file.xlsx");
//            OutputStream outputStream = new FileOutputStream(file);
//            byte[] buffer = new byte[1024];
//            int length;
//            while ((length = inputStream.read(buffer)) != -1) {
//                outputStream.write(buffer, 0, length);
//            }
//            inputStream.close();
//            outputStream.close();
//
//            RequestBody requestBody = RequestBody.create(MultipartBody.FORM, file);
//            MultipartBody.Part part = MultipartBody.Part.createFormData("course_outline_file", file.getName(), requestBody);
//
//            Retrofit retrofit = new Retrofit.Builder()
//                    .baseUrl("https://fd50-132-237-184-254.ngrok-free.app")
//                    .addConverterFactory(GsonConverterFactory.create())
//                    .build();
//
//            ApiService apiService = retrofit.create(ApiService.class);
//            Call<ApiResponse> call = apiService.uploadCourseOutline(part);
//
//            progressBar.setVisibility(View.VISIBLE);
//            call.enqueue(new Callback<ApiResponse>() {
//                @Override
//                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
//                    progressBar.setVisibility(View.GONE);
//                    if (response.isSuccessful() && response.body() != null) {
//                        try {
//                            File downloadedFile = downloadFileToStorage(response.body().getFileContent());
//                            Log.d(TAG, "File downloaded to: " + downloadedFile.getAbsolutePath());
//                            displayCourseOutline(new FileInputStream(new File(getContext().getCacheDir(), "uploaded_file.xlsx")), true);
//                            displayCourseOutline(new FileInputStream(downloadedFile), false);
//                            descriptionText.setText(response.body().getDescription());
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                            Toast.makeText(getContext(), "Download failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                        }
//                    } else {
//                        Toast.makeText(getContext(), "Upload failed", Toast.LENGTH_SHORT).show();
//                    }
//                }
//
//                @Override
//                public void onFailure(Call<ApiResponse> call, Throwable t) {
//                    progressBar.setVisibility(View.GONE);
//                    Toast.makeText(getContext(), "Request failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
//                }
//            });
//        } catch (IOException e) {
//            e.printStackTrace();
//            Toast.makeText(getContext(), "File upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private File downloadFileToStorage(ResponseBody responseBody) throws IOException {
//        File outputFile = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "UpdatedCourseOutline.xlsx");
//        InputStream inputStream = responseBody.byteStream();
//        OutputStream outputStream = new FileOutputStream(outputFile);
//        byte[] buffer = new byte[4096];
//        int bytesRead;
//        while ((bytesRead = inputStream.read(buffer)) != -1) {
//            outputStream.write(buffer, 0, bytesRead);
//        }
//        outputStream.flush();
//        outputStream.close();
//        inputStream.close();
//        return outputFile;
//    }
//
//    private void displayCourseOutline(InputStream inputStream, boolean isUploaded) {
//        try {
//            Workbook workbook = WorkbookFactory.create(inputStream);
//            Sheet sheet = workbook.getSheetAt(0);
//            TableLayout tableLayout = isUploaded ? uploadedTable : updatedTable;
//            tableLayout.removeAllViews();
//            for (Row row : sheet) {
//                TableRow tableRow = new TableRow(getContext());
//                for (Cell cell : row) {
//                    TextView textView = new TextView(getContext());
//                    textView.setText(cell.toString());
//                    textView.setPadding(16, 16, 16, 16);
//                    tableRow.addView(textView);
//                }
//                tableLayout.addView(tableRow);
//            }
//            tableLayout.setVisibility(View.VISIBLE);
//        } catch (Exception e) {
//            Toast.makeText(getContext(), "Error displaying courses: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//        } else {
//            Toast.makeText(getContext(), "Permission to write to storage is required", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    interface ApiService {
//        @Multipart
//        @POST("/generate_updated_courses")
//        Call<ApiResponse> uploadCourseOutline(@Part MultipartBody.Part file);
//    }
//
//    public class ApiResponse {
//        private String description;
//        private ResponseBody fileContent;
//
//        public String getDescription() {
//            return description;
//        }
//
//        public ResponseBody getFileContent() {
//            return fileContent;
//        }
//    }
//}
