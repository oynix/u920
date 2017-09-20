package com.oy.u920;

import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.oy.u920.imageloader.IconLoader;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IconLoader.ensureInitSingleton(getApplicationContext());
        IconLoader.getInstance().bindServicer(this);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        List<PackageInfo> installedPackages = getPackageManager().getInstalledPackages(0);
        recyclerView.setAdapter(new PkgAdapter(installedPackages));
    }

    @Override
    protected void onDestroy() {
        IconLoader.getInstance().unbindServicer(this);
        super.onDestroy();
    }

    private class PkgAdapter extends RecyclerView.Adapter<PkgAdapter.MyHolder> {

        private List<PackageInfo> mListData;

        PkgAdapter(List<PackageInfo> infos) {
            mListData = infos;
        }

        @Override
        public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = View.inflate(getApplicationContext(), R.layout.item, null);
            return new MyHolder(view);
        }

        @Override
        public void onBindViewHolder(MyHolder holder, int position) {
            PackageInfo packageInfo = mListData.get(position);
            CharSequence name = packageInfo.applicationInfo.loadLabel(getPackageManager());
            holder.name.setText(name);
            IconLoader.getInstance().displayImage(packageInfo.packageName, holder.icon);
        }

        @Override
        public int getItemCount() {
            return mListData.size();
        }

        class MyHolder extends RecyclerView.ViewHolder {

            ImageView icon;
            TextView name;

            MyHolder(View itemView) {
                super(itemView);
                icon = itemView.findViewById(R.id.icon);
                name = itemView.findViewById(R.id.name);
            }
        }
    }
}
