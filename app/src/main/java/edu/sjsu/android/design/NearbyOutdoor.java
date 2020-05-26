package edu.sjsu.android.design;


import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.PlaceType;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.RankBy;
import com.google.maps.model.LatLng;
import java.io.IOException;
import android.util.Log;
import android.content.Context;
import android.app.Activity;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;

public class NearbyOutdoor{

    FindActivity fa = new FindActivity();
    private static final String TAG = "TAG";
    public PlacesSearchResponse run(){

        PlacesSearchResponse request = new PlacesSearchResponse();
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey("AIzaSyBMETukHUGOqVWgXQ3Y58L3sHpPH1OoE04")
                .build();

        LatLng myLocation = new LatLng(fa.getLat(), fa.getLong());
        Log.d("TAG", "heres the lat and long outdoor " + fa.getLat()+ " "+ fa.getLong());
        try {
            request = PlacesApi.nearbySearchQuery(context, myLocation)
                    .radius(10000)
                    .rankby(RankBy.PROMINENCE)
                    .location(myLocation)
                    .keyword("hike")
                    .type(PlaceType.PARK)
                    .await();
        } catch (ApiException | IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            return request;
        }
    }
}

