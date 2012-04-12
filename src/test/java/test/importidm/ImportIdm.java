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
@ContextConfiguration(locations = { "/ImportIdm-context.xml" })
@TransactionConfiguration(defaultRollback = false)
/**
 * @author Wibowo, Eko
 */
@Transactional
public class ImportIdm {

	@Autowired
	protected IdmUpdater idmUpdater;

	/*
	 * disabled. UpdateModel has all the functionality of importModel, with
	 * added feature of updating database structure into the newest IDM I'll
	 * keep the method here for historical purpose only public void
	 * testImportIdnfi() throws Exception { idmUpdater.importIdnfi("idnfi",
	 * ClassLoader.getSystemResource("idnfi.idm.xml")); }
	 */

	@Test
	public void updateIdnfiIdm() throws IOException, InvalidIdmlException,
			SurveyImportException {
		idmUpdater.updateModel("idnfi",
				ClassLoader.getSystemResource("MOFOR_WORKING.idnfi.idm.xml"));
	}

	//@Test
	public void updateGreenbookIdm() throws IOException, InvalidIdmlException,
			SurveyImportException {
		idmUpdater.updateModel("greenbook", ClassLoader
				.getSystemResource("MOFOR_WORKING.greenbook.idm.xml"));
	}

}
