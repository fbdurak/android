package com.example.googlemap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;

import android.net.ParseException;
import android.util.Log;

public class Task {

	String ID = null;
	private String Source_Latitude=null;
	private String Source_Longitude=null;
	private String Dest_Latitude=null;
	private String Dest_Longitude=null;	

	public Task(String ID){
		this.ID = ID;
		URL url = null;

		try {
			url = new URL("http://hpc.iriscouch.com/iodb/" +ID);


		} catch (MalformedURLException e) {

			e.printStackTrace();
		}
		HttpURLConnection urlConnection = null;
		try {
			urlConnection = (HttpURLConnection) url.openConnection();
		} catch (IOException e) {

			e.printStackTrace();
		}
		//   InputStream in = new BufferedInputStream(urlConnection.getInputStream());
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
		try {
			in.close();
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
				//  System.out.println(entry.getKey() + "=>" + entry.getValue());

				if (entry.getKey().equals("Source_Latitude") ){
					this.Source_Latitude = entry.getValue().toString();
				}
				else if(entry.getKey().equals("Source_Longitude")){
					this.Source_Longitude= entry.getValue().toString();
				}
				else if(entry.getKey().equals("Dest_Latitude")){
					this.Dest_Latitude = entry.getValue().toString();
				}
				else if(entry.getKey().equals("Dest_Longitude")){
					this.Dest_Longitude = entry.getValue().toString();
				}
			}


			//Log.d("JSonValues", JSONValue.toString(json)   
		}
		catch(ParseException pe){
			System.out.println(pe);
		}


	}


	public void setSource_Latitude(String Source_Latitude){
		this.Source_Latitude = Source_Latitude;
	}

	public String getSource_Latitude(){
		return this.Source_Latitude;
	}

	public void setSource_Longitude(String Source_Longitude){
		this.Source_Longitude = Source_Longitude;
	}

	public String getSource_Longitude(){
		return this.Source_Longitude;
	}

	public void setDest_Latitude(String Dest_Latitude){
		this.Dest_Latitude = Dest_Latitude;
	}

	public String getDest_Latitude(){
		return this.Dest_Latitude;
	}

	public void setDest_Longitude(String Dest_Longitude){
		this.Dest_Longitude = Dest_Longitude;
	}

	public String getDest_Longitude(){
		return this.Dest_Longitude;
	}

}
