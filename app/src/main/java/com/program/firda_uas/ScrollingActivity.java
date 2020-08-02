package com.program.firda_uas;

import android.content.Context;
import android.os.Bundle;

import com.android.volley.AuthFailureError;
import com.program.firda_uas.user.AppController;
import com.program.firda_uas.user.Constant;
import com.program.firda_uas.user.DataModel;
import com.program.firda_uas.user.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Color;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ScrollingActivity extends AppCompatActivity {
    public RecyclerView recyclerView;
    FloatingActionButton fab;
    public ProgressBar progressBar;
    public ArrayList<DataModel> dataModelArrayList;
    public TextView empty_msg;
    DataAdapter adapter;
    android.app.AlertDialog.Builder dialog;
    public RelativeLayout layout;
    public SwipeRefreshLayout swipeRefreshLayout;
    public Snackbar snackbar;
    public Toolbar toolbar;
    public AlertDialog alertDialog;
    LayoutInflater inflater;
    EditText txt_id,txt_nim, txt_nama, txt_tlp, txt_email;
    String id,nim, nama, tlp, email;
    View dialogView;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        layout = findViewById(R.id.layout);
        toolbar = findViewById(R.id.toolBar);
        fab = (FloatingActionButton) findViewById(R.id.fab_add);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.app_name));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        empty_msg = findViewById(R.id.txtblanklist);
        progressBar = findViewById(R.id.progressBar);
        swipeRefreshLayout = findViewById(R.id.swipeLayout);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(ScrollingActivity.this));
        getData();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        // tombol melayang untuk menambahkan data mahasiswa
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogForm("", "", "", "", "","SIMPAN");
            }
        });
    }
    private void getData() {
        progressBar.setVisibility(View.VISIBLE);
        if (Utils.isNetworkAvailable(ScrollingActivity.this)) {
            getMainCategoryFromJson();
            invalidateOptionsMenu();
        } else {
            setSnackBar();
            progressBar.setVisibility(View.GONE);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void getMainCategoryFromJson() {
        progressBar.setVisibility(View.VISIBLE);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constant.URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            dataModelArrayList = new ArrayList<>();
                            JSONObject jsonObject = new JSONObject(response);
                            String error = jsonObject.getString(Constant.ERROR);
                            if (error.equalsIgnoreCase("false")) {
                                empty_msg.setVisibility(View.GONE);
                                JSONArray jsonArray = jsonObject.getJSONArray(Constant.DATA);
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    DataModel dataModel = new DataModel();
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    dataModel.setId(object.getString("id"));
                                    dataModel.setNim(object.getString("nim"));
                                    dataModel.setNama(object.getString("nama"));
                                    dataModel.setTelepon(object.getString("telepon"));
                                    dataModel.setEmail(object.getString("email"));
                                    dataModelArrayList.add(dataModel);
                                }
                                adapter = new DataAdapter(ScrollingActivity.this, dataModelArrayList);
                                recyclerView.setAdapter(adapter);
                                progressBar.setVisibility(View.GONE);
                            } else {
                                empty_msg.setText(getString(R.string.no_data));
                                progressBar.setVisibility(View.GONE);
                                empty_msg.setVisibility(View.VISIBLE);
                                if (adapter != null) {
                                    adapter = new DataAdapter(ScrollingActivity.this, dataModelArrayList);
                                    recyclerView.setAdapter(adapter);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ScrollingActivity.this,"ini err: "+error,Toast.LENGTH_LONG).show();
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put(Constant.accessKey, Constant.accessKeyValue);
                params.put(Constant.getdata_mhs, "1");
                return params;
            }
        };

        AppController.getInstance().getRequestQueue().getCache().clear();
        AppController.getInstance().addToRequestQueue(stringRequest);
    }
    public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ItemRowHolder> {
        private ArrayList<DataModel> dataList;
        private Context mContext;
        public DataAdapter(Context context, ArrayList<DataModel> dataList) {
            this.dataList = dataList;
            this.mContext = context;
        }
        @Override
        public DataAdapter.ItemRowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            return new ItemRowHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull DataAdapter.ItemRowHolder holder, final int position) {
            final DataModel dataModel = dataList.get(position);
            holder.id.setText(dataModel.getId());
            holder.text.setText(dataModel.getNama());
            holder.email.setText(dataModel.getEmail());
            holder.telepon.setText(dataModel.getTelepon());
            holder.nim.setText(dataModel.getNim());
            holder.image.setDefaultImageResId(R.mipmap.ic_user);
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    final String idx = dataList.get(position).getId();
                    final String nimx = dataList.get(position).getNim();
                    final String namax = dataList.get(position).getNama();
                    final String tlpx = dataList.get(position).getTelepon();
                    final String emailx = dataList.get(position).getEmail();

                    final CharSequence[] dialogitem = {"Ubah", "Hapus"};
                    dialog = new android.app.AlertDialog.Builder(ScrollingActivity.this);
                    dialog.setCancelable(true);
                    dialog.setItems(dialogitem, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            switch (which) {
                                case 0:
                                    edit(idx,nimx,namax,tlpx,emailx);
                                    break;
                                case 1:
                                    delete(idx);
                                    break;
                            }
                        }
                    }).show();
                    return false;
                }

            });
        }
        @Override
        public int getItemCount() {
            return dataList.size();
        }
        @Override
        public int getItemViewType(int position) {
            return position;
        }

        public class ItemRowHolder extends RecyclerView.ViewHolder {
            public NetworkImageView image;
            public TextView id,text,nama,nim,email,telepon;
            RelativeLayout relativeLayout;
            public ItemRowHolder(View itemView) {
                super(itemView);
                image = itemView.findViewById(R.id.cateImg);
                id=itemView.findViewById(R.id.id);
                nim = itemView.findViewById(R.id.nim);
                text = itemView.findViewById(R.id.nama);
                email= itemView.findViewById(R.id.email);
                telepon=itemView.findViewById(R.id.tlp);
                relativeLayout = itemView.findViewById(R.id.cat_layout);
            }
        }
    }
    public void setSnackBar() {
        snackbar = Snackbar
                .make(findViewById(android.R.id.content), "no internet connection", Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(R.string.retry), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getData();
                    }
                });

        snackbar.setActionTextColor(Color.RED);
        snackbar.show();
    }
    private void DialogForm(String idx, String nimx, String namax, String tlpx, String emailx, String button) {
        dialog = new android.app.AlertDialog.Builder(ScrollingActivity.this);
        inflater = getLayoutInflater();
        dialogView = inflater.inflate(R.layout.form_input, null);
        dialog.setView(dialogView);
        dialog.setCancelable(true);
        dialog.setIcon(R.mipmap.ic_user);
        dialog.setTitle("Data Barang");

        txt_id = (EditText) dialogView.findViewById(R.id.txt_id);
        txt_nim = (EditText) dialogView.findViewById(R.id.txt_nim);
        txt_nama = (EditText) dialogView.findViewById(R.id.txt_nama);
        txt_tlp = (EditText) dialogView.findViewById(R.id.txt_tlp);
        txt_email = (EditText) dialogView.findViewById(R.id.txt_email);

        if (!idx.isEmpty()) {
            txt_id.setText(idx);
            txt_nim.setText(nimx);
            txt_nama.setText(namax);
            txt_tlp.setText(tlpx);
            txt_email.setText(emailx);
        } else {
            setblank();
        }

        dialog.setPositiveButton(button, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                id = txt_id.getText().toString();
                nim = txt_nim.getText().toString();
                nama = txt_nama.getText().toString();
                tlp = txt_tlp.getText().toString();
                email = txt_email.getText().toString();
                simpan_data();
                dialog.dismiss();
            }
        });

        dialog.setNegativeButton("BATAL", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                setblank();
            }
        });

        dialog.show();
    }
    private void setblank() {
        txt_id.setText(null);
        txt_nama.setText(null);
        txt_tlp.setText(null);
        txt_email.setText(null);
    }
    private void simpan_data() {
        progressBar.setVisibility(View.VISIBLE);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constant.URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //  Toast.makeText(ScrollingActivity.this, "ini masuk onrespon", Toast.LENGTH_LONG).show();
                        try {
                            dataModelArrayList = new ArrayList<>();
                            JSONObject jsonObject = new JSONObject(response);
                            String error = jsonObject.getString(Constant.ERROR);

                            if (error.equalsIgnoreCase("false")) {
                                empty_msg.setVisibility(View.GONE);
                                //alertDialog.dismiss();
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(ScrollingActivity.this, "Data Berhasil disimpan", Toast.LENGTH_LONG).show();
                                getData();
                            } else {
                                Toast.makeText(ScrollingActivity.this, "Data gagal disimpan: "+id, Toast.LENGTH_LONG).show();


                                empty_msg.setText( getString(R.string.no_data));
                                progressBar.setVisibility(View.GONE);
                                empty_msg.setVisibility(View.VISIBLE);

                                if (adapter != null) {
                                    adapter = new DataAdapter(ScrollingActivity.this, dataModelArrayList);
                                    recyclerView.setAdapter(adapter);
                                }


                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ScrollingActivity.this,"ini err: "+error,Toast.LENGTH_LONG).show();
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put(Constant.accessKey, Constant.accessKeyValue);
                if (id.isEmpty()) {
                    params.put(Constant.setdata_mhs, "1");
                }else {
                    params.put(Constant.editdata_mhs,"1");
                    params.put(Constant.id,id);
                }
                params.put(Constant.nim,nim);
                params.put(Constant.nama,nama);
                params.put(Constant.telepon,tlp);
                params.put(Constant.email,email);
                return params;

            }
        };

        AppController.getInstance().getRequestQueue().getCache().clear();
        AppController.getInstance().addToRequestQueue(stringRequest);
    }
    private void edit(final String idx, String nimx,String namax, String tlpx, String emailx) {

        DialogForm(idx,nimx, namax, tlpx, emailx, "UPDATE");

    }
    private void delete(final String idx) {
        progressBar.setVisibility(View.VISIBLE);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constant.URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //  Toast.makeText(ScrollingActivity.this, "ini masuk onrespon", Toast.LENGTH_LONG).show();
                        try {
                            dataModelArrayList = new ArrayList<>();
                            JSONObject jsonObject = new JSONObject(response);
                            String error = jsonObject.getString(Constant.ERROR);

                            if (error.equalsIgnoreCase("false")) {
                                empty_msg.setVisibility(View.GONE);
                                //alertDialog.dismiss();
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(ScrollingActivity.this, "Data Berhasil dihapus", Toast.LENGTH_LONG).show();
                                getData();
                            } else {


                                empty_msg.setText(getString(R.string.no_data));
                                progressBar.setVisibility(View.GONE);
                                empty_msg.setVisibility(View.VISIBLE);

                                if (adapter != null) {
                                    adapter = new DataAdapter(ScrollingActivity.this, dataModelArrayList);
                                    recyclerView.setAdapter(adapter);
                                }


                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ScrollingActivity.this,"ini err: "+error,Toast.LENGTH_LONG).show();
                    }
                }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put(Constant.accessKey, Constant.accessKeyValue);
                params.put(Constant.id,idx);
                params.put(Constant.deletedata_mhs,"1");
                return params;

            }
        };

        AppController.getInstance().getRequestQueue().getCache().clear();
        AppController.getInstance().addToRequestQueue(stringRequest);
    }
}

