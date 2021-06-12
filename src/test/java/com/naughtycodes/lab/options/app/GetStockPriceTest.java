package com.naughtycodes.lab.options.app;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutionException;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class GetStockPriceTest {
	
	public static void main(String args[]) throws InterruptedException, ExecutionException, IOException {
		
		String htmlOut= "";
		
        // create a client
        var client = HttpClient.newHttpClient();

        // create a request
        var request = HttpRequest.newBuilder(
            URI.create("https://www1.nseindia.com/live_market/dynaContent/live_watch/get_quote/GetQuote.jsp?symbol=ITC"))
            .header("accept", "text/html,application/xhtml+xml")
            .build();

        // use the client to send the request
        var responseFuture = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
      
        // This blocks until the request is complete
        var response = responseFuture.get();

        // the response:
        htmlOut = response.body();
        
        //System.out.println(htmlOut);
        
      Document doc = Jsoup.parse(htmlOut);
		Element res = doc.getElementById("responseDiv");

		JSONObject jsonOut = new JSONObject(res.text());
		System.out.println(
					jsonOut.getJSONArray("data").getJSONObject(0).get("lastPrice")
				);
		
        
		FileWriter fw = new FileWriter("test.html", false);			
		fw.write(htmlOut);
		fw.close();

}


}
