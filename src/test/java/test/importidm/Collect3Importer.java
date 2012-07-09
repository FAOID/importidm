package test.importidm;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectRecord.Step;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.User;
import org.openforis.collect.persistence.RecordDao;
import org.openforis.collect.persistence.SurveyDao;
import org.openforis.collect.persistence.UserDao;
import org.openforis.collect.persistence.xml.DataHandler;
import org.openforis.collect.persistence.xml.DataUnmarshaller;
import org.openforis.collect.persistence.xml.DataUnmarshallerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

/**
 * 
 * @author S. Ricci
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/ImportIdm-context.xml" })
@TransactionConfiguration(defaultRollback = false)
public class Collect3Importer {
	@Autowired
	private RecordManager recordManager;
	@Autowired
	private RecordDao recordDao;
	@Autowired
	private SurveyDao surveyDao;
	@Autowired
	private UserDao userDao;

	private Map<String, User> users;

	protected void init() {
		initUsers();
	}

	protected void initUsers() {
		List<User> usersList = userDao.loadAll();
		users = new HashMap<String, User>();
		for (User user : usersList) {
			users.put(user.getName(), user);
		}
	}

	public CollectSurvey loadSurvey(String name) throws Exception {
		CollectSurvey survey = surveyDao.load(name);
		return survey;
	}


	public void replaceAll(CollectSurvey survey, String zipPath) throws IOException {
		long start = System.currentTimeMillis();
		long imported = 0;
		long totalRecords = 0;
		long skipped = 0;
		long warnings = 0;
		DataHandler handler = new DataHandler(survey, users);
		DataUnmarshaller dataUnmarshaller = new DataUnmarshaller(handler);

		ZipFile zipFile = new ZipFile(zipPath);
		Enumeration<? extends ZipEntry> entries = zipFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry zipEntry = (ZipEntry) entries.nextElement();
			if ( zipEntry.isDirectory() ) {
				continue;
			}
			String entryName = zipEntry.getName();
			String filePathSeparator = Pattern.quote("/");// Pattern.quote("/");
			String[] entryNameSplitted = entryName.split(filePathSeparator);
			String stepNumStr = entryNameSplitted[0];
			int stepNumber = Integer.parseInt(stepNumStr);
			String fileName = entryNameSplitted[1];
			String recordIdStr = fileName.split(Pattern.quote("."))[0];
			int recordId = Integer.parseInt(recordIdStr);
			System.out.println("Extracting: " + recordIdStr + " (" + entryName + ")");
			InputStream inputStream = zipFile.getInputStream(zipEntry);
			InputStreamReader reader = new InputStreamReader(inputStream);
			Step step = Step.valueOf(stepNumber);
			ParseRecordResult parseRecordResult = parseRecord(dataUnmarshaller, reader);
			System.out.println(parseRecordResult);
			CollectRecord parsedRecord = parseRecordResult.record;
			
			//long tload = parseRecordResult.tload;
			String message = parseRecordResult.message;
			//long tsave = -1;
			if ( parsedRecord == null ) {
				skipped++;
			} else {
				//long savestart = System.currentTimeMillis();
				parsedRecord.setStep(step);
				CollectRecord record = new CollectRecord(survey, parsedRecord.getVersion().getName());
				record.setId(recordId);
				replaceData(parsedRecord, record);
				recordDao.update(record);
				totalRecords ++;
				System.out.println("Updated record: " + recordId + " (" + record.getRootEntityKeyValues() + ") step: " + step);
				//tsave = System.currentTimeMillis() - savestart;
				imported++;
				warnings += parseRecordResult.warnings;
			}
			if ( ! message.equals("Ok") ) {
				List<String> rootEntityKeyValues = parsedRecord.getRootEntityKeyValues();
				System.out.println(rootEntityKeyValues+","+stepNumber+","+message);
			}
		}
		zipFile.close();
		long end = System.currentTimeMillis();
		long duration = end-start;
		System.out.println("Loaded "+imported+" records in "+(duration/1000)+"s ("+(duration/imported)+"ms/record). " +
				"\nTotal imported records: " + totalRecords +
				"\nWarnings:\t" + warnings +
				"\nSkipped:\t" + skipped);
	}

	private ParseRecordResult parseRecord(DataUnmarshaller dataUnmarshaller, Reader reader) throws IOException {
		ParseRecordResult result = new ParseRecordResult();
		result.message = "Ok";
		System.out.println("parseRecord");
		try {
			//long loadstart = System.currentTimeMillis();
			CollectRecord record = dataUnmarshaller.parse(reader);
			System.out.println("parseRecord2");
			recordManager.addEmptyNodes(record.getRootEntity());
			//record.updateDerivedStates();
			result.record = record;
			//result.tload = System.currentTimeMillis() - loadstart;
			List<String> warns = dataUnmarshaller.getLastParsingWarnings();
			if (warns.size() > 0) {
				result.message = "Processed with errors: " + warns.toString();
				result.warnings = warns.size();
			}
		} catch (DataUnmarshallerException e) {
			result.message = "Unable to process1: " + e.getMessages().toString();
		} catch (RuntimeException e) {
			result.message = "Unable to process2: " + e.toString();
		}
		System.out.println("parseRecord3 " + result.message);
		return result;
	}

	private void replaceData(CollectRecord fromRecord, CollectRecord toRecord) {
		toRecord.setCreatedBy(fromRecord.getCreatedBy());
		toRecord.setCreationDate(fromRecord.getCreationDate());
		toRecord.setModifiedBy(fromRecord.getModifiedBy());
		toRecord.setModifiedDate(fromRecord.getModifiedDate());
		toRecord.setStep(fromRecord.getStep());
		toRecord.setState(fromRecord.getState());
		toRecord.setRootEntity(fromRecord.getRootEntity());
		toRecord.updateRootEntityKeyValues();
		toRecord.updateEntityCounts();
	}

	public String[] getClusterIds(String dataPath, int phase) {
		File dataEntryDataPath = new File(dataPath + phase);
		String[] clusterIds = dataEntryDataPath.list();
		return clusterIds;
	}

	@Test
	public void testImport() throws Exception {
		String surveyName = "idnfi";
		String zipPath = "C:\\Users\\User\\Downloads\\data-error-2.zip";

		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("ImportIdm-context.xml");
		Collect3Importer naformaMigrator = ctx.getBean(Collect3Importer.class);

		// Import IDML
		CollectSurvey survey = naformaMigrator.loadSurvey(surveyName);

		naformaMigrator.replaceAll(survey, zipPath);
	}

	private class ParseRecordResult {

		private String message;
		//private long tload;
		private int warnings;
		private CollectRecord record;

	}
}

