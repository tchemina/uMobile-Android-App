package org.apereo.utils;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apereo.App;
import org.apereo.services.UmobileRestCallback;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ajclisso on 12/4/14.
 */
@EBean
public class CasClient {

    private static final String TAG = CasClient.class.getName();

    @Background
    public void authenticate(String username, String password, UmobileRestCallback<String> callback) {
        URL url;
        BufferedReader reader;
        String lt = null;
        String execution = null;
        String cookie = null;
        String portletHeader = null;

        HttpURLConnection getConnection = null;
        HttpURLConnection postConnection = null;
        String postPath = "https://cas.oakland.edu/cas/v1/tickets";
        URL postUrl;

        try {
            // Auth success and TGT Created
            url = new URL("https://cas.oakland.edu/cas/login?service=https://mysail.oakland.edu/uPortal/Login");
            getConnection = (HttpURLConnection) url.openConnection();
            reader = new BufferedReader(new InputStreamReader(getConnection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("<input type=\"hidden\" name=\"lt\" value=")) {
                    lt = line.substring(41, line.lastIndexOf("\""));
                }
                if (line.contains("<input type=\"hidden\" name=\"execution\" value=\"")) {
                    execution = line.substring(48, line.lastIndexOf("\""));
                }
            }
            cookie = getConnection.getHeaderField("Set-Cookie");

            Logger.d(TAG, "POSTING TO: " + postPath);
            postUrl = new URL(postPath);
            postConnection = (HttpURLConnection) postUrl.openConnection();
            postConnection.setInstanceFollowRedirects(true);
            postConnection.setRequestProperty("Cookie", cookie);
            HttpURLConnection.setFollowRedirects(true);
            List<NameValuePair> postData = new ArrayList<NameValuePair>(6);
            postData.add(new BasicNameValuePair("username", username));
            postData.add(new BasicNameValuePair("password", password));
            postData.add(new BasicNameValuePair("lt", lt));
            postData.add(new BasicNameValuePair("execution", execution));
            postData.add(new BasicNameValuePair("_eventId", "submit"));
            postData.add(new BasicNameValuePair("submit", "Sign In"));
            postConnection.setDoOutput(true);
            postConnection.setChunkedStreamingMode(0);
            OutputStream os = new BufferedOutputStream(postConnection.getOutputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            writer.write(getQuery(postData));
            writer.flush();
            writer.close();
            os.close();
            postConnection.connect();
            Logger.d(TAG, "" + postConnection.getHeaderFields());
            Logger.d(TAG, "End sending POST");

            // Service Ticket Created
            String requestST = postConnection.getHeaderField("Location");
            Logger.d(TAG, requestST);
            String tgt = requestST.split("tickets/")[1];
            requestST = requestST.replace("http", "https");
            Logger.d(TAG, "POSTING TO: " + requestST);
            Logger.d(TAG, requestST);
            URL postST = new URL(requestST);
            HttpURLConnection postConnection2 = (HttpURLConnection) postST.openConnection();
            postConnection2.setInstanceFollowRedirects(true);
            postConnection2.setRequestProperty("Cookie", cookie);
            List<NameValuePair> postData2 = new ArrayList<NameValuePair>(6);
            postData2.add(new BasicNameValuePair("service", "https://mysail.oakland.edu/uPortal/Login"));
            postConnection2.setDoOutput(true);
            postConnection2.setChunkedStreamingMode(0);
            OutputStream os2 = new BufferedOutputStream(postConnection2.getOutputStream());
            BufferedWriter writer2 = new BufferedWriter(new OutputStreamWriter(os2, "UTF-8"));
            writer2.write(getQuery(postData2));
            writer2.flush();
            writer2.close();
            os2.close();
            postConnection2.connect();
            Logger.d(TAG, "" + postConnection2.getHeaderFields());
            BufferedReader in = new BufferedReader(new InputStreamReader(postConnection2.getInputStream()));
            String serviceTicket;
            serviceTicket = in.readLine();
            Logger.d(TAG, "ST = " + serviceTicket);
            Logger.d(TAG, "End sending POST");

            // Proxy Granting Ticket and Service ticket validated
            url = new URL("https://mysail.oakland.edu/uPortal/Login?ticket="+serviceTicket);
            Logger.d(TAG, "GET TO: " + url.toString());
            getConnection = (HttpURLConnection) url.openConnection();
            getConnection.setRequestProperty("Cookie", cookie);
            getConnection.connect();
            Logger.d(TAG, "" + getConnection.getHeaderFields());
            Logger.d(TAG, "End sending GET");

            App.setCookie(cookie);
            App.setTgt(tgt);

            callback.onSuccess(null);

        } catch (MalformedURLException e) {
            callback.onError(e, null);
        } catch (IOException e) {
            callback.onError(e, null);
        } finally {
            if (postConnection != null)
                postConnection.disconnect();
        }
    }

    // http://stackoverflow.com/a/13486223/2546659
    private String getQuery(List<NameValuePair> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (NameValuePair pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getName(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }

}
