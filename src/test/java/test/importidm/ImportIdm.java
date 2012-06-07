package test.importidm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openforis.collect.manager.LogoManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.Logo;
import org.openforis.collect.persistence.LogoDao;
import org.openforis.collect.persistence.SurveyDao;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.collect.persistence.jooq.DialectAwareJooqFactory;
import org.openforis.collect.persistence.xml.CollectIdmlBindingContext;
import org.openforis.idm.metamodel.xml.InvalidIdmlException;
import org.openforis.idm.metamodel.xml.SurveyUnmarshaller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;
import static org.openforis.collect.persistence.jooq.tables.OfcLogo.OFC_LOGO;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/ImportIdm-context.xml" })
@TransactionConfiguration(defaultRollback = false)
/**
 * @author Wibowo, Eko
 */
@Transactional
public class ImportIdm {

	@Autowired
	protected SurveyDao surveyDao;

	@Autowired
	protected LogoManager logoManager;
	
	/*
	 * disabled. UpdateModel has all the functionality of importModel, with
	 * added feature of updating database structure into the newest IDM I'll
	 * keep the method here for historical purpose only public void
	 * testImportIdnfi() throws Exception { idmUpdater.importIdnfi("idnfi",
	 * ClassLoader.getSystemResource("idnfi.idm.xml")); }
	 */

	@Test
	public void updateIdnfiIdm() throws IOException, InvalidIdmlException, SurveyImportException {
	
		InputStream is = ClassLoader.getSystemResource("MOFOR_WORKING.idnfi.idm.xml").openStream();
		CollectIdmlBindingContext idmlBindingContext = surveyDao.getBindingContext();
		SurveyUnmarshaller surveyUnmarshaller = idmlBindingContext.createSurveyUnmarshaller();
		CollectSurvey survey = (CollectSurvey) surveyUnmarshaller.unmarshal(is);
		survey.setName("idnfi");
		survey.setUri("http://www.openforis.org/idm/idnfi");
		surveyDao.updateModel(survey);
	}

	//@Test
	public void updateGreenbookIdm() throws IOException, InvalidIdmlException,
			SurveyImportException {
		InputStream is = ClassLoader.getSystemResource("MOFOR_WORKING.greenbook.idm.xml").openStream();
		CollectIdmlBindingContext idmlBindingContext = surveyDao.getBindingContext();
		SurveyUnmarshaller surveyUnmarshaller = idmlBindingContext.createSurveyUnmarshaller();
		CollectSurvey survey = (CollectSurvey) surveyUnmarshaller.unmarshal(is);
		survey.setName("greenbook");
		survey.setUri("http://www.openforis.org/idm/greenbook");
		surveyDao.updateModel(survey);
	}
	
	//@Test
	public void storeLogo() throws IOException
	{
		byte[] image = null;
		Logo logo = new Logo();
		logo.setPosition(0);
		URL imgFile = ClassLoader.getSystemResource("logo/specific_logo.jpg");
		File file = new File(imgFile.getPath());		
		image = getBytesFromFile(file);
		logo.setImage(image);		
		logoManager.storeLogo(logo);
	}
	
	// borrowed from http://www.exampledepot.com/egs/java.io/file2bytearray.html
	public static byte[] getBytesFromFile(File file) throws IOException {
	    InputStream is = new FileInputStream(file);

	    // Get the size of the file
	    long length = file.length();

	    // You cannot create an array using a long type.
	    // It needs to be an int type.
	    // Before converting to an int type, check
	    // to ensure that file is not larger than Integer.MAX_VALUE.
	    if (length > Integer.MAX_VALUE) {
	        // File is too large
	    }

	    // Create the byte array to hold the data
	    byte[] bytes = new byte[(int)length];

	    // Read in the bytes
	    int offset = 0;
	    int numRead = 0;
	    while (offset < bytes.length
	           && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
	        offset += numRead;
	    }

	    // Ensure all the bytes have been read in
	    if (offset < bytes.length) {
	        throw new IOException("Could not completely read file "+file.getName());
	    }

	    // Close the input stream and return bytes
	    is.close();
	    return bytes;
	}


}
