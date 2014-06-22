package com.sinlimites.androidcameraapp;

import java.io.IOException;
import java.net.SocketException;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

public class JSONUpdateAsync extends AsyncTask<String, Void, String> {

	private String JsonUrl = "http://145.24.222.137:8080/Rebuild/rest/location/update/";
	private String json;
	private Context context;
	private String succeeded = "";
	private Activity activity;

	public JSONUpdateAsync(String code, String json) {
		this.JsonUrl = this.JsonUrl+code;
		this.json = json;
		this.activity = MyApplication.getActivity();
		this.context = MyApplication.getActivity();
	}

	/*
	 * Connects to the server and updates with an JSON string.
	 * 
	 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
	 */
	@Override
	protected String doInBackground(String... params) {
		HttpClient client = new DefaultHttpClient();
		HttpPost httpUpdate = new HttpPost(JsonUrl);
		try {
			httpUpdate.setEntity(new StringEntity(json));
			httpUpdate.setHeader("Content-type", "application/json");
			HttpResponse response = client.execute(httpUpdate);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				succeeded = activity.getResources().getString(R.string.http_updated_success);
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
		return "";
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
		Toast.makeText(context, succeeded, Toast.LENGTH_SHORT).show();
	}
}
