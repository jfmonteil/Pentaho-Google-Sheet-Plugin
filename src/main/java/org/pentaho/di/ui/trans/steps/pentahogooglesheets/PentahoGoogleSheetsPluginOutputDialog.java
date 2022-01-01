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
package org.pentaho.di.ui.trans.steps.pentahogooglesheets;

import org.pentaho.di.trans.steps.pentahogooglesheets.PentahoGoogleSheetsPluginCredentials;
import org.pentaho.di.trans.steps.pentahogooglesheets.PentahoGoogleSheetsPluginOutputMeta;


import com.google.api.client.http.HttpRequest;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.client.util.Base64;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.client.http.HttpRequestInitializer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ComboValuesSelectionListener;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.core.variables.Variables;


import java.io.IOException;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class PentahoGoogleSheetsPluginOutputDialog extends BaseStepDialog implements StepDialogInterface {

    private static final Class<?> PKG = PentahoGoogleSheetsPluginOutputMeta.class;

    private final PentahoGoogleSheetsPluginOutputMeta meta;

    private Label testServiceAccountInfo;
	private Label privateKeyInfo;
    private TextVar privateKeyStore;
    private TextVar spreadsheetKey;
    private TextVar worksheetId;
    private TextVar shareEmail;
	private TextVar shareDomain;
    private Button create;
	private Button append;
    private TextVar timeout;
    private TextVar impersonation;
    private TextVar appName;
	
    public PentahoGoogleSheetsPluginOutputDialog(Shell parent, Object in, TransMeta transMeta, String name) {
        super(parent, (BaseStepMeta) in, transMeta, name);
        this.meta = (PentahoGoogleSheetsPluginOutputMeta) in;
    }

    private static HttpRequestInitializer setHttpTimeout(final HttpRequestInitializer requestInitializer, final String timeout) {
        return new HttpRequestInitializer() {
            @Override
            public void initialize(HttpRequest httpRequest) throws IOException {
                requestInitializer.initialize(httpRequest);
                Integer TO = 5;
                if(!timeout.isEmpty()) {
                    TO = Integer.parseInt(timeout);
                }



                httpRequest.setConnectTimeout(TO * 60000);  // 3 minutes connect timeout
                httpRequest.setReadTimeout(TO * 60000);  // 3 minutes read timeout
            }};

    }

    @Override
    public String open() {
        Shell parent = this.getParent();
        Display display = parent.getDisplay();

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
        props.setLook(shell);
        setShellImage(shell, meta);

        ModifyListener modifiedListener = new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                meta.setChanged();
            }
        };
		
	    SelectionAdapter lsSa=new SelectionAdapter() {		
			public void modifySelect( SelectionEvent e ) {
			   meta.setChanged();
			   }
		};
		
		changed = meta.hasChanged();


        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText("Google Spreadsheet Output APIV4");

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // stepname - Label
        wlStepname = new Label(shell, SWT.RIGHT);
        wlStepname.setText(BaseMessages.getString(PKG, "System.Label.StepName"));
        props.setLook(wlStepname);
        fdlStepname = new FormData();
        fdlStepname.top = new FormAttachment(0, margin);
        fdlStepname.left = new FormAttachment(0, 0);
        fdlStepname.right = new FormAttachment(middle, -margin);
        wlStepname.setLayoutData(fdlStepname);

        // stepname - Text
        wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wStepname.setText(stepname);
        props.setLook(wStepname);
        wStepname.addModifyListener(modifiedListener);
        fdStepname = new FormData();
        fdStepname.top = new FormAttachment(0, margin);
        fdStepname.left = new FormAttachment(middle, 0);
        fdStepname.right = new FormAttachment(100, 0);
        wStepname.setLayoutData(fdStepname);

        CTabFolder tabFolder = new CTabFolder(shell, SWT.BORDER);
        props.setLook(tabFolder, Props.WIDGET_STYLE_TAB);
        tabFolder.setSimple(false);

        /*
         * BEGIN Service Account Tab
         */
        CTabItem serviceAccountTab = new CTabItem(tabFolder, SWT.NONE);
        serviceAccountTab.setText("Service Account");

        Composite serviceAccountComposite = new Composite(tabFolder, SWT.NONE);
        props.setLook(serviceAccountComposite);

        FormLayout serviceAccountLayout = new FormLayout();
        serviceAccountLayout.marginWidth = 3;
        serviceAccountLayout.marginHeight = 3;
        serviceAccountComposite.setLayout(serviceAccountLayout);
		
		// privateKey json - Label
        Label privateKeyLabel = new Label( serviceAccountComposite, SWT.RIGHT );
        privateKeyLabel.setText( "Json credential file (default .kettle directory/client-secret.json is used) :" );
        props.setLook( privateKeyLabel );
        FormData privateKeyLabelForm = new FormData();
        privateKeyLabelForm.top = new FormAttachment( 0, margin );
        privateKeyLabelForm.left = new FormAttachment( 0, 0 );
        privateKeyLabelForm.right = new FormAttachment( middle, -margin );
        privateKeyLabel.setLayoutData( privateKeyLabelForm );

        // privateKey - Button
        Button privateKeyButton = new Button( serviceAccountComposite, SWT.PUSH | SWT.CENTER );
        props.setLook( privateKeyButton );
        privateKeyButton.setText( "Browse" );
        FormData privateKeyButtonForm = new FormData();
        privateKeyButtonForm.top = new FormAttachment( 0, margin );
		privateKeyButtonForm.right = new FormAttachment(100, 0);
        privateKeyButton.setLayoutData( privateKeyButtonForm );

      
	   // privatekey - Text
        privateKeyStore = new TextVar(transMeta,serviceAccountComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(privateKeyStore);
        privateKeyStore.addModifyListener(modifiedListener);
        FormData privateKeyStoreData = new FormData();
        privateKeyStoreData.top = new FormAttachment(0, margin);
        privateKeyStoreData.left = new FormAttachment(middle, 0);
		privateKeyStoreData.right = new FormAttachment(privateKeyButton, -margin);
        privateKeyStore.setLayoutData(privateKeyStoreData);

        // Appname - Label
        Label appNameLabel = new Label( serviceAccountComposite, SWT.RIGHT );
        appNameLabel.setText( "Google Application Name :" );
        props.setLook( appNameLabel );
        FormData appNameLabelForm = new FormData();
        appNameLabelForm.top = new FormAttachment( privateKeyButton, margin );
        appNameLabelForm.left = new FormAttachment( 0, 0 );
        appNameLabelForm.right = new FormAttachment( middle, -margin );
        appNameLabel.setLayoutData( appNameLabelForm );

        // Appname - Text
        appName = new TextVar(transMeta,serviceAccountComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(appName);
        appName.addModifyListener(modifiedListener);
        FormData appNameData = new FormData();
        appNameData.top = new FormAttachment(privateKeyButton, margin);
        appNameData.left = new FormAttachment(middle, 0);
        appNameData.right = new FormAttachment(privateKeyButton, -margin);
        appName.setLayoutData(appNameData);

        // Timeout - Label
        Label timeoutLabel = new Label( serviceAccountComposite, SWT.RIGHT );
        timeoutLabel.setText( "Time out in minutes :" );
        props.setLook( timeoutLabel );
        FormData timeoutLabelForm = new FormData();
        timeoutLabelForm.top = new FormAttachment( appNameLabel, margin );
        timeoutLabelForm.left = new FormAttachment( 0, 0 );
        timeoutLabelForm.right = new FormAttachment( middle, -margin );
        timeoutLabel.setLayoutData( timeoutLabelForm );

        // timeout - Text
        timeout = new TextVar(transMeta,serviceAccountComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(timeout);
        timeout.addModifyListener(modifiedListener);
        FormData timeoutData = new FormData();
        timeoutData.top = new FormAttachment(appNameLabel, margin);
        timeoutData.left = new FormAttachment(middle, 0);
        timeoutData.right = new FormAttachment(privateKeyButton, -margin);
        timeout.setLayoutData(timeoutData);

        // Impersonation - Label
        Label impersonationLabel = new Label( serviceAccountComposite, SWT.RIGHT );
        impersonationLabel.setText( "Inpersonation account :" );
        props.setLook( impersonationLabel );
        FormData impersonationLabelForm = new FormData();
        impersonationLabelForm.top = new FormAttachment( timeout, margin );
        impersonationLabelForm.left = new FormAttachment( 0, 0 );
        impersonationLabelForm.right = new FormAttachment( middle, -margin );
        impersonationLabel.setLayoutData( impersonationLabelForm );

        // impersonation - Text
        impersonation = new TextVar(transMeta,serviceAccountComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(impersonation);
        impersonation.addModifyListener(modifiedListener);
        FormData impersonationData = new FormData();
        impersonationData.top = new FormAttachment(timeout, margin);
        impersonationData.left = new FormAttachment(middle, 0);
        impersonationData.right = new FormAttachment(privateKeyButton, -margin);
        impersonation.setLayoutData(impersonationData);

        // test service - Button
        Button testServiceAccountButton = new Button(serviceAccountComposite, SWT.PUSH | SWT.CENTER);
        props.setLook(testServiceAccountButton);
        testServiceAccountButton.setText("Test Connection");
        FormData testServiceAccountButtonData = new FormData();
        testServiceAccountButtonData.top = new FormAttachment(impersonation, margin);
        testServiceAccountButtonData.left = new FormAttachment(0, 0);
        testServiceAccountButton.setLayoutData(testServiceAccountButtonData);

        testServiceAccountInfo = new Label(serviceAccountComposite, SWT.LEFT);
        props.setLook(testServiceAccountInfo);
        FormData testServiceAccountInfoData = new FormData();
        testServiceAccountInfoData.top = new FormAttachment(impersonation, margin);
        testServiceAccountInfoData.left = new FormAttachment(middle, 0);
        testServiceAccountInfoData.right = new FormAttachment(100, 0);
        testServiceAccountInfo.setLayoutData(testServiceAccountInfoData);

        FormData serviceAccountCompositeData = new FormData();
        serviceAccountCompositeData.left = new FormAttachment(0, 0);
        serviceAccountCompositeData.top = new FormAttachment(0, 0);
        serviceAccountCompositeData.right = new FormAttachment(100, 0);
        serviceAccountCompositeData.bottom = new FormAttachment(100, 0);
        serviceAccountComposite.setLayoutData(serviceAccountCompositeData);

        serviceAccountComposite.layout();
        serviceAccountTab.setControl(serviceAccountComposite);
        /*
         * END Service Account Tab
         */

        /*
         * BEGIN Spreadsheet Tab
         */
        CTabItem spreadsheetTab = new CTabItem(tabFolder, SWT.NONE);
        spreadsheetTab.setText("Spreadsheet");

        Composite spreadsheetComposite = new Composite(tabFolder, SWT.NONE);
        props.setLook(spreadsheetComposite);

        FormLayout spreadsheetLayout = new FormLayout();
        spreadsheetLayout.marginWidth = 3;
        spreadsheetLayout.marginHeight = 3;
        spreadsheetComposite.setLayout(spreadsheetLayout);

        // spreadsheetKey - Label
        Label spreadsheetKeyLabel = new Label(spreadsheetComposite, SWT.RIGHT);
        spreadsheetKeyLabel.setText("Spreadsheet Key");
        props.setLook(spreadsheetKeyLabel);
        FormData spreadsheetKeyLabelData = new FormData();
        spreadsheetKeyLabelData.top = new FormAttachment(0, margin);
        spreadsheetKeyLabelData.left = new FormAttachment(0, 0);
        spreadsheetKeyLabelData.right = new FormAttachment(middle, -margin);
        spreadsheetKeyLabel.setLayoutData(spreadsheetKeyLabelData);

        // spreadsheetKey - Button
        Button spreadsheetKeyButton = new Button(spreadsheetComposite, SWT.PUSH | SWT.CENTER);
        spreadsheetKeyButton.setText("Browse");
        props.setLook(spreadsheetKeyButton);
        FormData spreadsheetKeyButtonData = new FormData();
        spreadsheetKeyButtonData.top = new FormAttachment(0, margin);
        spreadsheetKeyButtonData.right = new FormAttachment(100, 0);
        spreadsheetKeyButton.setLayoutData(spreadsheetKeyButtonData);

        // spreadsheetKey - Text
        spreadsheetKey = new TextVar(transMeta,spreadsheetComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(spreadsheetKey);
        spreadsheetKey.addModifyListener(modifiedListener);
        FormData spreadsheetKeyData = new FormData();
        spreadsheetKeyData.top = new FormAttachment(0, margin);
        spreadsheetKeyData.left = new FormAttachment(middle, 0);
        spreadsheetKeyData.right = new FormAttachment(spreadsheetKeyButton, -margin);
        spreadsheetKey.setLayoutData(spreadsheetKeyData);

        // worksheetId - Label
        Label worksheetIdLabel = new Label(spreadsheetComposite, SWT.RIGHT);
        worksheetIdLabel.setText("Worksheet Id");
        props.setLook(worksheetIdLabel);
        FormData worksheetIdLabelData = new FormData();
        worksheetIdLabelData.top = new FormAttachment(spreadsheetKeyButton, margin);
        worksheetIdLabelData.left = new FormAttachment(0, 0);
        worksheetIdLabelData.right = new FormAttachment(middle, -margin);
        worksheetIdLabel.setLayoutData(worksheetIdLabelData);

        // worksheetId - Button
        Button worksheetIdButton = new Button(spreadsheetComposite, SWT.PUSH | SWT.CENTER);
        worksheetIdButton.setText("Browse");
        props.setLook(worksheetIdButton);
        FormData worksheetIdButtonData = new FormData();
        worksheetIdButtonData.top = new FormAttachment(spreadsheetKeyButton, margin);
        worksheetIdButtonData.right = new FormAttachment(100, 0);
        worksheetIdButton.setLayoutData(worksheetIdButtonData);

        // worksheetId - Text
        worksheetId = new TextVar(transMeta,spreadsheetComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(worksheetId);
        worksheetId.addModifyListener(modifiedListener);
        FormData worksheetIdData = new FormData();
        worksheetIdData.top = new FormAttachment(spreadsheetKeyButton, margin);
        worksheetIdData.left = new FormAttachment(middle, 0);
        worksheetIdData.right = new FormAttachment(worksheetIdButton, -margin);
        worksheetId.setLayoutData(worksheetIdData);
				
		//Append tick box label
		Label wlAppend=new Label( spreadsheetComposite, SWT.RIGHT );
		wlAppend.setText(BaseMessages.getString(PKG, "PentahoGoogleSheetsPluginOutputDialog.Append.Label" ));
		props.setLook( wlAppend );
		FormData fdAppend = new FormData();
		fdAppend.top = new FormAttachment( worksheetIdButton, margin );
		fdAppend.left = new FormAttachment( 0, 0 );
		fdAppend.right = new FormAttachment( middle, -margin );
		wlAppend.setLayoutData( fdAppend );
		
		//Append tick box button
		append = new Button( spreadsheetComposite, SWT.CHECK );
		props.setLook( append );
	    append.addSelectionListener(lsSa);
		fdAppend = new FormData();
		fdAppend.top = new FormAttachment( worksheetIdButton, margin );
		fdAppend.left = new FormAttachment( middle, 0 );
		fdAppend.right = new FormAttachment( 100, 0 );
		append.setLayoutData( fdAppend );
		
		//Create New Sheet tick box label
		Label wlCreate=new Label( spreadsheetComposite, SWT.RIGHT );
		wlCreate.setText(BaseMessages.getString(PKG, "PentahoGoogleSheetsPluginOutputDialog.Create.Label" ));
		props.setLook( wlCreate );
		FormData fdCreate = new FormData();
		fdCreate.top = new FormAttachment( append, margin );
		fdCreate.left = new FormAttachment( 0, 0 );
		fdCreate.right = new FormAttachment( middle, -margin );
		wlCreate.setLayoutData( fdCreate );
		
		//Create New Sheet tick box button
		create = new Button( spreadsheetComposite, SWT.CHECK );
		props.setLook( create );
	    create.addSelectionListener(lsSa);
		fdCreate = new FormData();
		fdCreate.top = new FormAttachment( append, margin );
		fdCreate.left = new FormAttachment( middle, 0 );
		fdCreate.right = new FormAttachment( 100, 0 );
		create.setLayoutData( fdCreate );
	
		// Share spreadsheet with label
		Label wlShare = new Label( spreadsheetComposite, SWT.RIGHT );
		wlShare.setText( BaseMessages.getString( PKG,"PentahoGoogleSheetsPluginOutputDialog.Share.Label" ));
		props.setLook( wlShare );
		FormData fdlShare = new FormData();
		fdlShare.top = new FormAttachment( create, margin );
		fdlShare.left = new FormAttachment( 0, 0 );
		fdlShare.right = new FormAttachment( middle, -margin );
		wlShare.setLayoutData( fdlShare );
		// Share spreadsheet with label
		shareEmail = new TextVar( transMeta,spreadsheetComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
		props.setLook( shareEmail );
		shareEmail.addModifyListener( modifiedListener );
		FormData fdShare = new FormData();
		fdShare.top = new FormAttachment( create, margin );
		fdShare.left = new FormAttachment( middle, 0 );
		fdShare.right = new FormAttachment( 100, 0 );
		shareEmail.setLayoutData( fdShare );
    
	    // Share domainwise with label
		Label wlShareDW = new Label( spreadsheetComposite, SWT.RIGHT );
		wlShareDW.setText( BaseMessages.getString( PKG,"PentahoGoogleSheetsPluginOutputDialog.Share.LabelDW" ));
		props.setLook( wlShareDW );
		FormData fdlShareDW = new FormData();
		fdlShareDW.top = new FormAttachment( shareEmail, margin );
		fdlShareDW.left = new FormAttachment( 0, 0 );
		fdlShareDW.right = new FormAttachment( middle, -margin );
		wlShareDW.setLayoutData( fdlShareDW );
		// Share domainwise with label
		shareDomain = new TextVar( transMeta,spreadsheetComposite, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
		props.setLook( shareDomain );
		shareDomain.addModifyListener( modifiedListener );
		FormData fdShareDW = new FormData();
		fdShareDW.top = new FormAttachment( shareEmail, margin );
		fdShareDW.left = new FormAttachment( middle, 0 );
		fdShareDW.right = new FormAttachment( 100, 0 );
		shareDomain.setLayoutData( fdShareDW );

        FormData spreadsheetCompositeData = new FormData();
        spreadsheetCompositeData.left = new FormAttachment(0, 0);
        spreadsheetCompositeData.top = new FormAttachment(0, 0);
        spreadsheetCompositeData.right = new FormAttachment(100, 0);
        spreadsheetCompositeData.bottom = new FormAttachment(100, 0);
        spreadsheetComposite.setLayoutData(spreadsheetCompositeData);

        spreadsheetComposite.layout();
        spreadsheetTab.setControl(spreadsheetComposite);
        /*
         * END Spreadsheet Tab
         */
     
        FormData tabFolderData = new FormData();
        tabFolderData.left = new FormAttachment(0, 0);
        tabFolderData.top = new FormAttachment(wStepname, margin);
        tabFolderData.right = new FormAttachment(100, 0);
        tabFolderData.bottom = new FormAttachment(100, -50);
        tabFolder.setLayoutData(tabFolderData);

        // OK and cancel buttons
        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));

        BaseStepDialog.positionBottomButtons(shell, new Button[]{wOK, wCancel}, margin, tabFolder);

        lsCancel = new Listener() {
            @Override
            public void handleEvent(Event e) {
                cancel();
            }
        };
        lsOK = new Listener() {
            @Override
            public void handleEvent(Event e) {
                ok();
            }
        };
       
        wCancel.addListener(SWT.Selection, lsCancel);
        wOK.addListener(SWT.Selection, lsOK);
      //  wGet.addListener(SWT.Selection, lsGet);
        // default listener (for hitting "enter")
        lsDef = new SelectionAdapter() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                ok();
            }
        };
        wStepname.addSelectionListener(lsDef);
		//credential.json file selection
		privateKeyButton.addSelectionListener( new SelectionAdapter() {
            @Override
            public void widgetSelected( SelectionEvent e ) {
                FileDialog dialog = new FileDialog( shell, SWT.OPEN );
                dialog.setFilterExtensions( new String[] { "*json", "*" } );
                dialog.setFilterNames( new String[] { "credential JSON file", "All Files" } );
                String filename = dialog.open();
                if ( filename != null ) {
					 privateKeyStore.setText(filename);
					 meta.setChanged();
                }
            }
        } );


//testing connection to Google with API V4
        testServiceAccountButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {						
                    NetHttpTransport HTTP_TRANSPORT=GoogleNetHttpTransport.newTrustedTransport();
				    String APPLICATION_NAME = transMeta.environmentSubstitute(appName.getText());
                    JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
                    String TOKENS_DIRECTORY_PATH = Const.getKettleDirectory() +"/tokens";
					String scope=SheetsScopes.SPREADSHEETS_READONLY;
					Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, setHttpTimeout(PentahoGoogleSheetsPluginCredentials.getCredentialsJson(scope,transMeta.environmentSubstitute(privateKeyStore.getText()), transMeta.environmentSubstitute(impersonation.getText())),transMeta.environmentSubstitute(timeout.getText()))).setApplicationName(APPLICATION_NAME).build();
                    testServiceAccountInfo.setText("");

                    if (service == null) {
                        testServiceAccountInfo.setText("Connection Failed");
                    } else {
                        testServiceAccountInfo.setText("Google Drive API : Success!");
                    }
                } catch (Exception error) {
                    testServiceAccountInfo.setText("Connection Failed");
                }
            }
        });
