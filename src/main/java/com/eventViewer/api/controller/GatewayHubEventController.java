package com.eventViewer.api.controller;

import java.io.IOException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost", maxAge = 3600)
@RestController
@RequestMapping("/api/gatewayHubEvents")
public class GatewayHubEventController {
	
	 //Not working; needs SSL certificate to be configured 
	 @RequestMapping(value = "",
	    		produces = { MediaType.APPLICATION_JSON_VALUE },
	    		method = RequestMethod.POST)
	    public Void getAllEvents(@RequestBody String body) {
	 
		 	CloseableHttpClient client = HttpClients
		 			.custom()
		 		    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
		 		    .build();
		 	
		    HttpPost httpPost = new HttpPost("https://35.174.152.56:8080/v0/event/ac2/query");

		    StringEntity entity;
			try {
				entity = new StringEntity(body.toString());
				httpPost.setEntity(entity);
				httpPost.setHeader("Accept", "application/json");
				httpPost.setHeader("Content-type", "application/json");
				
			    CloseableHttpResponse response = client.execute(httpPost);
			    System.out.println(response);
			    client.close();
			
			} catch (IOException e) {
				e.printStackTrace();
			}
		   
		return null;

//	    	if(gatewayHubEvents.isEmpty())
//	    		return new ResponseEntity(HttpStatus.NO_CONTENT);
//	        return new ResponseEntity<List<GatewayHubEvent>>(response, HttpStatus.OK);
	    }
	 
}
