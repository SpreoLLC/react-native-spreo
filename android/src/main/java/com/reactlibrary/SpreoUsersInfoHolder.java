package com.reactlibrary;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.mlins.utils.PropertyHolder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.facebook.react.bridge.Callback;

public class SpreoUsersInfoHolder {


    private static SpreoUsersInfoHolder instance = null;
    private final String SERVER_ADDRESS = PropertyHolder.getInstance()
            .getServerName() + "res/" + "spreo_users_setting.json";
    public String userName, userPass;
    //	private Map<String, List<SpreoApiKeyConfigsObj>> usersMap = new HashMap<String, List<SpreoApiKeyConfigsObj>>();
    ArrayList<spreo.spreomobile.SpreoApiKeyConfigsObj> apiList = new ArrayList<spreo.spreomobile.SpreoApiKeyConfigsObj>();
    private Map<String, String> projectToKeyMap = new HashMap<String, String>();
    private ArrayList<spreo.spreomobile.SpreoOnUserDataDownloadedCallBack> mOnDataDownloadedCallBack = new ArrayList<spreo.spreomobile.SpreoOnUserDataDownloadedCallBack>();
    private String selectedKey = null;
    private String selectedLanguage = "english";
    private String dailogButtonsColor = "#1a4da0";

    public SpreoUsersInfoHolder() {
        super();
    }

    public static SpreoUsersInfoHolder getInstance() {
        if (instance == null) {
            instance = new SpreoUsersInfoHolder();
        }
        return instance;
    }

    public static void releaseInstance() {
        if (instance != null) {
            instance.clean();
            instance = null;
        }
    }

    public void setOnDataDownloaded(spreo.spreomobile.SpreoOnUserDataDownloadedCallBack listener) {
        mOnDataDownloadedCallBack.add(listener);
    }

    public void load(Context ctx, String name, String pass, Callback callback) {
        userName = name;
        userPass = pass;
        //ContactServerTask task = new ContactServerTask(ctx,callback);
       // task.execute();
    }

    private void clean() {

    }

    private void parseUsersData(String usersJsonData) {

        if (usersJsonData == null) {
            for (spreo.spreomobile.SpreoOnUserDataDownloadedCallBack listener : mOnDataDownloadedCallBack) {
                listener.OnUserDataDownloaded("failed");
            }
            return;
        }
        try {
            JSONTokener tokener = new JSONTokener(usersJsonData);
            JSONObject json = (JSONObject) tokener.nextValue();
            JSONObject obj = new JSONObject(usersJsonData);
            JSONArray m_jArry = new JSONArray();
            m_jArry = obj.getJSONArray("projects");
            apiList.clear();
            for (int i = 0; i < m_jArry.length(); i++) {
                try {
                    JSONObject jo_inside = m_jArry.getJSONObject(i);

                    String name = (jo_inside.getString("name"));
                    String key = (jo_inside.getString("key"));
                    projectToKeyMap.put(name, key);
                    spreo.spreomobile.SpreoApiKeyConfigsObj conf = new spreo.spreomobile.SpreoApiKeyConfigsObj();
                    conf.parse(jo_inside);
                    apiList.add(conf);
                } catch (Exception e) {
                    Log.e("Exception", e.toString());
                }
            }

            for (spreo.spreomobile.SpreoOnUserDataDownloadedCallBack listener : mOnDataDownloadedCallBack) {
                listener.OnUserDataDownloaded("ok");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public String getApiKeyByProjectName(String projectName) {
        return projectToKeyMap.get(projectName);
    }

    public List<spreo.spreomobile.SpreoApiKeyConfigsObj> getUserConfigs() {
        // TODO Auto-generated method stub
        return apiList;
    }

    public String getSelectedKey() {
        return selectedKey;
    }

    public void setSelectedKey(String selectedKey) {
        this.selectedKey = selectedKey;
    }

    public String getSelectedLanguage() {
        return selectedLanguage;
    }

    public void setSelectedLanguage(String selectedLanguage) {
        this.selectedLanguage = selectedLanguage;
    }

    // send to server
    private class ContactServerTask extends AsyncTask<String, String, String> {

        private ProgressDialog dialog = null;
        private Context ctx = null;
        private Callback callback = null;

        public ContactServerTask(Context ctx, Callback callback) {
            super();
            this.ctx = ctx;
            this.callback = callback;
        }

        @Override
        protected void onPreExecute() {
            try {
//                dialog = new ProgressDialog(ctx);
//                dialog.setCancelable(false);
//                dialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(String... req) {
            String base = PropertyHolder.getInstance().getServerName();
            String SERVERADDRESS = base + "auth?req=1&name="
                    + userName + "&pass=" + userPass;// PropertyHolder.getInstance().getServerName()
            // + "beacons";
            String status;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = null;
            Log.d("sriom", "do in back download start "+SERVERADDRESS);
            try {
                URL url = new URL(SERVERADDRESS);

                // Set the timeout in milliseconds until a connection is
                // established.
                // The default value is zero, that means the timeout is not
                // used.
                int timeoutConnection = 10000;
                // Set the default socket timeout (SO_TIMEOUT)
                // in milliseconds which is the timeout for waiting for data.
                int timeoutSocket = 10000;

                URLConnection conn = url.openConnection();
                conn.setConnectTimeout(timeoutConnection);
                conn.setReadTimeout(timeoutSocket);
                in = conn.getInputStream();

                byte[] buffer = new byte[4096];
                int n = -1;

                while ((n = in.read(buffer)) != -1) {
                    if (n > 0) {
                        out.write(buffer, 0, n);
                    }
                }
                Log.d("sriom", "do in back download complete "+SERVERADDRESS);
            } catch (Throwable t) {
                t.printStackTrace();

            } finally {
                try {
                    if (in != null)
                        in.close();
                    if (out != null)
                        out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            byte[] data = out.toByteArray();
            String usersJsonData = new String(data);
            Log.d("sriom", "user json data "+usersJsonData);
            return usersJsonData;
        }

        @Override
        protected void onPostExecute(String usersJsonData) {

            super.onPostExecute(usersJsonData);
            callback.invoke("done");
            Log.d("response", "done");
            if (usersJsonData == null || usersJsonData.equals("")) {
                Toast.makeText(ctx, "Network connection failed",
                        Toast.LENGTH_LONG).show();
                parseUsersData(null);
            } else {

                JSONObject obj;
                try {
                    obj = new JSONObject(usersJsonData);

                    if ("ok".equals(obj.getString("status"))) {
                        parseUsersData(usersJsonData);
                    } else {
//                        if (dialog != null) {
//                            dialog.dismiss();
//                        }
                        parseUsersData(null);
                        AlertDialog dialog;
                        AlertDialog.Builder db = new AlertDialog.Builder(ctx);
                        db.setTitle("The User Name OR Password you entered is incorrect");
                        db.setNegativeButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                    }
                                });
                        dialog = db.show();
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.parseColor(dailogButtonsColor));
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor(dailogButtonsColor));
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch blocFk
                    e.printStackTrace();
                    parseUsersData(null);
                }
            }

//            if (dialog != null) {
//                dialog.dismiss();
//            }

        }

    }

}
