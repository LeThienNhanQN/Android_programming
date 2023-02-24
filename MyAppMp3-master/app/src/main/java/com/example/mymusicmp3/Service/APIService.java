package com.example.mymusicmp3.Service;

public class APIService {
    private static String base_url = "https://adminsun1.000webhostapp.com/server/";
    public static Dataservice getService(){
        return APIRetrofitClient.getClient(base_url).create(Dataservice.class);
    }
}
