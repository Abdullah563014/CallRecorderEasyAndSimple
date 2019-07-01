package md.habibure.dhaka.callrecorder_easyandsimple.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import md.habibure.dhaka.callrecorder_easyandsimple.model.CallListModelClass;
import md.habibure.dhaka.callrecorder_easyandsimple.database.Database;
import md.habibure.dhaka.callrecorder_easyandsimple.model.DatabaseModelClass;
import md.habibure.dhaka.callrecorder_easyandsimple.model.MainModelClass;
import md.habibure.dhaka.callrecorder_easyandsimple.R;
import md.habibure.dhaka.callrecorder_easyandsimple.adapter.AllCallRecyclerViewAdapter;

public class AllCallFragment extends Fragment {

    View view;
    public RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<MainModelClass> arrayList;
    ArrayList<CallListModelClass> callArrayList;
    ArrayList<DatabaseModelClass> allData = new ArrayList<>();
    Database database;
    String userDate;
    String userMonth;
    String userYear;
    ArrayList<String> temporaryIdList;
    int allDataSize;


    public AllCallFragment() {
    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        arrayList = new ArrayList<MainModelClass>();
        loadSqliteDatabase();
        categorizedUserAllData();


//======================================All data collection completed here============================
//        loadSqliteDatabase();
//        categorizedUserAllData();


        /*
        Initialize all data here for recycler view
         */
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.all_call_fragment, container, false);
        Date date = new Date();
        String userDate = (String) DateFormat.format("dd", date.getTime());
        String userMonth = (String) DateFormat.format("MM", date.getTime());
        recyclerView = view.findViewById(R.id.allCallRecyclerViewId);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setVerticalScrollBarEnabled(true);
        mAdapter = new AllCallRecyclerViewAdapter(getContext(), arrayList, userDate, userMonth);
        recyclerView.setAdapter(mAdapter);
        recyclerView.smoothScrollToPosition(0);
        recyclerView.setVerticalScrollbarPosition(0);
        return view;
    }


    public void loadSqliteDatabase() {
        getDatabaseInstance();
        database.initializedDatabase();
        Cursor cursor = database.getAllData();
        allData.clear();
        String id;
        String date;
        String month;
        String year;
        String callIndicator;
        String duration;
        String name;
        String time;
        String file;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                id = cursor.getString(0);
                date = cursor.getString(1);
                month = cursor.getString(2);
                year = cursor.getString(3);
                callIndicator = cursor.getString(4);
                duration = cursor.getString(5);
                name = cursor.getString(6);
                time = cursor.getString(7);
                file = cursor.getString(8);

                DatabaseModelClass databaseModelClass = new DatabaseModelClass(id, date, month, year, callIndicator, duration, name, time, file);
                allData.add(databaseModelClass);
            }
            Toast.makeText(getContext(), "All Call data found " + allData.size(), Toast.LENGTH_SHORT).show();
            cursor.close();
        } else {
            Toast.makeText(getContext(), "No data found yet", Toast.LENGTH_SHORT).show();
        }
    }


    public void categorizedUserAllData() {

        temporaryIdList = new ArrayList<>();


        helperOFCategorizedMethod();

        while (allData.size() != 0) {
            temporaryIdList.clear();
            callArrayList = new ArrayList<>();
//            callArrayList.clear();
            for (int i = 0; i < allData.size(); i++) {
                if (allData.get(i).getYear().equalsIgnoreCase(userYear) && allData.get(i).getMonth().equalsIgnoreCase(userMonth) && allData.get(i).getDate().equalsIgnoreCase(userDate)) {
                    CallListModelClass callListModelClass = new CallListModelClass(allData.get(i).getCallIndicator(), allData.get(i).getDuration(), allData.get(i).getName(), allData.get(i).getTime(), allData.get(i).getFile());
                    callArrayList.add(callListModelClass);
                    temporaryIdList.add(allData.get(i).getId());
                }
                if (allData.size() == (i + 1)) {
                    arrayList.add(new MainModelClass(userDate, userMonth, userYear, callArrayList));
//                    callArrayList.clear();
                    for (int j = 0; j < temporaryIdList.size(); j++) {
                        int k;
                        for (k = 0; k < allData.size(); k++) {
                            if (temporaryIdList.get(j).equalsIgnoreCase(allData.get(k).getId())) {
                                allData.remove(k);
                                k = 0;
                            }
                        }
                    }
                    if (!allData.isEmpty()) {
                        helperOFCategorizedMethod();
                    }
                    temporaryIdList.clear();
                }
            }
            allDataSize = allDataSize - 1;
        }

//        mAdapter.notifyDataSetChanged();


    }

    public void helperOFCategorizedMethod() {
//        if (!callArrayList.isEmpty()){
//            callArrayList.clear();
//        }


        if (!allData.isEmpty()) {
            userYear = allData.get(0).getYear();
            userMonth = allData.get(0).getMonth();
            userDate = allData.get(0).getDate();
            allDataSize = allData.size();
        } else {
            allDataSize = 0;
        }
    }






    public boolean deleteFileFormStorage(String path) {
        File file = new File(path);
        boolean result = false;
        if (file.exists()) {
            result = file.delete();
        }
        return result;
    }


    public boolean deleteFileFormDatabase(String path) {

        boolean result = false;
        if (database!=null){
            int value=database.deleteData(path);
            if (value>=0){
                result=true;
            }
        }
        return result;
    }


    public Database getDatabaseInstance(){
        if (database==null){
            database=new Database(getContext());
        }
        return database;
    }

//======================================Lifecycle method start from here===============================

    @Override
    public void onDestroy() {
        if (database != null) {
            database.closeDatabase();
            database.close();
        }
        super.onDestroy();
    }

}
