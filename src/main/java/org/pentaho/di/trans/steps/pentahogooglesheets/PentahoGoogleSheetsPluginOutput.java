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

import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.core.Const;


import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;


import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.ClearValuesResponse;
import com.google.api.services.sheets.v4.model.ClearValuesRequest;
import com.google.api.services.sheets.v4.model.UpdateSheetPropertiesRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.SheetProperties;




import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.Permission;


import java.io.IOException;
import java.security.GeneralSecurityException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;



import org.pentaho.di.trans.steps.pentahogooglesheets.PentahoGoogleSheetsPluginOutputMeta;
import org.pentaho.di.trans.steps.pentahogooglesheets.PentahoGoogleSheetsPluginOutputData;
import org.pentaho.di.trans.steps.pentahogooglesheets.PentahoGoogleSheetsPluginCredentials;

/**
 * Describe your step plugin.
 * 
 */

public class PentahoGoogleSheetsPluginOutput extends BaseStep implements StepInterface {

  private static Class<?> PKG = PentahoGoogleSheetsPluginInput.class; // for i18n purposes, needed by Translator2!!
  
  private PentahoGoogleSheetsPluginOutputMeta meta;
  private PentahoGoogleSheetsPluginOutputData data;
  
  public PentahoGoogleSheetsPluginOutput( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans ) {
    super( stepMeta, stepDataInterface, copyNr, transMeta, trans );
  }
  
