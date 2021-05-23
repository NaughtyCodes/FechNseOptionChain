package com.naughtycodes.lab.options.app;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

public class JavaHttpClientTest {

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        //synchronousRequest();
        asynchronousRequest();
    }

    private static void asynchronousRequest() throws InterruptedException, ExecutionException {

        // create a client
        var client = HttpClient.newHttpClient();

        // create a request
        var request = HttpRequest.newBuilder(
            URI.create("https://www1.nseindia.com/marketinfo/companyTracker/mtOptionKeys.jsp?companySymbol=HEROMOTOCO&indexSymbol=NIFTY&series=EQ&instrument=OPTSTK&date=24JUN2021"))
            .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
            .build();

        // use the client to send the request
        var responseFuture = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        //var responseFuture = client.sendAsync(request, new JsonBodyHandler<>(String.class));

        // We can do other things here while the request is in-flight

        // This blocks until the request is complete
        var response = responseFuture.get();

        // the response:
        System.out.println(response.body());
    }

    private static void synchronousRequest() throws IOException, InterruptedException {
        // create a client
        var client = HttpClient.newHttpClient();

        // create a request
        var request = HttpRequest.newBuilder(
            URI.create("https://api.nasa.gov/planetary/apod?api_key=DEMO_KEY")
        ).build();

        // use the client to send the request
        HttpResponse<Supplier<APOD>> response = client.send(request, new JsonBodyHandler<>(APOD.class));

        // the response:
        System.out.println(response.body().get().title);
    }

}