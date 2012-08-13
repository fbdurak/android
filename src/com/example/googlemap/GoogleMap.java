package com.example.googlemap;

//import java.util.ArrayList;
import java.net.*;
import org.xml.sax.InputSource;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.ContainerFactory;
import org.json.simple.parser.JSONParser;
import org.w3c.dom.*;
import javax.xml.xpath.*;

import java.io.*;
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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.example.googlemap.DemoOverlay;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;
//import com.google.android.maps.MapView.LayoutParams;
import android.widget.ToggleButton;

import android.net.ParseException;
import android.os.Bundle;
import com.google.android.maps.MapView.LayoutParams;
import android.view.View;
import android.widget.LinearLayout;

public class GoogleMap extends MapActivity {

	private MapView mapView = null;
	private MapController mc = null;
	private View zoomView   = null;
	private LinearLayout zoomLayout = null;
	private List<Overlay> overlays = null;
	private MapOverlay myOverlay =null;

	private GeoPoint geopoint = null;
	private int sequence = 37;
	private String myID = "1";
	private int jobflag = 0;
	private int markflag= 0;
	private String intermediate_Lat=null;
	private String intermediate_Long=null;
	private Task T=null;
	private Result rs=null;





	// This block, MapOverlay, gets the GeoPoints of touched screen pixel!
	class MapOverlay extends com.google.android.maps.Overlay {

