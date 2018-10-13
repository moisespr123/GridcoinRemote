package mcr.apps.gridcoinremote;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private static final String COMMAND_GET_NEW_ADDRESS = "getnewaddress";
    String BalanceString = "N/A";
    String AddressString = "Address Unknown";
    String blocksString = "0";
    String PoRDiff = "0";
    String NetWeight = "0";
    String CPIDString = "N/A";
    String GRCMagUnit = "0";
    String ClientVersion = "0.0.0.0";
    String NodeConnections = "0";
    String MyMag = "0";
    boolean ErrorInDataGathering = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawerlistbox, getResources().getStringArray(R.array.drawerMainActivityList)));
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        mDrawerToggle = new ActionBarDrawerToggle((Activity) this, mDrawerLayout, R.drawable.ic_drawer, 0, 0) {
            public void onDrawerClosed(View view) {
                getWindow().getDecorView().findViewById(android.R.id.content).invalidate();
            }

            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu();
                mDrawerLayout.bringToFront();
            }

            public void onDrawerSlide(View drawerView, float offset) {
                if (offset != 0)
                    mDrawerLayout.bringToFront();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_drawer);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerList.setItemChecked(0, true);
        mDrawerToggle.syncState();
        if (!SignIn.SignInformationFilled) {
            SharedPreferences settings = getSharedPreferences("grcremote", MODE_PRIVATE);
            SignIn.ipFieldString = settings.getString("ip", "");
            SignIn.portFieldString = settings.getString("port", "");
            SignIn.UsernameFieldString = settings.getString("username", "");
            SignIn.PasswordFieldString = settings.getString("password", "");
            SignIn.RememberChecked = true;
            if (SignIn.ipFieldString.equals("") || SignIn.portFieldString.equals("") || SignIn.UsernameFieldString.equals("") || SignIn.PasswordFieldString.equals("")) {
                Intent login = new Intent(MainActivity.this, SignIn.class);
                startActivity(login);
            } else {
                SignIn.SignInformationFilled = true;
            }
        }
        if (SignIn.SignInformationFilled) {
            new LoadData().execute();
        }
    }

    private class LoadData extends AsyncTask<Void, String, Void> {
        ProgressDialog dialog = new ProgressDialog(MainActivity.this);

        protected void onPreExecute() {
            dialog.setMessage("Loading Wallet Data...");
            dialog.show();
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            super.onPreExecute();
        }

        protected Void doInBackground(Void... params) {
            try {
                BalanceString = getBalance();
                AddressString = getAddress();
                getMiningInfo();
                getInfo();
                getMyMag();
            } catch (Exception e) {
                ErrorInDataGathering = true;
            }
            return null;
        }

        protected void onProgressUpdate(String... params) {
            dialog.setMessage(params[0]);
        }

        protected void onPostExecute(Void result) {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
            if (ErrorInDataGathering) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Error");
                builder.setMessage("Could not connect to wallet. Please verify that the wallet is running and that the server information is correct.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                SignIn.EditMode = true;
                                Intent signin = new Intent(MainActivity.this, SignIn.class);
                                startActivity(signin);
                            }
                        });
                AlertDialog notify = builder.create();
                notify.show();

            } else {
                final TextView balanceText = findViewById(R.id.balance);
                final TextView addressText = findViewById(R.id.address);
                final TextView BlockText = findViewById(R.id.blocks);
                final TextView PoRText = findViewById(R.id.PoRDifficulty);
                final TextView NetWeightText = findViewById(R.id.NetWeight);
                final TextView CPIDText = findViewById(R.id.CPIDText);
                final TextView GRCMagText = findViewById(R.id.MagUnit);
                final TextView MyMagText = findViewById(R.id.MyMag);
                final TextView ClientVersionText = findViewById(R.id.version);
                final TextView ConnectionsText = findViewById(R.id.Connections);
                balanceText.setText(String.format("Balance: %s GRC", BalanceString));
                addressText.setText(AddressString);
                BlockText.setText(String.format("Blocks: %s", blocksString));
                PoRText.setText(String.format("PoR Difficulty: %s", PoRDiff));
                NetWeightText.setText(String.format("Net Weight: %s", NetWeight));
                CPIDText.setText(String.format("CPID: %s", CPIDString));
                GRCMagText.setText(String.format("GRC Mag Unit: %s", GRCMagUnit));
                MyMagText.setText(String.format("My Magnitude: %s", MyMag));
                ClientVersionText.setText(String.format("Client Version: %s", ClientVersion));
                ConnectionsText.setText(String.format("Connections: %s", NodeConnections));
            }
        }
    }

    private JSONObject invokeRPC(String id, String method, List<String> params) {
        DefaultHttpClient httpclient = new DefaultHttpClient();

        JSONObject json = new JSONObject();
        json.put("method", method);
        if (null != params) {
            JSONArray array = new JSONArray();
            array.addAll(params);
            json.put("params", params);
        }
        JSONObject responseJsonObj = null;
        try {
            StringEntity myEntity = new StringEntity(json.toJSONString());
            System.out.println(json.toString());
            HttpPost httppost = new HttpPost("http://" + SignIn.ipFieldString + ":" + SignIn.portFieldString);
            httppost.setEntity(myEntity);
            final String basicAuth = "Basic " + Base64.encodeToString((SignIn.UsernameFieldString + ":" + SignIn.PasswordFieldString).getBytes(), Base64.NO_WRAP);
            httppost.setHeader("Authorization", basicAuth);
            System.out.println("executing request" + httppost.getRequestLine());
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            System.out.println("----------------------------------------");
            System.out.println(response.getStatusLine());
            if (entity != null) {
                System.out.println("Response content length: " + entity.getContentLength());
            }
            JSONParser parser = new JSONParser();
            responseJsonObj = (JSONObject) parser.parse(EntityUtils.toString(entity));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            httpclient.getConnectionManager().shutdown();
        }
        return responseJsonObj;
    }

    public String getBalance() {
        JSONObject json = invokeRPC(UUID.randomUUID().toString(), "getbalance", null);
        return json.get("result").toString();
    }

    public String getNewAddress(String account) {
        String[] params = {account};
        JSONObject json = invokeRPC(UUID.randomUUID().toString(), COMMAND_GET_NEW_ADDRESS, Arrays.asList(params));
        return (String) json.get("result");
    }

    public String getAddress() {
        JSONObject json = invokeRPC(UUID.randomUUID().toString(), "listreceivedbyaddress", null);
        JSONArray param = (JSONArray) json.get("result");
        //Iterator i = param.iterator();
        for (int i = 0; i < param.size(); i++) {
            JSONObject item = (JSONObject) param.get(i);
            if (item.get("involvesWatchonly") != null) {
                if ((boolean) item.get("involvesWatchonly"))
                    return (String) item.get("address");
            }
        }
        return "";
    }

    public void getMiningInfo() {
        JSONObject json = invokeRPC(UUID.randomUUID().toString(), "getmininginfo", null);
        JSONObject json2 = (JSONObject) json.get("result");
        blocksString = json2.get("blocks").toString();
        CPIDString = json2.get("CPID").toString();
        GRCMagUnit = json2.get("Magnitude Unit").toString();
        double NetWeightDouble = (double) json2.get("netstakeweight");
        NetWeight = BigDecimal.valueOf(NetWeightDouble).toPlainString();
        JSONObject json3 = (JSONObject) json2.get("difficulty");
        PoRDiff = json3.get("proof-of-stake").toString();
    }

    public void getMyMag() {
        JSONObject json = invokeRPC(UUID.randomUUID().toString(), "mymagnitude", null);
        JSONArray array = (JSONArray) json.get("result");
        JSONObject json2 = (JSONObject) array.get(1);
        MyMag = json2.get("Magnitude (Last Superblock)").toString();
    }

    public void getInfo() {
        JSONObject json = invokeRPC(UUID.randomUUID().toString(), "getinfo", null);
        JSONObject json2 = (JSONObject) json.get("result");
        ClientVersion = json2.get("version").toString();
        NodeConnections = json2.get("connections").toString();
    }

    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        //noinspection SimplifiableIfStatement
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position == 1) //Settings
            {
                SignIn.EditMode = true;
                Intent signin = new Intent(MainActivity.this, SignIn.class);
                startActivity(signin);
            }
            if (position == 2) //About App
            {
                Intent about = new Intent(MainActivity.this, AboutApp.class);
                startActivity(about);
            }

            selectItem(position);
        }

    }

    private void selectItem(int position) {
        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void RefreshData(MenuItem item) {
        new LoadData().execute();
    }
}
