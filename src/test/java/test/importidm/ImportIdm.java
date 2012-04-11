package test.importidm;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openforis.collect.model.editor.IdmUpdater;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.idm.metamodel.xml.InvalidIdmlException;
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
	protected IdmUpdater idmUpdater;
	

	/* @Test */
	public void testImportIdnfi() throws Exception {
		idmUpdater.importIdnfi("idnfi", ClassLoader.getSystemResource("idnfi.idm.xml"));
	}


	@Test
	public void testUpdateIdnfi() throws IOException, InvalidIdmlException, SurveyImportException {
		idmUpdater.updateModel("idnfi",ClassLoader.getSystemResource("MOFOR_WORKING_update.idnfi.idm.xml"));
	}
	
	@Test
	public void testUpdateGreenbook() throws IOException, InvalidIdmlException, SurveyImportException {
		idmUpdater.updateModel("greenbook",ClassLoader.getSystemResource("MOFOR_WORKING_update.greenbook.idm.xml"));
	}
	
}
