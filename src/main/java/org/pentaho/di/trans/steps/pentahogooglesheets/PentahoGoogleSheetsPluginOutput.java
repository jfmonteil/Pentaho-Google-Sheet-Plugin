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
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;
import org.pentaho.di.i18n.BaseMessages;

import org.pentaho.di.core.variables.Variables;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

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
		String TOKENS_DIRECTORY_PATH = "public/tokens";
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
                    TOKENS_DIRECTORY_PATH = "public/tokens";   
					scope="https://www.googleapis.com/auth/drive.readonly";
					Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, PentahoGoogleSheetsPluginCredentials.getCredentialsJson(scope)).setApplicationName(APPLICATION_NAME).build();
                    FileList result = service.files().list().setPageSize(100).setFields("nextPageToken, files(id, name)").execute();
                    List<File> spreadsheets = result.getFiles();
	   //.setQ("mimeType='application/vnd.google-apps.spreadsheet'")
               
					
					String wsID=environmentSubstitute(meta.getSpreadsheetKey());
					for (File spreadsheet:spreadsheets) {
                        if(wsID.equals(spreadsheet.getId()))
						{
							exists=true; //file exists
							logBasic("Spreadsheet:"+ wsID +" exists");
						}
			                     
                    }			
				    //Init Service
					scope="https://www.googleapis.com/auth/spreadsheets";
					data.service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, PentahoGoogleSheetsPluginCredentials.getCredentialsJson(scope)).setApplicationName(APPLICATION_NAME).build();
								
					if(!exists)
					{						
						logBasic("SpreadSheet:"+ environmentSubstitute(meta.getSpreadsheetKey())+" does not exist Creating it");
						
						
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
    						logBasic("Sheet:"+ environmentSubstitute(meta.getWorksheetId())+" Renaming Sheet1 title to "+environmentSubstitute(meta.getWorksheetId()));

							//Getting "Sheet" 1 ID
							/*Spreadsheet response1= data.service.spreadsheets().get(environmentSubstitute(meta.getSpreadsheetKey())).setIncludeGridData(false).execute();          
                            List<Sheet> worksheets = response1.getSheets();
							Integer[] workSheetIds = new Integer[worksheets.size()];
							Sheet sheet = worksheets.get(0);
						    workSheetIds[0] = sheet.getProperties().getSheetId();
						    logBasic("Renaming sheet with ID:"	+workSheetIds[0]);	*/			
							//Renaming "Sheet 1" to user input
							SheetProperties title = new SheetProperties().setSheetId(0).setTitle(environmentSubstitute(meta.getWorksheetId()));
							// make a request with this properties
							UpdateSheetPropertiesRequest rename = new UpdateSheetPropertiesRequest().setProperties(title);
							// set fields you want to update
							rename.setFields("title");
							logBasic("Changing title to:"+ environmentSubstitute(meta.getWorksheetId()));				
							List<Request> requests = new ArrayList<>();
							Request request1 = new Request().setUpdateSheetProperties(rename);
							requests.add(request1);
							BatchUpdateSpreadsheetRequest requestBody = new BatchUpdateSpreadsheetRequest();
							requestBody.setRequests(requests);
		                     // now you can execute batchUpdate with your sheetsService and SHEET_ID
							data.service.spreadsheets().batchUpdate(spreadsheetID, requestBody).execute();
						}
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
   
/*public static List<List<Object>> getData() {

		List<Object> data1 = new ArrayList<Object>();
		data1.add("jagadeesh");
		data1.add("jagadeesh");
		data1.add("jagadeesh");

		List<Object> data2 = new ArrayList<Object>();
		data2.add("jagadeesh");
		data2.add("jagadeesh");
		data2.add("jagadeesh");

		List<List<Object>> data = new ArrayList<List<Object>>();
		data.add(data1);
		data.add(data2);

		return data;
	}*/

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    
	meta = (PentahoGoogleSheetsPluginOutputMeta) smi;
    data = (PentahoGoogleSheetsPluginOutputData) sdi;
	
	Object[] row = getRow();
    List<Object> r= new ArrayList<Object>();

	
    if (first && row!=null) {
		first = false;
		logBasic("Writing header");
		data.outputRowMeta=getInputRowMeta().clone();
		meta.getFields(data.outputRowMeta, getStepname(), null, null, this, repository, metaStore);	
		data.rows =  new ArrayList<List<Object>>();
		r= new ArrayList<Object>();

		for (int i = 0; i < data.outputRowMeta.size(); i++) {
			ValueMetaInterface v = data.outputRowMeta.getValueMeta(i);
			r.add(v.getName());			
		}
		data.rows.add(r);
	    data.currentRow++;	
		
	} else {
		try {
				//Object[] outputRowData = getRow();
				if (row == null) {
					        if(data.currentRow>0){
							logBasic("Clearing Sheet");
							ValueRange body = new ValueRange().setValues(data.rows);
							ClearValuesRequest requestBody = new ClearValuesRequest();
							
							String range=environmentSubstitute(meta.getWorksheetId());
							Sheets.Spreadsheets.Values.Clear request = data.service.spreadsheets().values().clear(environmentSubstitute(meta.getSpreadsheetKey()), range, requestBody);
                            ClearValuesResponse response = request.execute();
					        
							logBasic("Writing do Sheet");
							String valueInputOption="USER_ENTERED";
		                    UpdateValuesResponse result = data.service.spreadsheets().values().update(environmentSubstitute(meta.getSpreadsheetKey()), range, body).setValueInputOption(valueInputOption).execute();
							}
							else 
							{
								logBasic("No data found");
							}
					setOutputDone();
					return false;
				} else {
					r= new ArrayList<Object>();
					for (int i = 0; i < data.outputRowMeta.size(); i++) {
						int length=row.length;
						if(i<length && row[i]!=null)
						{
						r.add(row[i].toString());
						}
						else { r.add("");}
					}
					logBasic("Adding row:"+Integer.toString(data.currentRow));

					data.rows.add(r);
					putRow(data.outputRowMeta, row);

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