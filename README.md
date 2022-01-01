
# Pentaho Google Spreadsheet Plugin (Google Sheet API V4 / Google Drive API V3)

Jean-François Monteil
jfmonteil@gmail.com

As the google Spreadsheet API V3 comes close to depreciation (march 2020) I have developed this plugin using Google Spreadsheet API V4.

It contains 2 steps :
* Pentaho Google Sheets Input : Reads specified fields a google spreadsheet
* Pentaho Google Sheets Output : Writes a google spreadsheet

## Installation
In *delivery rep* you will find a zip that you can unzip in your *pentaho/design-tools/data-integration/plugin* folder.
Otherwise :  ``` mvn install ```
You should generate your client secret file (json) for your **Google service account** and paste it somewhere on your machine (or server). **It might be ,ecessary to regenerate it, because Google brought some change in 2020**
Google Tokens will also be create in *.kettle* directory */tokens*
The service account should be parameterized with both Google Drive and Google Spreadsheet  API’s read and write access

## Input step
### Service Account Tab
Lets you pick your google service account client secret json file. **It might be ,ecessary to regenerate it, because Google brought some change in 2020**
Application Name : Your application name for the service account in the Google Developer Console.
Timeout lets you specify an HTTP tiemout
Impersonation lets you impersonate your service account **ONLY if you have a GSUITE account** you need to gor through these steps as well : https://cloud.google.com/iam/docs/impersonating-service-accounts
Test button Lets you test your access to the API.
On success you should see a success message.
![Service Account](https://github.com/jfmonteil/Pentaho-Google-Sheet-Plugin/blob/master/screenshots/PentahoGoogleSheetInput-Credential.png?raw=true)


### Spreadsheet tab
Let’s you specify or browse for spreadsheets existing in the service account drive or for the ones that are shared with the service account email.
![enter image description here](https://raw.githubusercontent.com/jfmonteil/Pentaho-Google-Sheet-Plugin/master/screenshots/PentahoGoogleSheetInput-Spreadsheet.png)


### Fields tab
Lets you select from the fields of the sheet.
Fields name are always defined in the first line of the google spreadsheet.
* Get Fields* lets you get fields and guess their types, format, precision, decimal and group delimiter as well as trim type.
![enter image description here](https://raw.githubusercontent.com/jfmonteil/Pentaho-Google-Sheet-Plugin/master/screenshots/PentahoGoogleSheetInput-Fields.png)

## Output step
Lets you write data into a sheet (existing or not)
**IMPORTANT NOTE** : The API writes data in a "user entered mode" as if you were typing in the cells. That means the cells are interpreted by google after insert. I noticed that it is better to transmit STRINGS to the step so Google Sheets works well, specially numbers. Use *Select Value* step for this

### Service Account Tab
Lets you pick your google service account client secret json file.
Application Name : Your application name for the service account in the Google Developer Console.
Timeout lets you specify an HTTP tiemout
Impersonation lets you impersonate your service account **ONLY if you have a GSUITE account** you need to gor through these steps as well : https://cloud.google.com/iam/docs/impersonating-service-accounts

Lets  you test your access to the API. On success you should see the following screen

### Spreadsheet tab

* Spreadsheet key : 
Lets you specify or browse for spreadsheets existing in the service account drive or for the ones that are shared with the service account email.
If you type in a sheet name (that does not exist in the drive) it will attempt to create a sheet it the "create" checkbox is ticked.
* Worksheet Id : Should be browsed form the selected spreadsheet key. If you want to create a new file, type in any key that will become the name of the worksheet in the created spreadsheet

* Append checkbox : 
Appends the lines **without the header** to an existing spreadsheet. This is incompatible with the *create* option below.

* Create sheet if it does not exist checkbox : 
If the checkbox is checked then if the Spreadsheet Key specified in the field Spreadsheet key does not exist it will create a new spreadsheet within the service account drive (note that this account has no UI)

* Share with email : 
That is why the Share with user email field let’s you specify the email of a user who will get full rights on the freshly created file.

* Domain Wise Permission : 
Enables to share with your whole domain (if configured in Google Drive). For security concerns I share only with READ rights.


![enter image description here](https://raw.githubusercontent.com/jfmonteil/Pentaho-Google-Sheet-Plugin/master/screenshots/PentahoGoogleSheetOut-Spreadsheet.png)

All steps inbound fields are written in the output file

## Metadata injection.
Both steps fully support *metadata injection* and *parameters*
See mi-input-output transformations in the sample repository.
![enter image description here](https://raw.githubusercontent.com/jfmonteil/Pentaho-Google-Sheet-Plugin/master/screenshots/PentahoGoogleSheetOut-Spreadsheet_Variable.png)


> Written with [StackEdit](https://stackedit.io/).