package test.importidm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectSurveyContext;
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

	@Test
	public void testImportSurvey() throws Exception {
		importModel();
	}

	private Survey importModel() throws IOException, SurveyImportException,
			InvalidIdmlException {
		URL idm = ClassLoader.getSystemResource("updated.candidate.idnfi.idm.xml");
		InputStream is = idm.openStream();
		CollectSurveyContext surveyContext = new CollectSurveyContext(expressionFactory, validator);
		CollectIdmlBindingContext idmlBindingContext = new CollectIdmlBindingContext(surveyContext);
		SurveyUnmarshaller surveyUnmarshaller = idmlBindingContext.createSurveyUnmarshaller();
		CollectSurvey survey = (CollectSurvey) surveyUnmarshaller.unmarshal(is);
		survey.setName("idnfi");
		surveyDao.clearModel();
		surveyDao.importModel(survey);
		return survey;
	}

}