  /**
     * Initialize and do work where other steps need to wait for...
     *
     * @param stepMetaInterface
     *          The metadata to work with
     * @param stepDataInterface
     *          The data to initialize
     */
   @Override
   public boolean init( StepMetaInterface smi, StepDataInterface sdi ) {
        
		meta = (PentahoGoogleSheetsPluginOutputMeta) smi;
        data = (PentahoGoogleSheetsPluginOutputData) sdi;
        JsonFactory JSON_FACTORY=null;
		NetHttpTransport HTTP_TRANSPORT = null;
		String APPLICATION_NAME = "pentaho-sheets";
		String TOKENS_DIRECTORY_PATH = Const.getKettleDirectory() +"/tokens";
        String scope=SheetsScopes.SPREADSHEETS_READONLY;
		Boolean exists=false;

     
	    try {
   	   	    JSON_FACTORY = JacksonFactory.getDefaultInstance();
			HTTP_TRANSPORT=GoogleNetHttpTransport.newTrustedTransport();			
		} catch (Exception e) {
			logError("Exception",e.getMessage(),e);
		}
		
        
		if (super.init(smi, sdi)) {
				
				//Check if file exists
				 try {
                    HTTP_TRANSPORT=GoogleNetHttpTransport.newTrustedTransport();
				    APPLICATION_NAME = "pentaho-sheets";
                    JSON_FACTORY = JacksonFactory.getDefaultInstance();
                    TOKENS_DIRECTORY_PATH = Const.getKettleDirectory() +"/tokens";   
					scope="https://www.googleapis.com/auth/drive";
					Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, PentahoGoogleSheetsPluginCredentials.getCredentialsJson(scope,environmentSubstitute(meta.getJsonCredentialPath()))).setApplicationName(APPLICATION_NAME).build();
                    String wsID=environmentSubstitute(meta.getSpreadsheetKey());
					//"properties has { key='id' and value='"+wsID+"'}";
					String q="mimeType='application/vnd.google-apps.spreadsheet'";
					FileList result = service.files().list().setQ(q).setPageSize(100).setFields("nextPageToken, files(id, name)").execute();
                    List<File> spreadsheets = result.getFiles();
	  					
					for (File spreadsheet:spreadsheets) {
                        //logBasic(wsID+" VS "+spreadsheet.getId());
						if(wsID.equals(spreadsheet.getId()))
						{
							exists=true; //file exists
							logBasic("Spreadsheet:"+ wsID +" exists");
						}
			                     
                    }			
					//If it does not exist & create checkbox is checker create it.		
					//logBasic("Create if Not exist is :"+meta.getCreate());
					if(!exists && meta.getCreate())
					{						
						if(!meta.getAppend()){ //si append + create alors erreur
						 //Init Service
					    scope="https://www.googleapis.com/auth/spreadsheets";
					    data.service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, PentahoGoogleSheetsPluginCredentials.getCredentialsJson(scope,environmentSubstitute(meta.getJsonCredentialPath()))).setApplicationName(APPLICATION_NAME).build();
						
						//If it does not exist create it.
						Spreadsheet spreadsheet = new Spreadsheet().setProperties(new SpreadsheetProperties().setTitle(wsID));
						Sheets.Spreadsheets.Create request = data.service.spreadsheets().create(spreadsheet);
						Spreadsheet response = request.execute();
						String spreadsheetID=response.getSpreadsheetId();
						meta.setSpreadsheetKey(spreadsheetID);//
						//logBasic(response);
						//If it does not exist we use the Worksheet ID to rename 'Sheet ID'
						if(environmentSubstitute(meta.getWorksheetId())!="Sheet1")
						{
    					
							SheetProperties title = new SheetProperties().setSheetId(0).setTitle(environmentSubstitute(meta.getWorksheetId()));
							// make a request with this properties
							UpdateSheetPropertiesRequest rename = new UpdateSheetPropertiesRequest().setProperties(title);
							// set fields you want to update
							rename.setFields("title");
							logBasic("Changing worksheet title to:"+ environmentSubstitute(meta.getWorksheetId()));				
							List<Request> requests = new ArrayList<>();
							Request request1 = new Request().setUpdateSheetProperties(rename);
							requests.add(request1);
							BatchUpdateSpreadsheetRequest requestBody = new BatchUpdateSpreadsheetRequest();
							requestBody.setRequests(requests);
		                     // now you can execute batchUpdate with your sheetsService and SHEET_ID
							data.service.spreadsheets().batchUpdate(spreadsheetID, requestBody).execute();
							//now if share email is not null we share with R/W with the email given
							if((environmentSubstitute(meta.getShareEmail())!=null && !environmentSubstitute(meta.getShareEmail()).isEmpty()) || (environmentSubstitute(meta.getShareDomain())!=null && !environmentSubstitute(meta.getShareDomain()).isEmpty()))
							{
								
								
								String fileId=spreadsheetID;
								JsonBatchCallback<Permission> callback = new JsonBatchCallback<Permission>() {
								  @Override
								  public void onFailure(GoogleJsonError e,
														HttpHeaders responseHeaders)
									  throws IOException {
									// Handle error
									logError("Failed sharing file" + e.getMessage());
								  }

								  @Override
								  public void onSuccess(Permission permission,
														HttpHeaders responseHeaders)
									  throws IOException {
									logBasic("Shared successfully : Permission ID: " + permission.getId());
								  }
								};
								BatchRequest batch = service.batch();
								if(environmentSubstitute(meta.getShareEmail())!=null && !environmentSubstitute(meta.getShareEmail()).isEmpty())
								{
								logBasic("Sharing sheet with:"+ environmentSubstitute(meta.getShareEmail()));
								Permission userPermission = new Permission()
									.setType("user")
									.setRole("writer")
									.setEmailAddress(environmentSubstitute(meta.getShareEmail()));
								//Using Google drive service here not spreadsheet data.service
								service.permissions().create(fileId, userPermission)
									.setFields("id")
									.queue(batch, callback);
								}
								if(environmentSubstitute(meta.getShareDomain())!=null && !environmentSubstitute(meta.getShareDomain()).isEmpty())
								{
									logBasic("Sharing sheet with domain:"+environmentSubstitute(meta.getShareDomain()));
									Permission domainPermission = new Permission()
									.setType("domain")
									.setRole("reader")
									.setDomain(environmentSubstitute(meta.getShareDomain()));
								service.permissions().create(fileId, domainPermission)
									.setFields("id")
									.queue(batch, callback);

								}
								batch.execute();

							}
						
						}
					  } else {
					    	logError("Append and Create options cannot be activated alltogether");
					    	return false;
					  }
					
					}
					
					if(!exists && !meta.getCreate())
					{
						logError("File does not Exist");
						return false;
					}
						
						
            } catch (Exception e) {
                logError("Error: for worksheet : "+environmentSubstitute( meta.getWorksheetId())+" in spreadsheet :"+environmentSubstitute( meta.getSpreadsheetKey()) + e.getMessage(), e);
                setErrors(1L);
                stopAll();
                return false;
            }

