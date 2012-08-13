package com.example.googlemap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;

import android.net.ParseException;
import android.util.Log;

public class Result {

	private String ID; //worker ID
	private String db; 
	private String rev;
	//String jobID; //task ID
	public ArrayList<String> resultlist; //the results with ArrayList
	private JSONObject jsonresult; //the result with Json format

	public Result(String ID, String db/*, String jobID*/) {
		this.ID = ID;
		this.db = db;
		//this.jobID =jobID;
		this.resultlist = new ArrayList<String>(); 
		this.jsonresult =new JSONObject();
		this.rev = getRev(this.ID,this.db);
	}

	private String getRev(String id, String db){
		String rev=null;
		URL url = null;

		try {
			url = new URL("http://hpc.iriscouch.com/"+db+"/" +id);


		} catch (MalformedURLException e) {

			e.printStackTrace();
		}
		HttpURLConnection urlConnection = null;
		try {
			urlConnection = (HttpURLConnection) url.openConnection();
		} catch (IOException e) {

			e.printStackTrace();
		}
		//  InputStream in = new BufferedInputStream(urlConnection.getInputStream());
		BufferedReader in = null;
		try {
			in = new BufferedReader(
					new InputStreamReader(
							urlConnection.getInputStream()));
		} catch (IOException e) {

			e.printStackTrace();
		}
		String inputLine;
		String xml="";
		try {
			while ((inputLine = in.readLine()) != null){ 
				xml = xml+inputLine;
				//  System.out.println(inputLine);
			}
		} catch (IOException e) {

			e.printStackTrace();
		}


		Log.d("Json", xml);	


		JSONParser parser=new JSONParser();

		ContainerFactory containerFactory = new ContainerFactory(){
			public List creatArrayContainer() {
				return new LinkedList();
			}

			public Map createObjectContainer() {
				return new LinkedHashMap();
			}

		};

		try{
			Map json = null;
			try {
				json = (Map)parser.parse(xml, containerFactory);
			} catch (org.json.simple.parser.ParseException e) {

				e.printStackTrace();
			}
			Iterator iter = json.entrySet().iterator();

			while(iter.hasNext()){
				Map.Entry entry = (Map.Entry)iter.next();

				Log.d("brokerdb keys",entry.getKey().toString());
				if (entry.getKey().equals("_rev") ){
					rev = entry.getValue().toString();
				}

			}
			//Log.d("JSonValues", JSONValue.toString(json)   
		}
		catch(ParseException pe){
			System.out.println(pe);
		}


		return rev;
	}




	public void insertIntoDb(){
		try {
			jsonresult.accumulate("path", resultlist);
			jsonresult.put("_id", ID);
			jsonresult.put("_rev", this.rev);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		Log.d("JSonFormat", jsonresult.toString());

		HttpURLConnection con2 = null;
		try {
			con2 = (HttpURLConnection) new URL("http://hpc.iriscouch.com/" +this.db).openConnection();
		} catch (MalformedURLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		con2.setDoOutput(true);
		try {
			con2.setRequestMethod("POST");
		} catch (ProtocolException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		con2.setRequestProperty("Content-Type", "application/json");
		try {
			con2.connect();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			OutputStream os = con2.getOutputStream();
			os.write(this.jsonresult.toString().getBytes());
			os.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			System.out.println(con2.getInputStream().toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//con2.disconnect();


	}



}
