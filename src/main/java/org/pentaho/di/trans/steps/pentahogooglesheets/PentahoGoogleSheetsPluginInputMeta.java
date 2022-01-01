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

import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;
import org.pentaho.di.trans.steps.csvinput.CsvInputMeta;
import org.pentaho.di.trans.steps.file.BaseFileField;

import org.pentaho.di.trans.steps.pentahogooglesheets.PentahoGoogleSheetsPluginInputFields;


import java.util.ArrayList;
import java.util.List;
import java.security.KeyStore;

import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionDeep;
import org.pentaho.di.core.injection.InjectionSupported;


/**
 * Skeleton for PDI Step plugin.
 */

@Step(
	id = "PentahoGoogleSheetsPluginInputMeta", 
	image = "PentahoGoogleSheetsPluginInput.svg", 
	i18nPackageName = "org.pentaho.di.trans.steps.PentahoGoogleSheetsPluginInput",
    name = "PentahoGoogleSheetsPluginInput.Step.Name", 
	description = "PentahoGoogleSheetsPluginInput.Step.Name", 	
	categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Input",
	documentationUrl ="https://github.com/jfmonteil/Pentaho-Google-Sheet-Plugin/blob/master/README.md"
	) 
	
@InjectionSupported( localizationPrefix = "PentahoGoogleSheetsPluginInput.injection.", groups = { "SHEET", "INPUT_FIELDS" } )
public class PentahoGoogleSheetsPluginInputMeta extends BaseStepMeta implements StepMetaInterface {
	
	private static Class<?> PKG = PentahoGoogleSheetsPluginInputMeta.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
    public  PentahoGoogleSheetsPluginInputMeta() {
      super(); // allocate BaseStepMeta
	  allocate(0);
    }
    @Injection( name = "jsonCrendentialPath", group = "SHEET" )
    private String jsonCredentialPath;

	@Injection( name = "spreadsheetKey", group = "SHEET" )
    private String spreadsheetKey;
    
	@Injection( name = "worksheetId", group = "SHEET" )
	private String worksheetId;

    @Injection( name = "timeout", group = "SHEET" )
    private String timeout;

    @Injection( name = "impersonation", group = "SHEET" )
    private String impersonation;

	@Injection( name = "sampleFields", group = "INPUT_Fields" )
	private Integer sampleFields;

    @Injection( name = "appName", group = "SHEET" )
    private String appName;
	
	@InjectionDeep
	private PentahoGoogleSheetsPluginInputFields[] inputFields;
	
    @Override
    public void setDefault() {   
        this.spreadsheetKey = "";
        this.worksheetId = "";  
		this.jsonCredentialPath = Const.getKettleDirectory()+ "/client_secret.json";
		this.timeout="5";
		this.impersonation="";
        this.sampleFields=100;
   
   }
	
	public String getJsonCredentialPath() {
        return this.jsonCredentialPath == null ? "" : this.jsonCredentialPath;
    }

    public void setJsonCredentialPath(String key) {
        this.jsonCredentialPath = key;
    }
			
	public PentahoGoogleSheetsPluginInputFields[] getInputFields() {
		return inputFields;
	}

    public String getSpreadsheetKey() {
        return this.spreadsheetKey == null ? "" : this.spreadsheetKey;
    }

    public void setSpreadsheetKey(String key) {
        this.spreadsheetKey = key;
    }

    public String getWorksheetId() {
        return this.worksheetId == null ? "" : this.worksheetId;
    }

    public void setWorksheetId(String id) {
        this.worksheetId = id;
    }
    
	public int getSampleFields() {
        return this.sampleFields == null ? 100 : this.sampleFields;
    }

    public void setSampleFields(int sampleFields) {
        this.sampleFields = sampleFields;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }
    public String getTimeout() {
        return this.timeout == null ? "" : this.timeout;
    }

    public void setImpersonation(String impersonation) {
        this.impersonation = impersonation;
    }
    public String getImpersonation() {
        return this.impersonation == null ? "" : this.impersonation;
    }
    public void setAppName(String appName) {
        this.appName = appName;
    }
    public String getAppName() {
        return this.appName == null ? "" : this.appName;
    }
    public void allocate(int nrfields) {
	    inputFields = new PentahoGoogleSheetsPluginInputFields[nrfields];
	}