            return true;
        }
        return false;
    }
   

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    
	meta = (PentahoGoogleSheetsPluginOutputMeta) smi;
    data = (PentahoGoogleSheetsPluginOutputData) sdi;
	
	Object[] row = getRow();
    List<Object> r= new ArrayList<Object>();

	
    if (first && row!=null) {
		first = false;
		
		data.outputRowMeta=getInputRowMeta().clone();
		meta.getFields(data.outputRowMeta, getStepname(), null, null, this, repository, metaStore);	
		data.rows =  new ArrayList<List<Object>>();
		if(meta.getAppend()){ //If append is checked we do not write the header
		   logBasic("Appending lines so skipping the header");
		   data.currentRow++;	
		} else {
			logBasic("Writing header");
			r= new ArrayList<Object>();
			for (int i = 0; i < data.outputRowMeta.size(); i++) {
				ValueMetaInterface v = data.outputRowMeta.getValueMeta(i);
				r.add(v.getName());			
			}
			data.rows.add(r);
			data.currentRow++;	
		}
		
	} else {
		try {
				//if last row is reached
				if (row == null) { 
					        if(data.currentRow>0)
							{													
								ClearValuesRequest requestBody = new ClearValuesRequest();							
								String range=environmentSubstitute(meta.getWorksheetId());

								logBasic("Clearing range" +range +" in Spreadsheet :"+ environmentSubstitute(meta.getSpreadsheetKey()));
								//Creating service
								NetHttpTransport HTTP_TRANSPORT=GoogleNetHttpTransport.newTrustedTransport();
								JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
								String APPLICATION_NAME = "pentaho-sheets";
								String TOKENS_DIRECTORY_PATH = Const.getKettleDirectory() +"/tokens";
								String scope=SheetsScopes.SPREADSHEETS;
								data.service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, PentahoGoogleSheetsPluginCredentials.getCredentialsJson(scope,environmentSubstitute(meta.getJsonCredentialPath()))).setApplicationName(APPLICATION_NAME).build();
								
								
								if(!meta.getAppend()) //if Append is not checked we clear the sheet and we write content
								{
									//Clearing exsiting Sheet
									Sheets.Spreadsheets.Values.Clear request = data.service.spreadsheets().values().clear(environmentSubstitute(meta.getSpreadsheetKey()), range, requestBody);
									logBasic("Clearing Sheet:" +range +"in Spreadsheet :"+ environmentSubstitute(meta.getSpreadsheetKey()));
									if(request!=null){
									ClearValuesResponse response = request.execute();
									} else logBasic("Nothing to clear");
									//Writing Sheet
									logBasic("Writing to Sheet");
									ValueRange body = new ValueRange().setValues(data.rows);
									String valueInputOption="USER_ENTERED";
									UpdateValuesResponse result = data.service.spreadsheets().values().update(environmentSubstitute(meta.getSpreadsheetKey()), range, body).setValueInputOption(valueInputOption).execute();								
								
								} else { //Appending if option is checked

									// How the input data should be interpreted.
									String valueInputOption = "USER_ENTERED"; // TODO: Update placeholder value.

									// How the input data should be inserted.
									String insertDataOption = "INSERT_ROWS"; // TODO: Update placeholder value.

									// TODO: Assign values to desired fields of `requestBody`:
									ValueRange body = new ValueRange().setValues(data.rows);
									logBasic("Appending data :" +range +"in Spreadsheet :"+ environmentSubstitute(meta.getSpreadsheetKey()));

									Sheets.Spreadsheets.Values.Append request =data.service.spreadsheets().values().append(environmentSubstitute(meta.getSpreadsheetKey()), range, body);
									request.setValueInputOption(valueInputOption);
									request.setInsertDataOption(insertDataOption);
									AppendValuesResponse response = request.execute();

								}
							} else 
								{
									logBasic("No data found");
								}							
					setOutputDone();
					return false;
				} else {
					r= new ArrayList<Object>();
					for (int i = 0; i < data.outputRowMeta.size(); i++) {
						int length=row.length;
						//logBasic("Row length:"+length+" VS rowMeta "+data.outputRowMeta.size()+" i="+i);

						if(i<length && row[i]!=null)
						{
						r.add(row[i].toString());
						}
						else { r.add("");}
					}
					//logBasic("Adding row:"+Integer.toString(data.currentRow));

					data.rows.add(r);
					//logBasic("Added row:"+Integer.toString(data.currentRow));

					putRow(data.outputRowMeta, row);
					//logBasic("Puting row:"+Integer.toString(data.currentRow));


				}
		} catch (Exception e) {
			throw new KettleException(e.getMessage());
		} finally {
			data.currentRow++;
		}
	}

      
    return true;
  }
}