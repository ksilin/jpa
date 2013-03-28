package pro.jpa2.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import javax.ejb.Stateful;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Retrieves entities from DB. Main features are: <br/>
 * <br/>
 * - search by predicates passed as {@code Map<String, String>}<br/>
 * <br/>
 * - predicate keys may have the form "prop.nestedprop" referring to nested
 * properties - in this case the "nestedprop" property of the "prop" property of
 * the entity<br/>
 * <br/>
 * - wildcard search. If the value of a predicate contains the sql widcard -
 * `%`, this results in a case-insensitive LIKE search .<br/>
 * <br/>
 * -WARNING! The wildcard feature is not yet sufficiently tested. It seems works
 * properly for Strings, chars and ints. It will not work for Dates
 *
 * @author silin
 * @param <T>
 *            - the type of the retrieved entity
 */
@Stateful
public class GenericDao<T> {

	private static final Logger LOG = LoggerFactory.getLogger(GenericDao.class);

	@Inject
	protected transient EntityManager em;

	@Inject
	protected transient CriteriaBuilder criteriaBuilder;

	/**
	 * Unfortunately necessary as the class of T cannot be retrieved otherwise.
	 * Used for criteria queries
	 */
	protected Class<T> klazz;

	public List<T> findAll() {
		return find(null, 0, Integer.MAX_VALUE, null);
	}

	/**
	 * Paged unfiltered retrieval.
	 *
	 * @param startIndex
	 *            - index of the first retuned item
	 * @param howMany
	 * @return
	 */
	public List<T> find(final int startIndex, final int howMany) {
		return find(null, startIndex, howMany, null);
	}

	/**
	 * Accepts parameters with existing wildcards (%). Does not generate
	 * wildcards.
	 *
	 * @param stringPredicates
	 * @return
	 */
	public List<T> find(final Map<String, String> stringPredicates) {
		return find(stringPredicates, 0, Integer.MAX_VALUE, null);
	}

	/**
	 * Accepts parameters with existing wildcards (%). Does not generate
	 * wildcards.
	 *
	 * @param stringPredicates
	 * @return
	 */
	public List<T> find(final Map<String, String> stringPredicates,
			final int firstIndex, final int howMany) {
		return find(stringPredicates, firstIndex, howMany, null);
	}

	/**
	 * Accepts parameters with existing wildcards (%). Does not generate
	 * wildcards.
	 *
	 * @param stringPredicates
	 * @return
	 */
	public List<T> find(final Map<String, String> stringPredicates,
			final int firstIndex, final int howMany, final Ordering order) {
		LOG.debug("retrieving {} by properties: {}", klazz, stringPredicates);

		final CriteriaQuery<T> query = criteriaBuilder.createQuery(klazz);
		final Root<T> root = query.from(klazz);

		orderQuery(root, query, order);

		final List<Predicate> predicates = createPredicates(stringPredicates,
				root);

		query.where(predicates.toArray(new Predicate[predicates.size()]));
		query.select(root);
		final TypedQuery<T> typedQuery = em.createQuery(query);
		typedQuery.setFirstResult(firstIndex);
		typedQuery.setMaxResults(howMany);

		return typedQuery.getResultList();
	}

	/**
	 * Orders the query according to the order param. If no order is passed, no
	 * ordering will take place.
	 *
	 * @param root
	 * @param query
	 * @param order
	 */
	private void orderQuery(final Root<T> root, final CriteriaQuery<T> query,
			final Ordering order) {

		if (order != null) {
			final Path<Object> orderProp = toNestedPath(root,
					order.getPropName());

			Order cbOrder = criteriaBuilder.asc(orderProp);
			if (order == Ordering.DESC) {
				cbOrder = criteriaBuilder.desc(orderProp);
			}
			query.orderBy(cbOrder);
		}
	}

	/**
	 * decomposes a string where nested properties are delimited wiht a dot and
	 * cosntructs a Path for the Predicate
	 *
	 * @param root
	 * @param nestedProp
	 * @return
	 */
	protected Path<Object> toNestedPath(final Root<T> root,
			final String nestedProp) {

		final String[] props = nestedProp.split("\\.");
		LOG.info("split ordering string : {}", props);

		Path<Object> nestedPredicate = root.<Object> get(props[0]);
		for (int i = 1; i < props.length; i++) {
			nestedPredicate = nestedPredicate.get(props[i]);
		}
		return nestedPredicate;
	}

	/**
	 * Unfortunately I have not found a better way to incorporate wildcard
	 * queries.
	 *
	 * @param root
	 * @param nestedProp
	 * @return
	 */
	protected Path<String> toNestedStringPath(final Root<T> root,
			final String nestedProp) {

		final String[] props = nestedProp.split("\\.");
		LOG.info("split ordering string : {}", props);

		Path<String> nestedPredicate = root.<String> get(props[0]);
		for (int i = 1; i < props.length; i++) {
			nestedPredicate = nestedPredicate.get(props[i]);
		}
		return nestedPredicate;
	}

	protected List<Predicate> createPredicates(
			final Map<String, String> stringPredicates, final Root<T> root) {

		final List<Predicate> predicates = new ArrayList<Predicate>();

		if (stringPredicates != null) {

			for (final Entry<String, String> e : stringPredicates.entrySet()) {

				final String key = e.getKey();
				final String value = e.getValue();

				if ((key != null) && (value != null)) {

					// Case insensitive starts-with queries with wildcards
					if (value.contains("%")) {
						predicates.add(criteriaBuilder.like(criteriaBuilder
								.upper(toNestedStringPath(root, key)), value
								.toUpperCase(Locale.GERMANY)));
					} else {
						predicates.add(criteriaBuilder.equal(root.get(key),
								value));
					}
				}
			}
		}
		return predicates;
	}

	// TODO : incorporate better with the main get method
	/**
	 * Accepts paramenters with existing wildcards (%). Does not generate
	 * wildcards.
	 *
	 * @param <E>
	 * @param stringPredicates
	 * @return
	 */
	public <E> List<E> getProp(final Map<String, String> stringPredicates,
			final String propName, final Class<E> propClass,
			final boolean distinct) {
		LOG.debug("retrieving property {} of {} by predicates: {}",
				new Object[] { propName, klazz, stringPredicates });

		final CriteriaQuery<E> query = criteriaBuilder.createQuery(propClass);
		final Root<T> root = query.from(klazz);

		final List<Predicate> predicates = createPredicates(stringPredicates,
				root);

		query.where(predicates.toArray(new Predicate[predicates.size()]));
		query.select(root.<E> get(propName)).distinct(distinct);
		final List<E> result = em.createQuery(query).getResultList();

		LOG.debug("found {} results : {}", result.size(), result);
		return result;
	}

	public Class<T> getKlazz() {
		return klazz;
	}

	public void setKlazz(final Class<T> klazz) {
		this.klazz = klazz;
	}

	/**
	 * Returns true, if the entity has been successfully created, false otherwise
	 *
	 * @param entity
	 * @param primaryKey
	 * @return
	 */
	public boolean create(final T entity, final Object primaryKey) {
		LOG.debug("creating {}", entity);
		final T found = em.find(klazz, primaryKey);
		if (found == null) {
			em.persist(entity);
			LOG.info("created {}", entity);
			return true;
		}
		LOG.warn("an identical {} already exists: {}", klazz, found);
		return false;
	}
}
