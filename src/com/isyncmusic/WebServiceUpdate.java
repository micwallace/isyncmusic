package com.isyncmusic;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.content.SharedPreferences;
import android.util.Log;

public class WebServiceUpdate {
	private SharedPreferences prefs;
	public WebServiceUpdate(SharedPreferences _prefs){
		// set prefs variable
		prefs = _prefs;
	}
	// custom https trust manager to allow self signed ssl certificate
	X509TrustManager[] trustAllCerts = new X509TrustManager[] { 
		    new X509TrustManager() {     
		        public java.security.cert.X509Certificate[] getAcceptedIssuers() { 
		            return null;
		        }
		        public void checkClientTrusted( 
		            java.security.cert.X509Certificate[] certs, String authType) {
		            } 
		        public void checkServerTrusted( 
		            java.security.cert.X509Certificate[] certs, String authType) {
		        }
		    } 
		};
	public String run(){
		// connect to webservice and get ip addresses
		String query;
		try {
			String hashedpass = "";
			// hash the password; will eventually be done when user submits it
			try {
				MessageDigest digest = MessageDigest.getInstance("MD5");
				byte[] hashedbytes = digest.digest(prefs.getString("password", "").getBytes("UTF-8"));
				hashedpass = new BigInteger(1, hashedbytes).toString(16);
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "Password hashing failed!";
			}
			query = "email="+URLEncoder.encode(prefs.getString("username", ""),"UTF-8")+"&";
			query += "pass="+URLEncoder.encode(hashedpass,"UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return "Url encoding failed!";
		}
		// connect
		try {
			// set the custom trust store
			SSLContext sc = SSLContext.getInstance("SSL"); 
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			// set hostname verification
			HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier(){
				public boolean verify(String hostname, SSLSession session) {
					return (hostname.equals("wallaceitlogistics.com")?true:false);
				}
                });
		    // set url
			URL myurl = new URL("https://wallaceitlogistics.com/isyncmusic/getip.php");
			HttpsURLConnection con = (HttpsURLConnection) myurl.openConnection();
			// set request headers
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-length", String.valueOf(query.length())); 
			con.setRequestProperty("Content-Type","application/x-www-form-urlencoded"); 
			//con.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0;Windows98;DigExt)"); 
			con.setDoOutput(true); 
			con.setDoInput(true);
			// send post vars
			DataOutputStream output = new DataOutputStream(con.getOutputStream());  
			output.writeBytes(query);
			output.close();
			// read response
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String response = "";
			String currentline;
			while ((currentline = in.readLine()) != null){
				response+=currentline;
			}
			in.close();
			Log.i("isyncmusic", "server response: "+response);
			// Check response, if response does not contain "extip" return false; return server error message TBC
			if (!response.toString().contains("extip")){
				return response;
			}
			// parse JSON object
			JSONParser parser = new JSONParser();
            JSONObject newips;
			try {
				newips = (JSONObject) parser.parse(response);
				String newintip = newips.get("intip").toString();
				String newextip = newips.get("extip").toString();
				// set new ip addresses in preferences
				prefs.edit().putString("internalip", newintip).putString("externalip", newextip).commit(); 
				
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "JSON parse failed";
			}
            
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "IO Error";
		} catch (KeyManagementException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return "HTTPS key error";
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return "No algorithm exception";
		}
		return "1";
	}
}