// Display spreadsheets
        spreadsheetKeyButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                    NetHttpTransport HTTP_TRANSPORT=GoogleNetHttpTransport.newTrustedTransport();
				    String APPLICATION_NAME = transMeta.environmentSubstitute(appName.getText());
                    JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
                    String TOKENS_DIRECTORY_PATH = Const.getKettleDirectory() +"/tokens";   
					String scope="https://www.googleapis.com/auth/drive";
                    Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, setHttpTimeout(
                            PentahoGoogleSheetsPluginCredentials.getCredentialsJson(scope,transMeta.environmentSubstitute(privateKeyStore.getText()),transMeta.environmentSubstitute(meta.getImpersonation())),
                            transMeta.environmentSubstitute(meta.getTimeout()))).setApplicationName(APPLICATION_NAME).build();
					FileList result = service.files().list().setSupportsAllDrives(true).setIncludeItemsFromAllDrives(true).setQ("mimeType='application/vnd.google-apps.spreadsheet'").setPageSize(100).setFields("nextPageToken, files(id, name)").execute();
					List<File> spreadsheets = result.getFiles();
                    int selectedSpreadsheet = -1;
                    int i=0;
					String[] titles=new String[spreadsheets.size()];
					for (File spreadsheet:spreadsheets) {
                        titles[i] = spreadsheet.getName()+" - "+spreadsheet.getId();
                        if (spreadsheet.getId().equals(spreadsheetKey.getText())) {
                            selectedSpreadsheet = i;
                        }
						i++;
                    }

                    EnterSelectionDialog esd = new EnterSelectionDialog(shell, titles, "Spreadsheets",
                            "Select a Spreadsheet.");
                    if (selectedSpreadsheet > -1) {
                        esd.setSelectedNrs(new int[]{selectedSpreadsheet});
                    }
					String s=esd.open();
                    if(s!=null)
					{
						if (esd.getSelectionIndeces().length > 0) {
							selectedSpreadsheet = esd.getSelectionIndeces()[0];
							File spreadsheet = spreadsheets.get(selectedSpreadsheet);
							spreadsheetKey.setText(spreadsheet.getId());						
						} 
						else {
							spreadsheetKey.setText("");
						}
					}

                } catch (Exception err) {
                    new ErrorDialog(shell, "System.Dialog.Error.Title", err.getMessage(), err);
                }
            }
        });
