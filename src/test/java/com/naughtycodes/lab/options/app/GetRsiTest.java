package com.naughtycodes.lab.options.app;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutionException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class GetRsiTest {
	
	public static void main(String args[]) throws InterruptedException, ExecutionException, IOException {
		
			String htmlOut= "";
			
	        // create a client
	        var client = HttpClient.newHttpClient();

	        // create a request
	        var request = HttpRequest.newBuilder(
	            URI.create("https://www.traderscockpit.com/?pageView=rsi-indicator-rsi-chart&type=rsi&symbol=ITC"))
	            .header("accept", "text/html,application/xhtml+xml")
	            .build();

	        // use the client to send the request
	        var responseFuture = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
	      
	        // This blocks until the request is complete
	        var response = responseFuture.get();

	        // the response:
	        htmlOut = response.body();
	        
	        System.out.println(htmlOut);
	        
	        Document doc = Jsoup.parse(htmlOut);
			Elements tables = doc.getElementsByTag("table");
			
			for(int tableNo = 0; tableNo <= tables.size(); tableNo++) {
				if(tableNo == 3) {
					
					for(int i=0; i<tables.get(tableNo).getElementsByTag("tr").size(); i++) {
						System.out.println(tables.get(tableNo).getElementsByTag("tr").get(i).text());	
					}
					
					for(Element tr : tables.get(tableNo).getElementsByTag("tr")) {
						for(Element td : tr.getElementsByTag("td")) {
							//System.out.println(td.text());							
						}
					}
				}
			}
	        
			FileWriter fw = new FileWriter("test.html", false);			
			fw.write(htmlOut);
			fw.close();

	}

}
