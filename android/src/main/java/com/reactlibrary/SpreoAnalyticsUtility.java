package com.reactlibrary;

import android.os.AsyncTask;

import com.mlins.nav.location.sharing.SharedLocation;
import com.mlins.recorder.WlBlipsRecorder;
import com.mlins.utils.PropertyHolder;
import com.spreo.nav.interfaces.ILocation;
import com.spreo.sdk.data.Analytics;
import com.spreo.sdk.data.SpreoDataProvider;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SpreoAnalyticsUtility {
    public static final String NAVIGATE_REPORT = "navigate";
    public static final String SEARCH_REPORT = "search";
    public static final String FIRST_RUN_REPORT = "first_run";
    public static final String NEW_SESSION_REPORT = "new_session";
    private static  final String LOCATION_SERVER_ADDRESS = PropertyHolder.getInstance().getStreamServerName() + "location?req=0";
    public static boolean newSessionSent = false;
    public static boolean enabled = false;

    public static void sendReport(String action, String data, String facid) {
        if (enabled) {
            String campusId = SpreoDataProvider.getCampusId();
            if (campusId != null) {
                if (facid == null) {
                    facid = SpreoDataProvider.getFacilityId();
                }

                Analytics.getInstance().sendReport(action, data, campusId, facid);
            }
        }

    }

    public static String generateRandomID() {
//        return Long.toString(new Random().nextLong());
        return WlBlipsRecorder.getInstance().getSessionId();
    }


    public static void sendLocation(ILocation loc) {
        SharedLocation sLocation = new SharedLocation(generateRandomID(), loc);
        SpreoSendLocationTask task = new SpreoSendLocationTask(sLocation);
        task.execute();
    }

    private static class SpreoSendLocationTask extends
            AsyncTask<String, Void, Void> {

        private SharedLocation sharedLoc = null;


        public SpreoSendLocationTask(SharedLocation sharedLoc) {
            super();
            this.sharedLoc = sharedLoc;
        }

        @Override
        protected Void doInBackground(String... req) {

            try {

                if (sharedLoc == null) {
                    return null;
                }

                String uploadData = sharedLoc.getAsJsonString();

                // Set the timeout in milliseconds until a connection is established.
                // The default value is zero, that means the timeout is not used.
                int timeoutConnection = 10000;

                URL obj = new URL(LOCATION_SERVER_ADDRESS);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setConnectTimeout(timeoutConnection);

                // add reuqest header
                con.setRequestMethod("POST");

                con.setRequestProperty("charset", "utf-8");

                con.setRequestProperty("Content-Length", Integer.toString(uploadData.length()));

                // Send post request
                con.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                wr.writeBytes(uploadData);
                wr.flush();
                wr.close();

                int responseCode = con.getResponseCode();
                //System.out.println("\nSending 'POST' request to URL : " + SERVER_ADDRESS);
                //System.out.println("Post parameters : " + uploadData);
                //System.out.println("Response Code : " + responseCode);

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                // print result
                //System.out.println(response.toString());

            } catch (Throwable e) {
                e.printStackTrace();
                // something went wrong. connection with the server error
            }
            return null;
        }

    }
}