		@Override
		public boolean onTouchEvent(MotionEvent event, MapView mapView) {

			// when worker lifts his finger
			if (event.getAction() ==1) {
				//put a flag to run it when Mark Toggle is on
				if(markflag==1){
					GeoPoint p = mapView.getProjection().fromPixels((int) event.getX(), (int) event.getY());
					intermediate_Lat = Double.toString(p.getLatitudeE6() / 1E6) ;
					intermediate_Long = Double.toString(p.getLongitudeE6() / 1E6);

					String pairs[] = getDirectionData(T.getSource_Latitude(), T.getSource_Longitude() ,intermediate_Lat, intermediate_Long);
					if(pairs != null){
						Log.d("points from DB", pairs[0]);

						//push all the midpoint to DB
						for(String s : pairs){
							Log.d("arraylist", s);
							rs.resultlist.add(s);
						}

						//We have the nodes for the work result, and put them into JSon Object
						String[] lngLat = pairs[0].split(",");
						Log.d("lngLat", lngLat[1]);

						GeoPoint startGP = new GeoPoint((int) (Double.parseDouble(lngLat[0]) * 1E6), (int) (Double.parseDouble(lngLat[1]) * 1E6));
						geopoint = startGP;

						GeoPoint gp1;
						GeoPoint gp2 = startGP;

						for (int i = 1; i < pairs.length; i++) {
							lngLat = pairs[i].split(",");
							gp1 = gp2;
							gp2 = new GeoPoint((int) (Double.parseDouble(lngLat[0]) * 1E6),(int) (Double.parseDouble(lngLat[1]) * 1E6));
							mapView.getOverlays().add(new DemoOverlay(gp1, gp2));
							Log.d("xxx", "pair:" + pairs[i]);
						}

						// END POINT
						mapView.getOverlays().add(new DemoOverlay(gp2, gp2));
						mapView.getController().animateTo(startGP);


						T.setSource_Latitude(intermediate_Lat);
						T.setSource_Longitude(intermediate_Long);
					}


					Toast.makeText(
							getBaseContext(),
							p.getLatitudeE6() / 1E6 + "," + p.getLongitudeE6()
							/ 1E6, Toast.LENGTH_SHORT).show(); 
				}
			}
			return false;

		}
	}
	public void onToggleJobSendClicked(View view) {
		// Is the toggle on?
		boolean on = ((ToggleButton) view).isChecked();


		if (on) {
			// Enable vibrate
			//Log.d("ToggleJobSend", "on");

			while(jobflag==0){
				HttpURLConnection con = null;
				//starts the connection to DB with longpolling
				try {
					con = (HttpURLConnection) new URL("http://hpc.iriscouch.com/iodb/_changes?feed=longpoll&since="+sequence+"&heartbeat=600000").openConnection();
				} catch (MalformedURLException e1) {

					e1.printStackTrace();
				} catch (IOException e1) {

					e1.printStackTrace();
				}
				con.setDoOutput(true);
				con.setUseCaches(false);
				con.setRequestProperty("Connection", "Keep-Alive"); 
				try {
					con.setRequestMethod("GET");
				} catch (ProtocolException e2) {

					e2.printStackTrace();
				}
				try {
					con.connect();
				} catch (IOException e2) {

					e2.printStackTrace();
				}
				//Whenever the DB gets updated, it starts the read from DB with buffers. First get InputStream
				BufferedReader in = null;
				try {
					in = new BufferedReader(  new InputStreamReader(con.getInputStream()));
				} catch (IOException e1) {

					e1.printStackTrace();
				}
				//Read the data on jsonText1, it is JSon format
				String inputLine;
				String jsonText1="";
				try {
					while ((inputLine = in.readLine()) != null){ 
						jsonText1 = jsonText1+inputLine;

					}
				} catch (IOException e1) {

					e1.printStackTrace();
				}
				try {
					in.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				Log.d("Update from DB", jsonText1.trim());

				//Creates a parser to parse the data from JSon format
				JSONParser parser=new JSONParser();
				ContainerFactory containerFactory = new ContainerFactory(){
					public List creatArrayContainer() {
						return new LinkedList();
					}

					public Map createObjectContainer() {
						return new LinkedHashMap();
					}

				};
				//Maps the each key to values in each line iteratively          
				try{
					Map json = (Map)parser.parse(jsonText1, containerFactory);
					Iterator iter = json.entrySet().iterator();
					while(iter.hasNext()){
						Map.Entry entry = (Map.Entry)iter.next();

						// Check if myID(worker) got the job. If so, Set jobflag to 1.		    
						if(entry.getValue().toString().contains("id="+myID)){
							Log.d("changing flag", entry.getValue().toString());
							jobflag = 1;
							con.disconnect();
							break;

						}
						Log.d(entry.getKey().toString(), entry.getValue().toString());

					}
				}catch (org.json.simple.parser.ParseException e) {
					e.printStackTrace();
				}
				catch(ParseException pe){
					System.out.println(pe);
				}

				sequence++;
				//clear the overlay before getting the new job
				mapView.getOverlays().clear();
				mapView.invalidate();
				myOverlay = new MapOverlay();

				overlays = mapView.getOverlays();
				overlays.add(myOverlay);

				if(jobflag==1){
					T = new Task(myID);
					rs = new Result(myID,"brokerdb");


					//setting the start and end points with marker
					GeoPoint startGP = new GeoPoint((int) (Double.parseDouble(T.getSource_Latitude()) * 1E6), (int) (Double.parseDouble(T.getSource_Longitude()) * 1E6));
					GeoPoint endGP = new GeoPoint((int) (Double.parseDouble(T.getDest_Latitude()) * 1E6), (int) (Double.parseDouble(T.getDest_Longitude()) * 1E6));
					mc.setCenter(startGP);
					mc.setZoom(10);
					mapView.getOverlays().add(new DemoOverlay(startGP, startGP));
					mapView.getOverlays().add(new DemoOverlay(endGP, endGP));


				}//end of if jobflag=1

			}// end of while loop

		} else {
			// Disable vibrate
			Log.d("ToggleJobSend", "off");

			if (intermediate_Lat==null || intermediate_Long==null){
				intermediate_Lat = T.getSource_Latitude();
				intermediate_Long = T.getSource_Longitude();
			}

			String pairs[] = getDirectionData(intermediate_Lat, intermediate_Long,
					T.getDest_Latitude(), T.getDest_Longitude());

			if (pairs != null){
				Log.d("points from DB", pairs[0]);
				//getting the results into StringArray
				for(String s : pairs){
					Log.d("arraylist", s);
					rs.resultlist.add(s);
				}

				rs.insertIntoDb();



				//We have the nodes for the work result, and put them into JSon Object
				String[] lngLat = pairs[0].split(",");
				Log.d("lngLat", lngLat[1]);
				GeoPoint startGP = new GeoPoint((int) (Double.parseDouble(lngLat[0]) * 1E6), (int) (Double.parseDouble(lngLat[1]) * 1E6));
				geopoint = startGP;
				

				GeoPoint gp1;
				GeoPoint gp2 = startGP;

				for (int i = 1; i < pairs.length; i++) {
					lngLat = pairs[i].split(",");
					gp1 = gp2;
					gp2 = new GeoPoint((int) (Double.parseDouble(lngLat[0]) * 1E6),(int) (Double.parseDouble(lngLat[1]) * 1E6));
					mapView.getOverlays().add(new DemoOverlay(gp1, gp2));
					Log.d("xxx", "pair:" + pairs[i]);
				}

				// END POINT
				mapView.getOverlays().add(new DemoOverlay(gp2, gp2));

				mapView.getController().animateTo(startGP);
			}
			jobflag = 0;
		}
	}

	public void onToggleMarkZoomClicked(View view) {
		// Is the toggle on?
		boolean on = ((ToggleButton) view).isChecked();

		if (on) {
			// Enable vibrate
			Log.d("Mark", "on");
			markflag=1 ;


		} else {
			// Disable vibrate
			Log.d("Zoom", "off");
			markflag=0;
		}
	}


	//Starts the main activity
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_google_map);

