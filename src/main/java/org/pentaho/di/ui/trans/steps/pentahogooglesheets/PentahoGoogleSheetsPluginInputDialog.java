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
import org.pentaho.di.trans.steps.pentahogooglesheets.PentahoGoogleSheetsPluginInputMeta;


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
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.client.util.Base64;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

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
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.ComboValuesSelectionListener;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.trans.step.BaseStepDialog;

import javax.security.auth.x500.X500Principal;
import java.io.FileInputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public class PentahoGoogleSheetsPluginInputDialog extends BaseStepDialog implements StepDialogInterface {

    private static final Class<?> PKG = PentahoGoogleSheetsPluginInputMeta.class;

    private final PentahoGoogleSheetsPluginInputMeta meta;

    private Label privateKeyInfo;
    private Label testServiceAccountInfo;
  //  private Text serviceEmail;
  //  private String privateKeyStore;
    private TextVar spreadsheetKey;
    private TextVar worksheetId;
    private TableView wFields;
	
    public PentahoGoogleSheetsPluginInputDialog(Shell parent, Object in, TransMeta transMeta, String name) {
        super(parent, (BaseStepMeta) in, transMeta, name);
        this.meta = (PentahoGoogleSheetsPluginInputMeta) in;
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

        ModifyListener contentListener = new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent arg0) {
                // asyncUpdatePreview();
            }
        };
		
		changed = meta.hasChanged();

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText("Google Spreadsheet Input APIV4");

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // stepname - Label
        wlStepname = new Label(shell, SWT.RIGHT);
        wlStepname.setText(BaseMessages.getString(PKG, "PentahoGoogleSheetsPluginInputDialog.Stepname.Label"));
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

        // test service - Button
        Button testServiceAccountButton = new Button(serviceAccountComposite, SWT.PUSH | SWT.CENTER);
        props.setLook(testServiceAccountButton);
        testServiceAccountButton.setText("Test Connection");
        FormData testServiceAccountButtonData = new FormData();
        testServiceAccountButtonData.top = new FormAttachment(0, margin);
        testServiceAccountButtonData.left = new FormAttachment(0, 0);
        testServiceAccountButton.setLayoutData(testServiceAccountButtonData);

        testServiceAccountInfo = new Label(serviceAccountComposite, SWT.LEFT);
        props.setLook(testServiceAccountInfo);
        FormData testServiceAccountInfoData = new FormData();
        testServiceAccountInfoData.top = new FormAttachment(0, margin);
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

        /*
         * BEGIN Fields Tab
         */
        CTabItem fieldsTab = new CTabItem(tabFolder, SWT.NONE);
        fieldsTab.setText("Fields");

        Composite fieldsComposite = new Composite(tabFolder, SWT.NONE);
        props.setLook(fieldsComposite);

        FormLayout fieldsLayout = new FormLayout();
        fieldsLayout.marginWidth = 3;
        fieldsLayout.marginHeight = 3;
        fieldsComposite.setLayout(fieldsLayout);

        wGet = new Button(fieldsComposite, SWT.PUSH);
        wGet.setText(BaseMessages.getString(PKG, "System.Button.GetFields"));

        // Fields
        ColumnInfo[] columnInformation = new ColumnInfo[]{
                new ColumnInfo("Name", ColumnInfo.COLUMN_TYPE_TEXT, false),
                new ColumnInfo("Type", ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMeta.getTypes(), true),
                new ColumnInfo("Format", ColumnInfo.COLUMN_TYPE_FORMAT, 2),
                new ColumnInfo("Length", ColumnInfo.COLUMN_TYPE_TEXT, false),
                new ColumnInfo("Precision", ColumnInfo.COLUMN_TYPE_TEXT, false),
                new ColumnInfo("Currency", ColumnInfo.COLUMN_TYPE_TEXT, false),
                new ColumnInfo("Decimal", ColumnInfo.COLUMN_TYPE_TEXT, false),
                new ColumnInfo("Group", ColumnInfo.COLUMN_TYPE_TEXT, false),
                new ColumnInfo("Trim type", ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMeta.trimTypeDesc),
        };

        columnInformation[2].setComboValuesSelectionListener(new ComboValuesSelectionListener() {

            public String[] getComboValues(TableItem tableItem, int rowNr, int colNr) {
                String[] comboValues = new String[]{};
                int type = ValueMeta.getType(tableItem.getText(colNr - 1));
                switch (type) {
                    case ValueMetaInterface.TYPE_DATE:
                        comboValues = Const.getDateFormats();
                        break;
                    case ValueMetaInterface.TYPE_INTEGER:
                    case ValueMetaInterface.TYPE_BIGNUMBER:
                    case ValueMetaInterface.TYPE_NUMBER:
                        comboValues = Const.getNumberFormats();
                        break;
                    default:
                        break;
                }
                return comboValues;
            }

        });

        wFields = new TableView(transMeta, fieldsComposite, SWT.FULL_SELECTION | SWT.MULTI, columnInformation, 1, modifiedListener, props);

        FormData fdFields = new FormData();
        fdFields.top = new FormAttachment(0, margin);
        fdFields.bottom = new FormAttachment(wGet, -margin * 2);
        fdFields.left = new FormAttachment(0, 0);
        fdFields.right = new FormAttachment(100, 0);
        wFields.setLayoutData(fdFields);
        wFields.setContentListener(contentListener);

        FormData fieldsCompositeData = new FormData();
        fieldsCompositeData.left = new FormAttachment(0, 0);
        fieldsCompositeData.top = new FormAttachment(0, 0);
        fieldsCompositeData.right = new FormAttachment(100, 0);
        fieldsCompositeData.bottom = new FormAttachment(100, 0);
        fieldsComposite.setLayoutData(fieldsCompositeData);

        setButtonPositions(new Button[]{wGet}, margin, null);

        fieldsComposite.layout();
        fieldsTab.setControl(fieldsComposite);
        /*
         * END Fields Tab
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
        lsGet = new Listener() {
            @Override
            public void handleEvent(Event e) {
                getSpreadsheetFields();
            }
        };

        wCancel.addListener(SWT.Selection, lsCancel);
        wOK.addListener(SWT.Selection, lsOK);
        wGet.addListener(SWT.Selection, lsGet);

        // default listener (for hitting "enter")
        lsDef = new SelectionAdapter() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                ok();
            }
        };
        wStepname.addSelectionListener(lsDef);


//testing connection to Google with API V4
        testServiceAccountButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                try {						
                    NetHttpTransport HTTP_TRANSPORT=GoogleNetHttpTransport.newTrustedTransport();
				    String APPLICATION_NAME = "pentaho-sheets";
                    JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
                    String TOKENS_DIRECTORY_PATH = Const.getKettleDirectory() +"/tokens";
					String scope=SheetsScopes.SPREADSHEETS_READONLY;
					Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, PentahoGoogleSheetsPluginCredentials.getCredentialsJson(scope)).setApplicationName(APPLICATION_NAME).build();
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
				    String APPLICATION_NAME = "pentaho-sheets";
                    JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
                    String TOKENS_DIRECTORY_PATH = Const.getKettleDirectory() +"/tokens";   
					String scope="https://www.googleapis.com/auth/drive.readonly";
					Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, PentahoGoogleSheetsPluginCredentials.getCredentialsJson(scope)).setApplicationName(APPLICATION_NAME).build();

                    FileList result = service.files().list().setQ("mimeType='application/vnd.google-apps.spreadsheet'").setPageSize(100).setFields("nextPageToken, files(id, name)").execute();
                    List<File> spreadsheets = result.getFiles();
                    int selectedSpreadsheet = -1;
                    int i=0;
					String[] titles=new String[spreadsheets.size()];
					for (File spreadsheet:spreadsheets) {
                        titles[i] = spreadsheet.getName()+" - "+spreadsheet.getId()+")";
						
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
                    esd.open();
                    if (esd.getSelectionIndeces().length > 0) {
                        selectedSpreadsheet = esd.getSelectionIndeces()[0];
                        File spreadsheet = spreadsheets.get(selectedSpreadsheet);
                        spreadsheetKey.setText(spreadsheet.getId());
                    } else {
                        spreadsheetKey.setText("");
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
				    String APPLICATION_NAME = "pentaho-sheets";
                    JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
                    String TOKENS_DIRECTORY_PATH = Const.getKettleDirectory() +"/tokens";
					String scope=SheetsScopes.SPREADSHEETS_READONLY;
					
					Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, PentahoGoogleSheetsPluginCredentials.getCredentialsJson(scope)).setApplicationName(APPLICATION_NAME).build();
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

                    EnterSelectionDialog esd = new EnterSelectionDialog(shell, names, "Worksheets",
                            "Select a Worksheet.");
                    if (selectedSheet > -1) {
                        esd.setSelectedNrs(new int[]{selectedSheet});
                    }
                    esd.open();

                    if (esd.getSelectionIndeces().length > 0) {
                        selectedSheet = esd.getSelectionIndeces()[0];                       
						Sheet sheet = worksheets.get(selectedSheet);
                        String id = sheet.getProperties().getTitle();
                        worksheetId.setText(id.substring(id.lastIndexOf("/") + 1));
                    } else {
                        worksheetId.setText("");
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

    private void getData(PentahoGoogleSheetsPluginInputMeta meta) {
        this.wStepname.selectAll();

  
        this.spreadsheetKey.setText(meta.getSpreadsheetKey());
		this.worksheetId.setText(meta.getWorksheetId());
		

        for ( int i = 0; i < meta.getInputFields().length; i++ ) {
		  TextFileInputField field = meta.getInputFields()[i];

		  TableItem item = new TableItem( wFields.table, SWT.NONE );

		  /*if ( insertAtTop ) {
			item = new TableItem( wFields.table, SWT.NONE, i );
		  } else {
			if ( i >= wFields.table.getItemCount() ) {
			  item = wFields.table.getItem( i );
			} else {
			  item = new TableItem( wFields.table, SWT.NONE );
			}
		  }*/

		  item.setText( 1, Const.NVL( field.getName(), "" ) );
		  String type = field.getTypeDesc();
		  String format = field.getFormat();
		  String position = "" + field.getPosition();
		  String length = "" + field.getLength();
		  String prec = "" + field.getPrecision();
		  String curr = field.getCurrencySymbol();
		  String group = field.getGroupSymbol();
		  String decim = field.getDecimalSymbol();
		  String trim = field.getTrimTypeDesc();
		 /* String rep =
			  field.isRepeated() ? BaseMessages.getString( PKG, "System.Combo.Yes" ) : BaseMessages.getString( PKG,
				  "System.Combo.No" );*/

		  if ( type != null ) {
			item.setText( 2, type );
		  }
		  if ( format != null ) {
			item.setText( 3, format );
		  }
		  if ( position != null && !"-1".equals( position ) ) {
			item.setText( 4, position );
		  }
		  if ( length != null && !"-1".equals( length ) ) {
			item.setText( 5, length );
		  }
		  if ( prec != null && !"-1".equals( prec ) ) {
			item.setText( 6, prec );
		  }
		  if ( curr != null ) {
			item.setText( 7, curr );
		  }
		  if ( decim != null ) {
			item.setText( 8, decim );
		  }
		  if ( group != null ) {
			item.setText( 9, group );
		  }
		  if ( trim != null ) {
			item.setText( 12, trim );
		  }

    }


        wFields.removeEmptyRows();
        wFields.setRowNums();
        wFields.optWidth(true);
		
		meta.setChanged();

    }

    private void setData(PentahoGoogleSheetsPluginInputMeta meta) {
  
        meta.setSpreadsheetKey(this.spreadsheetKey.getText());
        meta.setWorksheetId(this.worksheetId.getText());

        int nrNonEmptyFields = wFields.nrNonEmpty();
        meta.allocate(nrNonEmptyFields);

        for (int i = 0; i < nrNonEmptyFields; i++) {
            TableItem item = wFields.getNonEmpty(i);
            meta.getInputFields()[i] = new TextFileInputField();

            int colnr = 1;
            meta.getInputFields()[i].setName(item.getText(colnr++));
            meta.getInputFields()[i].setType(ValueMeta.getType(item.getText(colnr++)));
            meta.getInputFields()[i].setFormat(item.getText(colnr++));
            meta.getInputFields()[i].setLength(Const.toInt(item.getText(colnr++), -1));
            meta.getInputFields()[i].setPrecision(Const.toInt(item.getText(colnr++), -1));
            meta.getInputFields()[i].setCurrencySymbol(item.getText(colnr++));
            meta.getInputFields()[i].setDecimalSymbol(item.getText(colnr++));
            meta.getInputFields()[i].setGroupSymbol(item.getText(colnr++));
            meta.getInputFields()[i].setTrimType(ValueMeta.getTrimTypeByDesc(item.getText(colnr++)));
        }
        wFields.removeEmptyRows();
        wFields.setRowNums();
        wFields.optWidth(true);

        meta.setChanged();
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

    private void getSpreadsheetFields() {
        try {
            PentahoGoogleSheetsPluginInputMeta meta = new PentahoGoogleSheetsPluginInputMeta();
            setData(meta);
            NetHttpTransport HTTP_TRANSPORT=GoogleNetHttpTransport.newTrustedTransport();
			String APPLICATION_NAME = "pentaho-sheets";
            JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
            String TOKENS_DIRECTORY_PATH = "tokens";
			String scope=SheetsScopes.SPREADSHEETS_READONLY;
            wFields.table.removeAll();
			
			Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, PentahoGoogleSheetsPluginCredentials.getCredentialsJson(scope)).setApplicationName(APPLICATION_NAME).build();
			String range=transMeta.environmentSubstitute(meta.getWorksheetId())+"!"+"1:1";
			ValueRange result = service.spreadsheets().values().get(transMeta.environmentSubstitute(meta.getSpreadsheetKey()), range).execute();            
			List<List<Object>> values = result.getValues();
            if (values != null || !values.isEmpty()) {
			 for (List row : values) {
				 for(int j=0;j<row.size();j++)
				 {
				 TableItem item = new TableItem(wFields.table, SWT.NONE);
				 item.setText(1, Const.trim(row.get(j).toString()));
                 item.setText(2, ValueMeta.getTypeDesc(ValueMetaInterface.TYPE_STRING));
				 }
			 }
			}
          
            wFields.removeEmptyRows();
            wFields.setRowNums();
            wFields.optWidth(true);
        } catch (Exception e) {
            new ErrorDialog(shell, BaseMessages.getString(PKG, "System.Dialog.Error.Title"), "Error getting Fields", e);
        }
    }

    private String getPrivateKeyClientID(KeyStore keyStore) {
        try {
            X509Certificate cert = (X509Certificate) keyStore.getCertificate("privatekey");
            String name = cert.getIssuerX500Principal().getName(X500Principal.RFC2253);
            return name.replaceAll("CN=", "");
        } catch (Exception e) {

        }
        return "";
    }

}
