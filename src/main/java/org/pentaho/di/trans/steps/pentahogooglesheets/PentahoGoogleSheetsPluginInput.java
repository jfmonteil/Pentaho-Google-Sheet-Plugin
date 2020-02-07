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
import org.pentaho.di.core.Const;

import org.pentaho.di.core.variables.Variables;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.api.services.sheets.v4.SheetsScopes;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;


import org.pentaho.di.trans.steps.pentahogooglesheets.PentahoGoogleSheetsPluginInputMeta;
import org.pentaho.di.trans.steps.pentahogooglesheets.PentahoGoogleSheetsPluginInputData;
import org.pentaho.di.trans.steps.pentahogooglesheets.PentahoGoogleSheetsPluginCredentials;

/**
 * Describe your step plugin.
 * 
 */

public class PentahoGoogleSheetsPluginInput extends BaseStep implements StepInterface {

  private static Class<?> PKG = PentahoGoogleSheetsPluginInput.class; // for i18n purposes, needed by Translator2!!
  
  private PentahoGoogleSheetsPluginInputMeta meta;
  private PentahoGoogleSheetsPluginInputData data;
  
  public PentahoGoogleSheetsPluginInput( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta, Trans trans ) {
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
        
		meta = (PentahoGoogleSheetsPluginInputMeta) smi;
        data = (PentahoGoogleSheetsPluginInputData) sdi;
        JsonFactory JSON_FACTORY=null;
		NetHttpTransport HTTP_TRANSPORT = null;
		String APPLICATION_NAME = "pentaho-sheets";
		String TOKENS_DIRECTORY_PATH = Const.getKettleDirectory() +"/tokens";
        String scope=SheetsScopes.SPREADSHEETS_READONLY;
     
	    try {
   	   	    JSON_FACTORY = JacksonFactory.getDefaultInstance();
			HTTP_TRANSPORT=GoogleNetHttpTransport.newTrustedTransport();			
		} catch (Exception e) {
			logError("Exception",e.getMessage(),e);
		}
		
        
		if (super.init(smi, sdi)) {
            try {				
				Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, PentahoGoogleSheetsPluginCredentials.getCredentialsJson(scope)).setApplicationName(APPLICATION_NAME).build();
				String range=environmentSubstitute(meta.getWorksheetId());
				ValueRange response = service.spreadsheets().values().get(environmentSubstitute(meta.getSpreadsheetKey()),range).execute();             
				if(response==null) {
					logError("No data found for worksheet : "+environmentSubstitute( meta.getWorksheetId())+" in spreadshit :"+environmentSubstitute( meta.getSpreadsheetKey()));
				} else 
					{				
					List<List<Object>> values = response.getValues();
					logBasic("Reading Sheet, found: "+values.size()+" rows");
					if (values == null || values.isEmpty()) {
						logError("No response found for worksheet : "+environmentSubstitute( meta.getWorksheetId())+" in spreadsheet :"+environmentSubstitute( meta.getSpreadsheetKey()));
						} else {
							data.rows=values;
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
   

  public boolean processRow( StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {
    
	meta = (PentahoGoogleSheetsPluginInputMeta) smi;
    data = (PentahoGoogleSheetsPluginInputData) sdi;
	
    if (first) {
		first = false;
		data.outputRowMeta = new RowMeta();
		meta.getFields(data.outputRowMeta, getStepname(), null, null, this, repository, metaStore);
		data.currentRow++;
		
	} else {
		try {
				Object[] outputRowData = readRow();
				if (outputRowData == null) {
					setOutputDone();
					return false;
				} else {
					putRow(data.outputRowMeta, outputRowData);
				}
		} catch (Exception e) {
			throw new KettleException(e.getMessage());
		} finally {
			data.currentRow++;
		}
	}

      
    return true;
  }
  
   private Object[] readRow() {
        try {
            logRowlevel("Allocating :" + Integer.toString(data.outputRowMeta.size()));
			Object[] outputRowData = RowDataUtil.allocateRowData(data.outputRowMeta.size());
            int outputIndex = 0;
		    int logcur=data.currentRow;			
			logRowlevel("Reading Row: "+Integer.toString(data.currentRow)+" out of : "+Integer.toString(data.rows.size()));         
			if (data.currentRow < data.rows.size()) {
                List<Object> row= data.rows.get(data.currentRow);
                for (ValueMetaInterface column : data.outputRowMeta.getValueMetaList()) {
                String value="";				
				logRowlevel("Reading columns: "+Integer.toString(outputIndex)+" out of : "+Integer.toString(row.size()));
				if(outputIndex>row.size()-1){
				  logRowlevel("Beyond size"); 
				  outputRowData[outputIndex++] = null;
				}
				else {	
						if(row.get(outputIndex)!=null){
							logRowlevel("getting value" +Integer.toString(outputIndex));
							value = row.get(outputIndex).toString();
							logRowlevel("got value "+Integer.toString(outputIndex));

						}
						if (value == null||value.isEmpty()||value==""){
							outputRowData[outputIndex++] = null;
							logRowlevel("null value");
						}
						else {
							outputRowData[outputIndex++] = value;	
							logRowlevel("value : "+value);
						}
					 }
                }
            } else {
                logBasic("Finished reading last row "+ Integer.toString(data.currentRow) +" / "+Integer.toString(data.rows.size()));
				return null;
            }
            return outputRowData;
        } catch (Exception e) {
            logError("Exception reading value :" +e.getMessage());
			return null;
        }
    }
}