package com.example.guessr;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetRequest {

    private String baseUrl;
    private String roomUrl;


    private GetRequest(Builder builder) {
        this.baseUrl = builder.baseUrl;
        this.roomUrl = builder.roomUrl;
    }

    public JSONObject jsonObjectRequest(String endpoint) throws IOException, JSONException {
        StringBuffer response = baseRequest(this.roomUrl.concat(endpoint));
        JSONObject jsonObject = new JSONObject(String.valueOf(response));
        return jsonObject;
    }

    public JSONArray jsonArrayRequest(String endpoint) throws IOException, JSONException {
        StringBuffer response = baseRequest(this.roomUrl.concat(endpoint));
        JSONArray newobj = new JSONArray(String.valueOf(response));
        return newobj;
    }

    public void simpleRequest(String endpoint) throws IOException {
        StringBuffer response = baseRequest(this.roomUrl.concat(endpoint));
    }

    private StringBuffer baseRequest(String url) throws IOException {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setUseCaches(false);
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return response;
    }

    public static class Builder {
        private String baseUrl = "";
        private String roomUrl = "";

        public Builder baseUrl(String baseUrl) {
            if (!baseUrl.endsWith("/")) {
                this.baseUrl = baseUrl.concat("/");
            } else {
                this.baseUrl = baseUrl;
            }
            this.roomUrl = this.baseUrl;
            return this;
        }

        public Builder roomName(String roomName) {
            this.roomUrl = this.baseUrl.concat(roomName.replace(" ", "%20")).concat("/");
            return this;
        }

        public GetRequest build() {
            return new GetRequest(this);
        }
    }
}