//Display worksheets
        worksheetIdButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {
                  					
					NetHttpTransport HTTP_TRANSPORT=GoogleNetHttpTransport.newTrustedTransport();
				    String APPLICATION_NAME = transMeta.environmentSubstitute(appName.getText());
                    JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
                    String TOKENS_DIRECTORY_PATH = Const.getKettleDirectory() +"/tokens";
					String scope=SheetsScopes.SPREADSHEETS_READONLY;
					
					Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, setHttpTimeout(PentahoGoogleSheetsPluginCredentials.getCredentialsJson(scope,transMeta.environmentSubstitute(privateKeyStore.getText()),transMeta.environmentSubstitute(impersonation.getText())),transMeta.environmentSubstitute(timeout.getText()))).setApplicationName(APPLICATION_NAME).build();
					Spreadsheet response1= service.spreadsheets().get(spreadsheetKey.getText()).setIncludeGridData(false).execute();

                    
                    List<Sheet> worksheets = response1.getSheets();
                    String[] names = new String[worksheets.size()];
                    int selectedSheet = -1;
                    for (int i = 0; i < worksheets.size(); i++) {
                        Sheet sheet = worksheets.get(i);
                        names[i] = sheet.getProperties().getTitle();
                        if (sheet.getProperties().getTitle().endsWith("/" + worksheetId.getText())) {
                            selectedSheet = i;
                        }
                    }

                    EnterSelectionDialog esd = new EnterSelectionDialog(shell, names, "Worksheets", "Select a Worksheet.");
                    if (selectedSheet > -1) {
                        esd.setSelectedNrs(new int[]{selectedSheet});
                    }
					String s=esd.open();
                    if(s!=null){
						if (esd.getSelectionIndeces().length > 0) {
							selectedSheet = esd.getSelectionIndeces()[0];  						
							Sheet sheet = worksheets.get(selectedSheet);
							String id = sheet.getProperties().getTitle();
							worksheetId.setText(id.substring(id.lastIndexOf("/") + 1));
						} 
						else {
							worksheetId.setText("");
						}
					}

                } catch (Exception err) {
                    new ErrorDialog(shell, BaseMessages.getString(PKG, "System.Dialog.Error.Title"), err.getMessage(), err);
                }

            }
        });

        shell.addShellListener(new ShellAdapter() {
            @Override
            public void shellClosed(ShellEvent e) {
                cancel();
            }
        });

        tabFolder.setSelection(0);
        setSize();
        getData(meta);
        meta.setChanged(changed);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        return stepname;
    }

    private void getData(PentahoGoogleSheetsPluginOutputMeta meta) {
        this.wStepname.selectAll();

        this.spreadsheetKey.setText(meta.getSpreadsheetKey());
		this.worksheetId.setText(meta.getWorksheetId());
		this.shareEmail.setText(meta.getShareEmail());
		this.create.setSelection( meta.getCreate() );
		this.append.setSelection( meta.getAppend() );
        this.timeout.setText( meta.getTimeout() );
        this.impersonation.setText(meta.getImpersonation());
        this.appName.setText( meta.getAppName());
        this.shareDomain.setText(meta.getShareDomain());
		this.privateKeyStore.setText(meta.getJsonCredentialPath());
       
    
    }

    private void setData(PentahoGoogleSheetsPluginOutputMeta meta) {
  
        meta.setJsonCredentialPath(this.privateKeyStore.getText());
		meta.setSpreadsheetKey(this.spreadsheetKey.getText());
        meta.setWorksheetId(this.worksheetId.getText());
		meta.setShareEmail(this.shareEmail.getText());
		meta.setCreate(this.create.getSelection());
		meta.setAppend(this.append.getSelection());
        meta.setShareDomain(this.shareDomain.getText());
        meta.setTimeout(this.timeout.getText());
        meta.setAppName(this.appName.getText());
        meta.setImpersonation(this.impersonation.getText());
    }

    private void cancel() {
        stepname = null;
        meta.setChanged(changed);
        dispose();
    }

    private void ok() {
        stepname = wStepname.getText();
        setData(this.meta);
        dispose();
    }

}
