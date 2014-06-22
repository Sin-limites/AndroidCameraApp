package com.sinlimites.androidcameraapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.sinlimites.objects.Handling;

public class JSONGetAsync extends AsyncTask<String, Void, String> {

	private String JsonUrl = "http://145.24.222.137:8080/Rebuild/rest/handling/get/";
	private Context context;
	private ArrayList<Handling> handlingList = new ArrayList<Handling>();
	private String succeeded = "";
	private Activity activity;

	public JSONGetAsync(){
		this.activity = MyApplication.getActivity();
		this.context = MyApplication.getActivity();
	}
	/*
	 * Connects to the server and gets an JSON string as return type.
	 * 
	 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
	 */
	@Override
	protected String doInBackground(String... params) {

		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(JsonUrl);
		try {
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				succeeded = activity.getResources().getString(R.string.http_get_success);
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(content));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
			} else if (statusCode == 500){
				succeeded = activity.getResources().getString(R.string.http_500);
			} else if (statusCode == 409){
				succeeded = activity.getResources().getString(R.string.http_409);
			} else {
				succeeded = activity.getResources().getString(R.string.http_all);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			succeeded = activity.getResources().getString(R.string.http_failed_connect);
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return builder.toString();
	}

	/*
	 * A simple message to show the user, that its connecting to the server.
	 * 
	 * @see android.os.AsyncTask#onPreExecute()
	 */
	protected void onPreExecute() {
		Toast.makeText(context, R.string.connecting_to_server, Toast.LENGTH_SHORT).show();
	}

	/*
	 * Checks if the JSON is an JSONArray or JSONObject.
	 * 
	 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
	 */
	protected void onPostExecute(String JsonString) {
		if (!JsonString.equals("")){
			try {
				Object JSON = new JSONTokener(JsonString).nextValue();

				if (JSON instanceof JSONArray) {
					JSONArray JsonArray = (JSONArray) JSON;
					for (int i = 0; i < JsonArray.length(); i++) {
						JSONObject JsonObject = JsonArray.getJSONObject(i);
						IterateJsonObject(JsonObject);
					}
				} else if (JSON instanceof JSONObject) {
					JSONObject JsonObject = (JSONObject) JSON;
					IterateJsonObject(JsonObject);
				}
				MainService.setHandlingArrayList(handlingList);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else 
			Toast.makeText(context, succeeded, Toast.LENGTH_SHORT).show();
	}

	/*
	 * Iterates through the JSON and sets all the TextViews with the data.
	 */
	@SuppressLint("NewApi")
	private void IterateJsonObject(JSONObject JsonObject) {
		try {
			JsonObject = JsonObject.getJSONObject("handlingID");
			Handling handling = new Handling();
			handling.setHandlingName(JsonObject.getString("handlingName"));
			handling.setHandlingID(JsonObject.getInt("handlingID"));
			handlingList.add(handling);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
