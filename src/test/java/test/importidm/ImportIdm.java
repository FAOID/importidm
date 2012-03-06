package test.importidm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;

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
		File idmFile = new File(
				"E:\\work\\importidm\\src\\main\\resources\\simple.idm.xml");
		FileInputStream is = new FileInputStream(idmFile);
		CollectIdmlBindingContext idmlBindingContext = new CollectIdmlBindingContext(new CollectSurveyContext(expressionFactory, validator));
		SurveyUnmarshaller surveyUnmarshaller = idmlBindingContext
				.createSurveyUnmarshaller();
		Survey survey = surveyUnmarshaller.unmarshal(is);
		//surveyDao.clearModel();
		survey.setName("idnfi");
		surveyDao.importModel(survey);
		return survey;
	}

}
