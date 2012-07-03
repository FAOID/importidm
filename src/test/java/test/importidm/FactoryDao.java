package test.importidm;

import org.openforis.collect.persistence.jooq.DialectAwareJooqFactory;
import org.openforis.collect.persistence.jooq.JooqDaoSupport;



public class FactoryDao extends JooqDaoSupport {

	@Override
	public DialectAwareJooqFactory getJooqFactory() {
		return super.getJooqFactory();
	}
}