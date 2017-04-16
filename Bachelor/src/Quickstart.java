

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.Sheets.Spreadsheets.Create;
import com.google.api.services.sheets.v4.Sheets.Spreadsheets.Values.BatchUpdate;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetResponse;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesResponse;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.api.services.sheets.v4.model.UpdateSpreadsheetPropertiesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;

import google.api.GoogleAuthApp;
import google.api.GoogleVerificationCodeReceiver;

public class Quickstart {
	/** Application name. */
	private static final String APPLICATION_NAME =
			"Google Sheets API Java Quickstart";

	/** Directory to store user credentials for this application. */
	private static final java.io.File DATA_STORE_DIR = new java.io.File(
			System.getProperty("user.home"), ".credentials/sheets.googleapis.com-java-quickstart");

	/** Global instance of the {@link FileDataStoreFactory}. */
	private static FileDataStoreFactory DATA_STORE_FACTORY;

	/** Global instance of the JSON factory. */
	private static final JsonFactory JSON_FACTORY =
			JacksonFactory.getDefaultInstance();

	/** Global instance of the HTTP transport. */
	private static HttpTransport HTTP_TRANSPORT;

	/** Global instance of the scopes required by this quickstart.
	 *
	 * If modifying these scopes, delete your previously saved credentials
	 * at ~/.credentials/sheets.googleapis.com-java-quickstart
	 */
	private static final List<String> SCOPES =
			Arrays.asList(SheetsScopes.SPREADSHEETS, SheetsScopes.DRIVE);

	static {
		try {
			HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
		} catch (Throwable t) {
			t.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Creates an authorized Credential object.
	 * @return an authorized Credential object.
	 * @throws IOException
	 */
	public static Credential authorize() throws IOException {
		// Load client secrets.
		System.out.println(JSON_FACTORY);
		InputStream in =
				Quickstart.class.getResourceAsStream("/client_secret_ec2.json");
		System.out.println(in);
		GoogleClientSecrets clientSecrets =
				GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow =
				new GoogleAuthorizationCodeFlow.Builder(
						HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
				.setDataStoreFactory(DATA_STORE_FACTORY)
				.setAccessType("offline")
				.build();
		VerificationCodeReceiver receiver = new GoogleVerificationCodeReceiver("localhost", 5151);
		
		GoogleAuthApp authApp = new GoogleAuthApp(
				flow, receiver);
		authApp.setListener(new UrlListener());
		Credential credential = authApp.authorize("user");
		System.out.println(
				"Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
		return credential;
	}

	/**
	 * Build and return an authorized Sheets API client service.
	 * @return an authorized Sheets API client service
	 * @throws IOException
	 */
	public static Sheets getSheetsService() throws IOException {
//		GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream("client_secret.json"))
//			    .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS_READONLY));
		Credential credential = authorize();
		return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
				.setApplicationName(APPLICATION_NAME)
				.build();
	}
	
	private static Drive getDriveService() throws IOException {
		Credential credential = authorize();
		Drive drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();
		return drive;
	}
	

	public static void main(String[] args) throws IOException {
		// Build a new authorized API client service.
		
		Sheets service = getSheetsService();
		Drive drive = getDriveService();
		// Prints the names and majors of students in a sample spreadsheet:
		// https://docs.google.com/spreadsheets/d/1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms/edit
		Spreadsheet newSheet = createSheet(service);
		newSheet.getProperties().setTitle("F1702324");
		Request TitleupdateReq = createTitleRequest("Test");

		
		List<Request> reqList = new ArrayList<>();
		reqList.add(TitleupdateReq);
		
		BatchUpdateSpreadsheetRequest batchUpdateSpreadsheetRequest = new BatchUpdateSpreadsheetRequest();
		batchUpdateSpreadsheetRequest.setRequests(reqList);
		batchUpdateSpreadsheetRequest.setIncludeSpreadsheetInResponse(true);
		 BatchUpdateSpreadsheetResponse response = service.spreadsheets().batchUpdate(newSheet.getSpreadsheetId(), batchUpdateSpreadsheetRequest).execute();
		
		Spreadsheet sheet = response.getUpdatedSpreadsheet();
		List<List<Object>> values = new ArrayList<>();
		List<Object> inner = new ArrayList<>();
		inner.add("Test1");
		inner.add("Test2");
		values.add(inner);
		System.out.println(sheet.toPrettyString());
		
		BatchUpdateValuesRequest batchUpdateValuesRequest = createCellUpdateRequest("A1:B1", values);
		BatchUpdate batchUpdate = service.spreadsheets().values().batchUpdate(newSheet.getSpreadsheetId(), batchUpdateValuesRequest);
		batchUpdateValuesRequest.setIncludeValuesInResponse(true);
		BatchUpdateValuesResponse batchResponse = batchUpdate.execute();
		System.out.println(batchResponse.getResponses().get(0).getUpdatedData().toPrettyString());
		
		drive.files().update(newSheet.getSpreadsheetId(), null ).setAddParents("0B9dp65nRHm0raVVoWWRRa1dlVmc")
		.setFields("*")
		.execute();
//		getTestSheet(service);
	
	}
	


	private static BatchUpdateValuesRequest createCellUpdateRequest(String range, List<List<Object>> values) {
		BatchUpdateValuesRequest req = new BatchUpdateValuesRequest();
		req.setValueInputOption("RAW");
		List<ValueRange> data = new ArrayList<>();
		data.add(new ValueRange().setRange(range).setValues(values));
		req.setData(data );
		return req;
	}

	private static Request createTitleRequest(String title){
		Request TitleupdateReq = new Request();
		UpdateSpreadsheetPropertiesRequest updateSpreadsheetProperties = new UpdateSpreadsheetPropertiesRequest();
		updateSpreadsheetProperties.setFields("*");
		updateSpreadsheetProperties.setProperties(new SpreadsheetProperties().setTitle("Test"));
		TitleupdateReq.setUpdateSpreadsheetProperties(updateSpreadsheetProperties );
		
		return TitleupdateReq;
	}

	private static Spreadsheet createSheet(Sheets service) {
		try {
			Create create = service.spreadsheets().create(new Spreadsheet());
			Spreadsheet newSheet = create.execute();
			System.out.println(newSheet.getSpreadsheetId());
			return newSheet;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}

	private static void getTestSheet(Sheets service) throws IOException {
		String spreadsheetId = "1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms";
		String range = "Class Data!A2:E";
		ValueRange response = service.spreadsheets().values()
				.get(spreadsheetId, range)
				.execute();
		List<List<Object>> values = response.getValues();
		if (values == null || values.size() == 0) {
			System.out.println("No data found.");
		} else {
			System.out.println("Name, Major");
			for (List row : values) {
				// Print columns A and E, which correspond to indices 0 and 4.
				System.out.printf("%s, %s\n", row.get(0), row.get(4));
			}
		}
	}


}