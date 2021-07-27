package com.naughtycodes.lab.options.app;

import java.io.*;
import java.util.*;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.errors.TransportException;
import org.springframework.beans.factory.annotation.Autowired;

import com.naughtycodes.lab.options.app.config.GitConfig;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;

public class JsonFileLoader {
	
	public static void main(String args[]) throws InvalidRemoteException, TransportException, IOException, GitAPIException {
		String fn = "F:\\lab\\eclipse-workspace\\NseOptionsChainData\\SEP_UpdatedData.json";
	     JSONParser parser = new JSONParser();
	      try {
	         Object obj = parser.parse(new FileReader(fn));
	         JSONObject jsonObject = (JSONObject)obj;
	         for(String key: jsonObject.keySet()) {
	        	 System.out.println(key);
	         }
	        
	      } catch(Exception e) {
	         e.printStackTrace();
	      }

	}

}
