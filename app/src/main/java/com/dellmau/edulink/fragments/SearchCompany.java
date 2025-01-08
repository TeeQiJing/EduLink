package com.dellmau.edulink.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import com.dellmau.edulink.R;
import com.dellmau.edulink.adapters.SearchCompanyAdapter;
import com.dellmau.edulink.adapters.SearchLessonAdapter;
import com.dellmau.edulink.models.Collaboration;
import com.dellmau.edulink.models.Company;
import com.dellmau.edulink.models.CurrentLessonCard;
import com.dellmau.edulink.models.Educator;
import com.dellmau.edulink.models.Employer;
import com.dellmau.edulink.models.Student;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchCompany#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchCompany extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private ArrayList<Company> companies;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private RecyclerView recyclerView;
    private SearchView searchView;
    private SearchCompanyAdapter searchCompanyAdapter;
    private ArrayList<CollectionReference> collections;
    private ArrayList<String> collaborations;
    private ImageView backButton;



    private Runnable searchRunnable;
    private Handler handler = new Handler(Looper.getMainLooper());
    private TextView result;

    public SearchCompany() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SearchCompany.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchCompany newInstance(String param1, String param2) {
        SearchCompany fragment = new SearchCompany();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_company, container, false);
    }

//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        result = view.findViewById(R.id.company_result);
//        db = FirebaseFirestore.getInstance();
//        mAuth = FirebaseAuth.getInstance();
//        recyclerView = view.findViewById(R.id.company_result_rec_view);
//        searchView = view.findViewById(R.id.search_company_post);
//        collections = new ArrayList<>(Arrays.asList(db.collection("employer"), db.collection("educator"), db.collection("student")));
//        collaborations = new ArrayList<>();
//        companies = new ArrayList<>();
//        searchCompanyAdapter = new SearchCompanyAdapter(requireActivity().getSupportFragmentManager());
//        recyclerView.setAdapter(searchCompanyAdapter);
//        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
//
//        result.setText("Collaborations");
//        fetchData("");
//
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                return false; // No action needed for submit
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                if (searchRunnable != null) {
//                    handler.removeCallbacks(searchRunnable); // Cancel previous execution
//                }
//
//                searchRunnable = () -> {
//                    companies.clear();
//                    collaborations.clear();
//                    if (newText.isEmpty()) {
//                        // Show companies in collaborations if query is empty
//                        result.setText("Collaborations");
//                        fetchData("");
//                    } else {
//                        Log.d("Search", "new Text: " + newText);
//                        fetchData(newText); // Fetch with filtering based on user input
//                    }
//                };
//
//                // Execute the query after 500ms
//                handler.postDelayed(searchRunnable, 500);
//                return true;
//            }
//        });
//    }
//
//    private void fetchData(String query) {
//        // Get the current user ID
//        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//        for (int i = 0; i < 3; i++) {
//            int index = i;
//            collections.get(i).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                @Override
//                public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                    if (task.isSuccessful()) {
//                        QuerySnapshot querySnapshot = task.getResult();
//                        if (!querySnapshot.isEmpty()) {
//                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
//                                Log.d("company", document.getId().toString());
//                                Log.d("company", userId);
//                                Log.d("company", String.valueOf(index));
//                                if (index == 2 && document.getId().equals(userId) ) {
//                                    Log.d("company", "yay");
//                                    DocumentReference company = document.getDocumentReference("organization");
//                                    fetchDetails(company, query);
//                                }
//                                else if (index == 1 && document.getId().equals(userId)) {
//                                    DocumentReference company = document.getDocumentReference("organization");
//                                    fetchDetails(company, query);
//                                }
//                                else if (index == 0 && document.getId().equals(userId)) {
//                                    DocumentReference company = document.getDocumentReference("organization");
//                                    fetchDetails(company, query);
//                                }
//                            }
//                        } else {
//                            System.out.println("No documents found in the collection.");
//                        }
//                    } else {
//                        System.err.println("Error getting documents: " + task.getException());
//                    }
//                }
//            });
//        }
//    }
//
//
//
//    private void fetchDetails(DocumentReference documentReference, String query) {
//        db.collection("collaboration")
//                .whereEqualTo("university", documentReference)
//                .get()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        QuerySnapshot querySnapshot = task.getResult();
//
//                        // Log the query result
//                        Log.d("details", task.toString());
//
//                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
//                            // Loop through all the documents returned
//                            for (DocumentSnapshot document : querySnapshot) {
//                                // Deserialize the document to the CurrentLessonCard object
//                                Collaboration collaboration = document.toObject(Collaboration.class);
//
//                                // Log the fetched data to help debug
//                                if (collaboration != null) {
//                                    collaborations.add(collaboration.getCompany().getId());
//                                }
//                            }
//                            Log.d("details", collaborations.get(0));
//                            filterCompany(query);
//                        } else {
//                            collaborations.clear();
//                            filterCompany(query);
//                        }
//                    } else {
//                        // Error while fetching data
//                        Log.e("Fetch Details", "Error getting documents: ", task.getException());
//
//                    }
//                });
//    }
//
//    private void filterCompany(String query) {
//        companies.clear();  // Clear old company results
//        HashSet<String> addedCompanyIds = new HashSet<>();
//
//        for (int i = 0; i < collaborations.size(); i++) {
//            db.collection("company_list")
//                    .document(collaborations.get(i))
//                    .get()
//                    .addOnCompleteListener(task -> {
//                        if (task.isSuccessful()) {
//                            DocumentSnapshot documentSnapshot = task.getResult();
//                            if (documentSnapshot != null && documentSnapshot.exists()) {
//                                String companyId = documentSnapshot.getId();
//                                String name = documentSnapshot.getString("name");
//                                if (!name.isEmpty() && name.toLowerCase().contains(query.toLowerCase())
//                                        && !addedCompanyIds.contains(companyId)) {
//                                    addedCompanyIds.add(companyId);
//                                    companies.add(documentSnapshot.toObject(Company.class));
//                                }
//                            }
//
//                            searchCompanyAdapter.setCompanies(companies);
//                        } else {
//                            Log.e("Fetch Details", "Error getting documents: ", task.getException());
//                        }
//                    });
//        }
//    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        result = view.findViewById(R.id.company_result);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        recyclerView = view.findViewById(R.id.company_result_rec_view);
        searchView = view.findViewById(R.id.search_company_post);
        collections = new ArrayList<>(Arrays.asList(db.collection("employer"), db.collection("educator"), db.collection("student")));
        collaborations = new ArrayList<>();
        companies = new ArrayList<>();
        searchCompanyAdapter = new SearchCompanyAdapter(requireActivity().getSupportFragmentManager());
        recyclerView.setAdapter(searchCompanyAdapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // Fetch all companies initially with empty query
        result.setText("Collaborations");
        fetchData("");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }

                searchRunnable = () -> {
                    companies.clear();
                    collaborations.clear();
                    if (newText.isEmpty()) {
                        result.setText("Collaborations");
                        fetchData("");  // Fetch all companies
                    } else {
                        Log.d("Search", "new Text: " + newText);
                        fetchData(newText);  // Fetch filtered companies
                    }
                };

                handler.postDelayed(searchRunnable, 500);
                return true;
            }
        });
        backButton = view.findViewById(R.id.arrow);
        backButton.setOnClickListener(v -> {
            // Use FragmentManager to navigate back
            requireActivity().getSupportFragmentManager().popBackStack();
        });

    }

    private void fetchData(String query) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        for (int i = 0; i < collections.size(); i++) {
            int index = i;
            collections.get(i).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            if (document.getId().equals(userId)) {
                                DocumentReference company = document.getDocumentReference("organization");
                                fetchDetails(company, query);
                            }
                        }
                    }
                } else {
                    Log.e("Fetch Data", "Error getting documents: ", task.getException());
                }
            });
        }
    }

    private void fetchDetails(DocumentReference documentReference, String query) {
        db.collection("collaboration")
                .whereEqualTo("university", documentReference)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                Collaboration collaboration = document.toObject(Collaboration.class);
                                if (collaboration != null) {
                                    collaborations.add(collaboration.getCompany().getId());
                                }
                            }
                        }
                        filterCompany(query);
                    } else {
                        Log.e("Fetch Details", "Error getting documents: ", task.getException());
                        filterCompany(query);
                    }
                });
    }

    private void filterCompany(String query) {
        companies.clear();
        HashSet<String> addedCompanyIds = new HashSet<>();

        if (collaborations.isEmpty()) {
            updateUIOnNoResults();
            return;
        }

        for (String companyId : collaborations) {
            db.collection("company_list")
                    .document(companyId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                String name = documentSnapshot.getString("name");
                                if (name != null && name.toLowerCase().contains(query.toLowerCase())
                                        && !addedCompanyIds.contains(companyId)) {
                                    addedCompanyIds.add(companyId);
                                    companies.add(documentSnapshot.toObject(Company.class));
                                }
                            }
                            searchCompanyAdapter.setCompanies(companies);
                            updateUIOnResults();
                        } else {
                            Log.e("Fetch Company", "Error getting documents: ", task.getException());
                        }
                    });
        }
    }

    private void updateUIOnResults() {
        if (companies.isEmpty()) {
            result.setText("No Result Found");
        } else {
            result.setText("Collaborations");
        }
    }

    private void updateUIOnNoResults() {
        searchCompanyAdapter.setCompanies(companies);
        result.setText("No Result Found");
    }
}