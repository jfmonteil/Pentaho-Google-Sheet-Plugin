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

import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.exception.KettleFileException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;
import org.pentaho.di.core.Const;



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
	        //InputStream in = PentahoGoogleSheetsPluginCredentials.class.getResourceAsStream("/plugins/pentaho-googledrive-vfs/credentials/client_secret.json");//pentaho-sheets-261911-18ce0057e3d3.json
            //logBasic("Getting credential json file from :"+Const.getKettleDirectory());
            InputStream in=null;
			try{
		       in = KettleVFS.getInputStream( Const.getKettleDirectory() + "/client_secret.json");
			}  catch (Exception e) {
			//throw new KettleFileException("Exception",e.getMessage(),e);
		    }
			
			if (in == null) {
               throw new FileNotFoundException("Resource not found:"+ Const.getKettleDirectory() + "/client_secret.json");			   
            }
			credential = GoogleCredential.fromStream(in).createScoped(Collections.singleton(scope));
            return credential;
	}
	

	
	
}
