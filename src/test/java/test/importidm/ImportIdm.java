package test.importidm;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.editor.IdmEditor;
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
	protected IdmEditor idmEditor;
	

	/* @Test */
	public void testImportIdnfi() throws Exception {
		idmEditor.importIdnfi();
	}


	@Test
	public void testUpdateIdnfi() throws IOException, InvalidIdmlException, SurveyImportException {
		idmEditor.updateModel();
	}
}
