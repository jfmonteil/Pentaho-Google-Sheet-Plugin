/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.pentaho.di.trans.steps.pentahogooglesheets;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;


import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.api.client.util.Base64;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.security.KeyStore;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

/**
 * Describe your step plugin.
 * 
 */
public class PentahoGoogleSheetsPluginCredentials {	
    
	// Generate a service account and P12 key:
	// https://developers.google.com/identity/protocols/OAuth2ServiceAccount
	//private final String CLIENT_ID = "<your service account email address>";
	// Add requested scopes.
	// The name of the p12 file you created when obtaining the service account
	//private final String P12FILE = "/<your p12 file name>.p12";

	
	public static Credential getCredentialsJson(String scope) throws IOException {
            
			Credential credential=null;
	        InputStream in = PentahoGoogleSheetsPluginCredentials.class.getResourceAsStream("/plugins/pentaho-googledrive-vfs/credentials/client_secret.json");//pentaho-sheets-261911-18ce0057e3d3.json
            if (in == null) {
			//logError("Resource not found");
               throw new FileNotFoundException("Resource not found: /plugins/pentaho-googledrive-vfs/credentials/client_secret.json");			   
            }
			credential = GoogleCredential.fromStream(in).createScoped(Collections.singleton(scope));
            return credential;
	}
	
		/* OLD SCHOOL
	public static Sheets getSheetService(NetHttpTransport HTTP_TRANSPORT,JsonFactory JSON_FACTORY,String CLIENT_ID,String CREDENTIALS_FILE_PATH,String TOKENS_DIRECTORY_PATH,String HTTP_TRANSPORT) throws IOException 
	{     	
		 
	
    
		
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        if (clientSecrets == null) {
            //logError("Resource not found");
			throw new FileNotFoundException("clientSecrets Null ");
        }
		FileDataStoreFactory fdf = new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH));
		if (fdf == null) {
            //logError("data store Null");
			throw new FileNotFoundException("FileDataStore Factory Null ");
        }
		
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(fdf)
				.setAccessType("offline")
                .build();
		
		
		if (flow == null) {
            throw new FileNotFoundException("Could not create Authorization");
        }
       LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize(CLIENT_ID);
		
    }*/

	
	public static String base64EncodePrivateKeyStore(KeyStore pks) throws GeneralSecurityException, IOException {
        if (pks != null && pks.containsAlias("privatekey")) {
            ByteArrayOutputStream privateKeyStream = new ByteArrayOutputStream();
            //pks.store(privateKeyStream, GoogleSpreadsheet.SECRET);
            return Base64.encodeBase64String(privateKeyStream.toByteArray());
        }
        return "";
    }

    public static KeyStore base64DecodePrivateKeyStore(String pks) throws GeneralSecurityException, IOException {
        if (pks != null && !pks.equals("")) {
            ByteArrayInputStream privateKeyStream = new ByteArrayInputStream(Base64.decodeBase64(pks));
            if (privateKeyStream.available() > 0) {
                KeyStore privateKeyStore = KeyStore.getInstance("PKCS12");
                //privateKeyStore.load(privateKeyStream, GoogleSpreadsheet.SECRET);
                if (privateKeyStore.containsAlias("privatekey")) {
                    return privateKeyStore;
                }
            }
        }
        return null;
    }
}