    @Override
    public Object clone() {
        PentahoGoogleSheetsPluginInputMeta retval = (PentahoGoogleSheetsPluginInputMeta) super.clone();
     
        int nrKeys = inputFields.length;
        retval.allocate(nrKeys);
		retval.setJsonCredentialPath(this.jsonCredentialPath);
		retval.setSpreadsheetKey(this.spreadsheetKey);
        retval.setWorksheetId(this.worksheetId);
        retval.setTimeout(this.timeout);
        retval.setImpersonation(this.impersonation);
        retval.setAppName(this.appName);
		retval.setSampleFields(this.sampleFields);
		for ( int i = 0; i < nrKeys; i++ ) {
			retval.inputFields[i] = (PentahoGoogleSheetsPluginInputFields) inputFields[i].clone();
        }
        return retval;
    }

    @Override
    public String getXML() throws KettleException {
        StringBuilder xml = new StringBuilder();
        try {         
            xml.append(XMLHandler.addTagValue("worksheetId", this.worksheetId));
			xml.append(XMLHandler.addTagValue("spreadsheetKey", this.spreadsheetKey));
     		xml.append(XMLHandler.addTagValue("jsonCredentialPath", this.jsonCredentialPath));
            xml.append(XMLHandler.addTagValue("timeout", this.timeout));
            xml.append(XMLHandler.addTagValue("impersonation", this.impersonation));
            xml.append(XMLHandler.addTagValue("appName", this.appName));
			String tmp="100";
			if(this.sampleFields!=null){
				xml.append(XMLHandler.addTagValue("sampleFields", this.sampleFields.toString()));
			}			
            else xml.append(XMLHandler.addTagValue("sampleFields", tmp));
			xml.append(XMLHandler.openTag("fields"));
            for ( int i = 0; i < inputFields.length; i++ ) {
			  PentahoGoogleSheetsPluginInputFields field = inputFields[i];
			  xml.append( "      <field>" ).append( Const.CR );
			  xml.append( "        " ).append( XMLHandler.addTagValue( "name", field.getName() ) );
			  xml.append( "        " ).append( XMLHandler.addTagValue( "type", field.getTypeDesc() ) );
			  xml.append( "        " ).append( XMLHandler.addTagValue( "format", field.getFormat() ) );
			  xml.append( "        " ).append( XMLHandler.addTagValue( "currency", field.getCurrencySymbol() ) );
			  xml.append( "        " ).append( XMLHandler.addTagValue( "decimal", field.getDecimalSymbol() ) );
			  xml.append( "        " ).append( XMLHandler.addTagValue( "group", field.getGroupSymbol() ) );
			  xml.append( "        " ).append( XMLHandler.addTagValue( "position", field.getPosition() ) );
			  xml.append( "        " ).append( XMLHandler.addTagValue( "length", field.getLength() ) );
			  xml.append( "        " ).append( XMLHandler.addTagValue( "precision", field.getPrecision() ) );
			  xml.append( "        " ).append( XMLHandler.addTagValue( "trim_type", field.getTrimTypeCode() ) );
			  xml.append( "      </field>" ).append( Const.CR );
			}
			xml.append( "    </fields>" ).append( Const.CR );

        } catch (Exception e) {
            throw new KettleValueException("Unable to write step to XML", e);
        }
        return xml.toString();
    }