		//initilize the objects for Zoom

		//shows the google map with Zoom panel
		mapView = (MapView) findViewById(R.id.mapView);
		mapView.setBuiltInZoomControls(true);
		mc = mapView.getController();
		mapView.setSatellite(false);
		zoomLayout = (LinearLayout) findViewById(R.id.zoom);
		zoomView = mapView.getZoomControls();
		zoomLayout.addView(zoomView, new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		mapView.setBuiltInZoomControls(true);
		mapView.displayZoomControls(true);

	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}


	private String[] getDirectionData(String sourceLat, String sourceLong, String destinationLat, String destinationLong) {

		URL google = null;
		int statusflag=1;
		try {
			google = new URL("http://maps.googleapis.com/maps/api/directions/xml?origin="+sourceLat+","+sourceLong+"&destination="+destinationLat+","+destinationLong + "&sensor=false");
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		URLConnection yc = null;
		try {
			yc = google.openConnection();
		} catch (IOException e1) {

			e1.printStackTrace();
		}
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader( yc.getInputStream()));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		String inputLine;
		String xml="";
		try {
			while ((inputLine = in.readLine()) != null){ 
				xml = xml+inputLine;
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			in.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		Log.d("Results from URL->", xml);

		InputStream is = new ByteArrayInputStream(xml.getBytes());
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		String status = null;
		InputSource inputXml = new InputSource(new ByteArrayInputStream(xml.getBytes()));
		try {
			NodeList snl= (NodeList) xpath.evaluate("/DirectionsResponse/status", inputXml, XPathConstants.NODESET);
			status = snl.item(0).getTextContent();
			Log.d("status", status);
			if (!status.equals("OK")){
				statusflag= 0;

			}


		} catch (XPathExpressionException e) {

			e.printStackTrace();
		}
		String pathConent = "";

		if(statusflag==1){
			NodeList nl = null;
			InputSource inputXml1 = new InputSource(is);
			try {
				nl = (NodeList) xpath.evaluate("/DirectionsResponse/route/leg/step/start_location", inputXml1, XPathConstants.NODESET);

				for (int s = 0; s < nl.getLength(); s++) {

					String nodeString = nl.item(s).getTextContent();

					nodeString = nodeString.trim().replaceAll("( )+", ",");

					pathConent = pathConent+" "+nodeString;

					Log.d("values", nodeString);
				}
			} catch (XPathExpressionException e) {

				e.printStackTrace();
			}
			Log.d("pathcontent",pathConent);
			Log.d("pathcontent",pathConent.trim());
			String[] tempContent = pathConent.trim().split(" ");

			return tempContent;
		}else {
			return null;
		}

	}

}
