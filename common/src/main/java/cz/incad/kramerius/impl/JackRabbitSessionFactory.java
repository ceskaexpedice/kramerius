package cz.incad.kramerius.impl;

import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class JackRabbitSessionFactory extends BasePooledObjectFactory<Session>{

    private Repository repo;

	public JackRabbitSessionFactory(Repository repo) {
		super();
		this.repo = repo;
	}

	@Override
	public Session create() throws Exception {
    	return this.repo.login(new SimpleCredentials("admin", "admin".toCharArray()));
	}

	@Override
	public PooledObject<Session> wrap(Session obj) {
		return new DefaultPooledObject<Session>(obj);
	}

	
}
