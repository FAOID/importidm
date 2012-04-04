package test.importidm;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.RecordDao;
import org.openforis.collect.persistence.SurveyDao;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.collect.persistence.xml.CollectIdmlBindingContext;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.validation.Validator;
import org.openforis.idm.metamodel.xml.InvalidIdmlException;
import org.openforis.idm.metamodel.xml.SurveyUnmarshaller;
import org.openforis.idm.model.expression.ExpressionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/ImportIdm-context.xml"})
@TransactionConfiguration(defaultRollback = false)
@Transactional
public class ImportIdm {

	@Autowired
	protected SurveyDao surveyDao;

	@Autowired
	protected RecordDao recordDao;
	@Autowired
	protected ExpressionFactory expressionFactory;
	@Autowired
	protected Validator validator;

	/* @Test */
	public void testImportSurvey() throws Exception {
		importModel();
	}

	/*
	 * This will completely erase the database
	 */
	private Survey importModel() throws IOException, SurveyImportException,
			InvalidIdmlException {
		URL idm = ClassLoader.getSystemResource("idnfi.idm.xml");
		InputStream is = idm.openStream();
		CollectIdmlBindingContext idmlBindingContext = surveyDao.getBindingContext();
		SurveyUnmarshaller surveyUnmarshaller = idmlBindingContext.createSurveyUnmarshaller();
		CollectSurvey survey = (CollectSurvey) surveyUnmarshaller.unmarshal(is);
		survey.setName("idnfi");
		surveyDao.clearModel();
		surveyDao.importModel(survey);
		return survey;
	}
	
	@Test
	public void testUpdateSurvey() throws IOException, InvalidIdmlException, SurveyImportException {
		updateModel();
	}

	/*
	 * Eko 14:19 03/04/2012 Allow adding of new fields only. Entity renaming not supported. Deletion not yet researched
	 */
	private Survey updateModel() throws IOException, InvalidIdmlException, SurveyImportException {
		
		URL idm = ClassLoader.getSystemResource("MOFOR_WORKING_update.idnfi.idm.xml");//MOFOR_2012_04_03_update.idnfi.idm.xml
		InputStream is = idm.openStream();
		CollectIdmlBindingContext idmlBindingContext = surveyDao.getBindingContext();
		SurveyUnmarshaller surveyUnmarshaller = idmlBindingContext.createSurveyUnmarshaller();
		CollectSurvey survey = (CollectSurvey) surveyUnmarshaller.unmarshal(is);
		survey.setName("idnfi");
		surveyDao.updateModel(survey);
		return survey;
	}

}
