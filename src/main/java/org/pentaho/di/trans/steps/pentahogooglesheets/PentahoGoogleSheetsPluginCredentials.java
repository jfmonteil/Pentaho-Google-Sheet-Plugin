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

import com.google.api.client.auth.oauth2.Credential;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.services.sqladmin.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import com.google.api.client.http.*;
import java.io.InputStream;
import java.util.Collections;


/**
 * Describe your step plugin.
 * 
 */
public class PentahoGoogleSheetsPluginCredentials {


	public static HttpCredentialsAdapter getCredentialsJson(String scope,String jsonCredentialPath,String impersonation) throws IOException {

		GoogleCredentials credential=null;

            InputStream in=null;
			try{
		       in = KettleVFS.getInputStream(jsonCredentialPath);//Const.getKettleDirectory() + "/client_secret.json");
			}  catch (Exception e) {
		    }
			
			if (in == null) {
               throw new FileNotFoundException("Resource not found:"+ jsonCredentialPath);			   
            }
			if(impersonation.isEmpty()) {
				credential = GoogleCredentials.fromStream(in).createScoped(Collections.singleton(scope));
			}
			else credential = GoogleCredentials.fromStream(in)
					.createScoped(Collections.singleton(SQLAdminScopes.SQLSERVICE_ADMIN)).createDelegated(impersonation);

            return new HttpCredentialsAdapter(credential);
	}

	public static HttpRequestInitializer setHttpTimeout(final HttpRequestInitializer requestInitializer, final String timeout) {
		return new HttpRequestInitializer() {
			@Override
			public void initialize(HttpRequest httpRequest) throws IOException {
				Integer TO=Integer.parseInt(timeout);
				requestInitializer.initialize(httpRequest);
				httpRequest.setConnectTimeout(TO * 60000);  // 10 minutes connect timeout
				httpRequest.setReadTimeout(TO * 60000);  // 10 minutes read timeout
			}
		};
	}
	

	
	
}
