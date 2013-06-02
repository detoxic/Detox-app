package com.hackforchange.android.detox.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonObjectRequest;

public class JSONRequestWithParams extends JsonObjectRequest {

	private Map<String, String> params = new HashMap<String, String>();

	public JSONRequestWithParams(int method, String url, JSONObject jsonRequest,
			Listener<JSONObject> listener, ErrorListener errorListener) {
		super(method, url, jsonRequest, listener, errorListener);
	}

	@Override
	protected Map<String, String> getParams() throws AuthFailureError {
		return params;
	}

	public void setParameter(String name, String value) {
		params.put(name, value);
	}

	@Override
	public String getUrl() {
		String url = super.getUrl();
		if (params.isEmpty()) {
			return url;
		}
		
		StringBuilder encodedUrl = new StringBuilder(url);
		String paramsEncoding = getParamsEncoding();
        try {
        	encodedUrl.append('?');
            for (Map.Entry<String, String> entry : params.entrySet()) {
                encodedUrl.append(URLEncoder.encode(entry.getKey(), paramsEncoding));
                encodedUrl.append('=');
                encodedUrl.append(URLEncoder.encode(entry.getValue(), paramsEncoding));
                encodedUrl.append('&');
            }
            url = encodedUrl.toString().replace("+", "%20");
            Log.d("JSONRequestWithParams", url);
            return url;
        } catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Encoding not supported: " + paramsEncoding, uee);
        }
	}
	
	

}