    @Override
    public void loadXML(Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore) throws KettleXMLException {
        try {
           
            this.worksheetId = XMLHandler.getTagValue(stepnode, "worksheetId");
            this.spreadsheetKey = XMLHandler.getTagValue(stepnode, "spreadsheetKey");
            this.jsonCredentialPath = XMLHandler.getTagValue(stepnode, "jsonCredentialPath");
            this.timeout = XMLHandler.getTagValue(stepnode, "timeout");
            this.impersonation = XMLHandler.getTagValue(stepnode, "impersonation");
            this.appName = XMLHandler.getTagValue(stepnode, "appName");
			String tmp=XMLHandler.getTagValue(stepnode, "sampleField");
            if(tmp!=null && !tmp.isEmpty()){
			 this.sampleFields = Integer.parseInt(tmp);
			} else {
				this.sampleFields = 100;
			}
            Node fields = XMLHandler.getSubNode(stepnode, "fields");
            int nrfields = XMLHandler.countNodes(fields, "field");

            allocate(nrfields);

            for ( int i = 0; i < nrfields; i++ ) {
				Node fnode = XMLHandler.getSubNodeByNr( fields, "field", i );
				PentahoGoogleSheetsPluginInputFields field = new PentahoGoogleSheetsPluginInputFields();

				field.setName( XMLHandler.getTagValue( fnode, "name" ) );
				field.setType( ValueMetaFactory.getIdForValueMeta( XMLHandler.getTagValue( fnode, "type" ) ) );
				field.setFormat( XMLHandler.getTagValue( fnode, "format" ) );
				field.setCurrencySymbol( XMLHandler.getTagValue( fnode, "currency" ) );
				field.setDecimalSymbol( XMLHandler.getTagValue( fnode, "decimal" ) );
				field.setGroupSymbol( XMLHandler.getTagValue( fnode, "group" ) );
				//field.setNullString( XMLHandler.getTagValue( fnode, "nullif" ) );
				//field.setIfNullValue( XMLHandler.getTagValue( fnode, "ifnull" ) );
				field.setPosition( Const.toInt( XMLHandler.getTagValue( fnode, "position" ), -1 ) );
				field.setLength( Const.toInt( XMLHandler.getTagValue( fnode, "length" ), -1 ) );
				field.setPrecision( Const.toInt( XMLHandler.getTagValue( fnode, "precision" ), -1 ) );
				field.setTrimType( ValueMetaString.getTrimTypeByCode( XMLHandler.getTagValue( fnode, "trim_type" ) ) );
				//field.setRepeated( YES.equalsIgnoreCase( XMLHandler.getTagValue( fnode, "repeat" ) ) );

                inputFields[i] = field;
             }

        } catch (Exception e) {
            throw new KettleXMLException("Unable to load step from XML", e);
        }
    }

    @Override
    public void readRep(Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases) throws KettleException {
        try {

            this.worksheetId = rep.getStepAttributeString(id_step, "worksheetId");
            this.spreadsheetKey = rep.getStepAttributeString(id_step, "spreadsheetKey");
            this.jsonCredentialPath = rep.getStepAttributeString(id_step, "jsonCredentialPath");
            this.timeout = rep.getStepAttributeString(id_step, "timeout");
            this.impersonation = rep.getStepAttributeString(id_step, "impersonation");
            this.appName = rep.getStepAttributeString(id_step, "appName");
			this.sampleFields =(int) rep.getStepAttributeInteger(id_step, "sampleFields");
            this.appName = rep.getStepAttributeString(id_step, "appName");
            int nrfields = rep.countNrStepAttributes(id_step, "field_name");

            allocate(nrfields);

            for ( int i = 0; i < nrfields; i++ ) {
				PentahoGoogleSheetsPluginInputFields field = new PentahoGoogleSheetsPluginInputFields();

				field.setName( rep.getStepAttributeString( id_step, i, "field_name" ) );
				field.setType( ValueMetaFactory.getIdForValueMeta( rep.getStepAttributeString( id_step, i, "field_type" ) ) );
				field.setFormat( rep.getStepAttributeString( id_step, i, "field_format" ) );
				field.setCurrencySymbol( rep.getStepAttributeString( id_step, i, "field_currency" ) );
				field.setDecimalSymbol( rep.getStepAttributeString( id_step, i, "field_decimal" ) );
				field.setGroupSymbol( rep.getStepAttributeString( id_step, i, "field_group" ) );
				//field.setNullString( rep.getStepAttributeString( id_step, i, "field_nullif" ) );
				//field.setIfNullValue( rep.getStepAttributeString( id_step, i, "field_ifnull" ) );
				field.setPosition( (int) rep.getStepAttributeInteger( id_step, i, "field_position" ) );
				field.setLength( (int) rep.getStepAttributeInteger( id_step, i, "field_length" ) );
				field.setPrecision( (int) rep.getStepAttributeInteger( id_step, i, "field_precision" ) );
				field.setTrimType( ValueMetaString.getTrimTypeByCode( rep.getStepAttributeString( id_step, i, "field_trim_type" ) ) );
				//field.setRepeated( rep.getStepAttributeBoolean( id_step, i, "field_repeat" ) );

				inputFields[i] = field;
			  }
        } catch (Exception e) {
            throw new KettleException("Unexpected error reading step information from the repository", e);
        }
    }

