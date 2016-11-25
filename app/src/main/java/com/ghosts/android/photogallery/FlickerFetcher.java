package com.ghosts.android.photogallery;

import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.ghosts.android.photogallery.GalleryItem.PhotosBean.PhotoBean;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Allam on 12/03/2016.
 */
public class FlickerFetcher {
    private static final String TAG = "FlickrFetchr";
    private static final String API_KEY = "a2b0acdef1549bb459d9711463fdeee6";
    public static final String FETCH_RECENTS_METHOD = "flickr.photos.getRecent";
    public static final String SEARCH_METHOD = "flickr.photos.search";
    private static final Uri ENDPOINT = Uri
            .parse("https://api.flickr.com/services/rest/")
            .buildUpon()
            .appendQueryParameter("api_key", API_KEY)
            .appendQueryParameter("format", "json")
            .appendQueryParameter("nojsoncallback", "1")
            .appendQueryParameter("extras", "url_s")
            .build();

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() +
                        ": with " +
                        urlSpec);
            }
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public List<PhotoBean> fetchItems(int page, String method, String query) {
        GalleryItem items = new GalleryItem();

        try {
            String url = buildUrl(page,  method,  query);
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            items = parseItems(jsonString);

        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        }
        return items.getPhotos().getPhoto();

    }


    private GalleryItem parseItems(String jsonString) {
        Gson gson = new Gson();
        GalleryItem galleryItem = gson.fromJson(jsonString, GalleryItem.class);
        return galleryItem;
    }


    private String buildUrl(int page, String method, String query) {

        Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                .appendQueryParameter("method", method)
                .appendQueryParameter("page", page+"");
        if (method.equals(SEARCH_METHOD)) {
            uriBuilder.appendQueryParameter("text", query);
        }
        Log.i("AAA" ,uriBuilder.build().toString());
        return uriBuilder.build().toString();

    }


}
