package org.eclipse.rdf4j.spring;

import java.lang.invoke.MethodHandles;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.spring.operationcache.CachingOperationInstantiator;
import org.eclipse.rdf4j.spring.operationcache.OperationCacheProperties;
import org.eclipse.rdf4j.spring.operationlog.LoggingRepositoryConnectionFactory;
import org.eclipse.rdf4j.spring.operationlog.log.OperationLog;
import org.eclipse.rdf4j.spring.pool.PoolProperties;
import org.eclipse.rdf4j.spring.pool.PooledRepositoryConnectionFactory;
import org.eclipse.rdf4j.spring.resultcache.CachingRepositoryConnectionFactory;
import org.eclipse.rdf4j.spring.resultcache.ResultCacheProperties;
import org.eclipse.rdf4j.spring.support.DirectOperationInstantiator;
import org.eclipse.rdf4j.spring.support.OperationInstantiator;
import org.eclipse.rdf4j.spring.support.Rdf4JTemplate;
import org.eclipse.rdf4j.spring.support.connectionfactory.DirectRepositoryConnectionFactory;
import org.eclipse.rdf4j.spring.support.connectionfactory.RepositoryConnectionFactory;
import org.eclipse.rdf4j.spring.tx.TransactionalRepositoryConnectionFactory;
import org.eclipse.rdf4j.spring.tx.TxProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class Rdf4JConfig {
	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Bean
	Rdf4JTemplate getRdf4JTemplate(
			@Autowired RepositoryConnectionFactory repositoryConnectionFactory,
			@Autowired(required = false) OperationCacheProperties operationCacheProperties) {
		OperationInstantiator operationInstantiator;
		if (operationCacheProperties != null && operationCacheProperties.isEnabled()) {
			operationInstantiator = new CachingOperationInstantiator();
		} else {
			operationInstantiator = new DirectOperationInstantiator();
		}
		return new Rdf4JTemplate(repositoryConnectionFactory, operationInstantiator);
	}

	@Bean
	RepositoryConnectionFactory getRepositoryConnectionFactory(
			@Autowired Repository repository,
			@Autowired(required = false) PoolProperties poolProperties,
			@Autowired(required = false) ResultCacheProperties resultCacheProperties,
			@Autowired(required = false) OperationLog operationLog,
			@Autowired(required = false) TxProperties txProperties) {
		RepositoryConnectionFactory factory = getDirectRepositoryConnectionFactory(repository);
		if (poolProperties != null) {
			factory = wrapWithPooledRepositoryConnectionFactory(factory, poolProperties);
		}
		if (resultCacheProperties != null) {
			factory = wrapWithCachingRepositoryConnectionFactory(factory, resultCacheProperties);
		}
		if (operationLog != null) {
			factory = wrapWithLoggingRepositoryConnectionFactory(factory, operationLog);
		}
		if (txProperties != null && txProperties.isEnabled()) {
			factory = wrapWithTxRepositoryConnectionFactory(factory);
		}
		return factory;
	}

	RepositoryConnectionFactory getDirectRepositoryConnectionFactory(Repository repository) {
		return new DirectRepositoryConnectionFactory(repository);
	}

	RepositoryConnectionFactory wrapWithPooledRepositoryConnectionFactory(
			RepositoryConnectionFactory delegate, PoolProperties poolProperties) {
		GenericObjectPoolConfig<RepositoryConnection> config = new GenericObjectPoolConfig<>();
		config.setMaxTotal(poolProperties.getMaxConnections());
		config.setMinIdle(poolProperties.getMinIdleConnections());
		config.setTimeBetweenEvictionRunsMillis(
				poolProperties.getTimeBetweenEvictionRuns().toMillis());
		config.setTestWhileIdle(poolProperties.isTestWhileIdle());
		return new PooledRepositoryConnectionFactory(delegate, config);
	}

	RepositoryConnectionFactory wrapWithLoggingRepositoryConnectionFactory(
			RepositoryConnectionFactory delegate, OperationLog operationLog) {
		return new LoggingRepositoryConnectionFactory(delegate, operationLog);
	}

	RepositoryConnectionFactory wrapWithCachingRepositoryConnectionFactory(
			RepositoryConnectionFactory delegate, ResultCacheProperties resultCacheProperties) {
		return new CachingRepositoryConnectionFactory(delegate, resultCacheProperties);
	}

	TransactionalRepositoryConnectionFactory wrapWithTxRepositoryConnectionFactory(
			RepositoryConnectionFactory delegate) {
		return new TransactionalRepositoryConnectionFactory(delegate);
	}
}