    @Override
    public void saveRep(Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step) throws KettleException {
        try {
            rep.saveStepAttribute(id_transformation, id_step, "spreadsheetKey", this.spreadsheetKey);
            rep.saveStepAttribute(id_transformation, id_step, "worksheetId", this.worksheetId);
            rep.saveStepAttribute(id_transformation, id_step, "jsonCredentialPath", this.jsonCredentialPath);
            rep.saveStepAttribute(id_transformation, id_step, "sampleFields", this.sampleFields.toString());
            rep.saveStepAttribute(id_transformation, id_step, "timeout", this.timeout);
            rep.saveStepAttribute(id_transformation, id_step, "impersonation", this.impersonation);
            rep.saveStepAttribute(id_transformation, id_step, "appName", this.appName);
		    int nrfields = rep.countNrStepAttributes(id_step, "field_name");
           
		for ( int i = 0; i < inputFields.length; i++ ) {
			PentahoGoogleSheetsPluginInputFields field = inputFields[i];

			rep.saveStepAttribute( id_transformation, id_step, i, "field_name", field.getName() );
			rep.saveStepAttribute( id_transformation, id_step, i, "field_type", field.getTypeDesc() );
			rep.saveStepAttribute( id_transformation, id_step, i, "field_format", field.getFormat() );
			rep.saveStepAttribute( id_transformation, id_step, i, "field_currency", field.getCurrencySymbol() );
			rep.saveStepAttribute( id_transformation, id_step, i, "field_decimal", field.getDecimalSymbol() );
			rep.saveStepAttribute( id_transformation, id_step, i, "field_group", field.getGroupSymbol() );
			//rep.saveStepAttribute( id_transformation, id_step, i, "field_nullif", field.getNullString() );
			//rep.saveStepAttribute( id_transformation, id_step, i, "field_ifnull", field.getIfNullValue() );
			rep.saveStepAttribute( id_transformation, id_step, i, "field_position", field.getPosition() );
			rep.saveStepAttribute( id_transformation, id_step, i, "field_length", field.getLength() );
			rep.saveStepAttribute( id_transformation, id_step, i, "field_precision", field.getPrecision() );
			rep.saveStepAttribute( id_transformation, id_step, i, "field_trim_type", field.getTrimTypeCode() );
			//rep.saveStepAttribute( id_transformation, id_step, i, "field_repeat", field.isRepeated() );
		  }
        } catch (Exception e) {
            throw new KettleException("Unable to save step information to the repository for id_step=" + id_step, e);
        }
    }

    @Override
    public void getFields(RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space, Repository repository, IMetaStore metaStore) throws KettleStepException {
        try {
            inputRowMeta.clear(); // Start with a clean slate, eats the input
             for ( int i = 0; i < inputFields.length; i++ ) {
			  PentahoGoogleSheetsPluginInputFields field = inputFields[i];

			  int type = field.getType();
			  if ( type == ValueMetaInterface.TYPE_NONE ) {
				type = ValueMetaInterface.TYPE_STRING;
			  }

			  try {
				ValueMetaInterface v = ValueMetaFactory.createValueMeta( field.getName(), type );
				
				v.setLength( field.getLength() );
				v.setPrecision( field.getPrecision() );
				v.setOrigin( name );
				v.setConversionMask( field.getFormat() );
				v.setDecimalSymbol( field.getDecimalSymbol() );
				v.setGroupingSymbol( field.getGroupSymbol() );
				v.setCurrencySymbol( field.getCurrencySymbol() );
				//v.setDateFormatLenient( dateFormatLenient );
				//v.setDateFormatLocale( dateFormatLocale );
				v.setTrimType( field.getTrimType() );

				inputRowMeta.addValueMeta( v );
			  } catch ( Exception e ) {
				throw new KettleStepException( e );
			  }
			}
        } catch (Exception e) {

        }
    }

    @Override
    public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String[] input, String[] output, RowMetaInterface info, VariableSpace space, Repository repository, IMetaStore metaStore) {
        if (prev == null || prev.size() == 0) {
            remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_OK, "Not receiving any fields from previous steps.", stepMeta));
        } else {
            remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, String.format("Step is connected to previous one, receiving %1$d fields", prev.size()), stepMeta));
        }

        if (input.length > 0) {
            remarks.add( new CheckResult(CheckResultInterface.TYPE_RESULT_ERROR, "Step is receiving info from other steps!", stepMeta) );
        } else {
            remarks.add(new CheckResult(CheckResultInterface.TYPE_RESULT_OK, "No input received from other steps.", stepMeta));
        }
    }
  
  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta tr, Trans trans ) {
    return new PentahoGoogleSheetsPluginInput( stepMeta, stepDataInterface, cnr, tr, trans );
  }
  
  @Override
  public StepDataInterface getStepData() {
    return new PentahoGoogleSheetsPluginInputData();
  }
}

