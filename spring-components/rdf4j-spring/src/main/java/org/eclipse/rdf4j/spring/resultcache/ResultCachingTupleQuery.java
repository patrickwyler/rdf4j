package org.eclipse.rdf4j.spring.resultcache;

import java.lang.invoke.MethodHandles;
import java.lang.ref.WeakReference;

import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.spring.support.query.DelegatingTupleQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResultCachingTupleQuery extends DelegatingTupleQuery {
	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	private WeakReference<ResultCache<Integer, ReusableTupleQueryResult>> localResultCacheRef;
	private ResultCache<Integer, ReusableTupleQueryResult> globalResultCache;
	private ResultCacheProperties properties;

	public ResultCachingTupleQuery(
			TupleQuery delegate,
			ResultCache<Integer, ReusableTupleQueryResult> localResultCache,
			ResultCache<Integer, ReusableTupleQueryResult> globalResultCache,
			ResultCacheProperties properties) {
		super(delegate);
		this.localResultCacheRef = new WeakReference<>(localResultCache);
		this.globalResultCache = globalResultCache;
		this.properties = properties;
	}

	public void renewLocalResultCache(
			ResultCache<Integer, ReusableTupleQueryResult> localResultCache) {
		if (logger.isDebugEnabled()) {
			ResultCache<Integer, ReusableTupleQueryResult> previousCache = localResultCacheRef.get();
			logger.debug(
					"resetting local result cache to {} (was: {})",
					localResultCache.hashCode(),
					previousCache != null ? previousCache.hashCode() : "null");
		}
		this.localResultCacheRef = new WeakReference<>(localResultCache);
	}

	@Override
	public TupleQueryResult evaluate() throws QueryEvaluationException {
		BindingSet currentBindings = getDelegate().getBindings();
		Integer cacheKey = currentBindings.hashCode() + getDelegate().hashCode();
		logger.debug("Checking global result cache");
		TupleQueryResult cachedResult;
		if (properties.isAssumeNoOtherRepositoryClients()) {
			cachedResult = recreateCachedResultIfPossible(globalResultCache, currentBindings, cacheKey);
			if (cachedResult != null) {
				return cachedResult;
			}
		}
		logger.debug("Checking local result cache");
		ResultCache<Integer, ReusableTupleQueryResult> localResultCache = localResultCacheRef.get();
		if (localResultCache != null) {
			cachedResult = recreateCachedResultIfPossible(localResultCache, currentBindings, cacheKey);
			if (cachedResult != null) {
				return cachedResult;
			}
		}
		logger.debug("No reusable cached result found, executing query");
		TupleQueryResult delegateResult = getDelegate().evaluate();
		if (delegateResult instanceof ReusableTupleQueryResult) {
			throw new IllegalStateException(
					"Cannot cache an already cached result! This should not happen, the caching layer seems misconfigured.");
		}
		ReusableTupleQueryResult cacheableResult = new ReusableTupleQueryResult(delegateResult, currentBindings);
		if (localResultCache != null) {
			localResultCache.put(cacheKey, cacheableResult);
		}
		if (properties.isAssumeNoOtherRepositoryClients()) {
			globalResultCache.put(cacheKey, cacheableResult);
		}
		return cacheableResult;
	}

	private TupleQueryResult recreateCachedResultIfPossible(
			ResultCache<Integer, ReusableTupleQueryResult> cache,
			BindingSet currentBindings,
			Integer cacheKey) {
		ReusableTupleQueryResult result = cache.get(cacheKey);
		if (result != null
				&& result.queryBindingsAreIdentical(currentBindings)
				&& result.canReuse()) {
			logger.debug("Reusing cached result");
			return result.recreateTupleQueryResult();
		}
		return null;
	}

	@Override
	public void evaluate(TupleQueryResultHandler handler)
			throws QueryEvaluationException, TupleQueryResultHandlerException {
		TupleQueryResult queryResult = evaluate();
		QueryResults.report(queryResult, handler);
	}
}
