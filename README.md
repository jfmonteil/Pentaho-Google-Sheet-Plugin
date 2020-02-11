
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
You should generate your client secret file (json) for your **Google service account** and paste it somewhere on your machine (or server).
Google Tokens will also be create in *.kettle* directory */tokens*
The service account should be parameterized with both Google Drive and Google Spreadsheet  API’s read and write access

## Input step
### Credential Tab
Lets you pick your google service account client secret json file.
Test button Lets you test your access to the API.
On success you should see a success message

### Spreadsheet tab
Let’s you specify or browse for spreadsheets existing in the service account drive or for the ones that are shared with the service account email.

### Fields tab
Lets you select from the fields of the sheet.
Fields name are always defined in the first line of the google spreadsheet.
Note that they are all types as String for the moment.
Input fields are mandatory.

## Output step
Lets you write data into a sheet (existing or not)

### Credential Tab
Lets you pick your google service account client secret json file.
Lets  you test your access to the APIOn success you should see the following screen

### Spreadsheet tab

* Spreadsheet key : 
Lets you specify or browse for spreadsheets existing in the service account drive or for the ones that are shared with the service account email.
if you type in a sheet name (that does not exist in the drive) it will attempt to create a sheet it the "create" checkbox is ticked.
* Worksheet Id : Should be browsed form the selected spreadsheet key. If you want to create a new file, type in any key that will become the name of the worksheet in the created spreadsheet

* Create sheet if it does not exist checkbox : 
If the checkbox is checked then if the Spreadsheet Key spécified in the field Spreadsheet key does not exist it will create a new spreadsheet within the service account drive (note that this account has no UI)

* Share with email : 
That is why the Share with user email field let’s you specify the email of a user who will get full rights on the freshly created file.

* Domain Wise permission :
If configured for your company Google Drive, it allows to share the file with a whole domain e.g. * hitachivantara.com * (Read only for the moment)

All steps inbound fields are written in the output file

## Metadata injection.
Both steps fully support metadata injection
See mi-input-output transformations in the sample repository.

 



> Written with [StackEdit](https://stackedit.io/).