/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.api.db.hibernate;

import java.util.Date;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.Cohort;
import org.openmrs.api.db.CohortDAO;
import org.openmrs.api.db.DAOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hibernate implementation of the CohortDAO
 *
 * @see CohortDAO
 * @see org.openmrs.api.context.Context
 * @see org.openmrs.api.CohortService
 */
public class HibernateCohortDAO implements CohortDAO {
	
	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	private SessionFactory sessionFactory;
	
	/**
	 * Auto generated method comment
	 *
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	/**
	 * @see org.openmrs.api.db.CohortDAO#getCohort(java.lang.Integer)
	 */
	@Override
	public Cohort getCohort(Integer id) throws DAOException {
		return (Cohort) sessionFactory.getCurrentSession().get(Cohort.class, id);
	}

	/**
	 * @see org.openmrs.api.db.CohortDAO#getCohortsContainingPatientId(java.lang.Integer)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Cohort> getCohortsContainingPatientId(Integer patientId, boolean includeVoided,
	                                                  Date asOfDate) throws DAOException {
		Disjunction orEndDate = Restrictions.disjunction();
		orEndDate.add(Restrictions.isNull("m.endDate"));
		orEndDate.add(Restrictions.gt("m.endDate", asOfDate));

		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Cohort.class);
		criteria.createAlias("memberships", "m");
		criteria.add(Restrictions.le("m.startDate", asOfDate));
		criteria.add(orEndDate);
		criteria.add(Restrictions.eq("m.patientId", patientId));
		criteria.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		
		if (!includeVoided) {
			criteria.add(Restrictions.eq("voided", includeVoided));
		}
		return criteria.list();
	}

	/**
	 * @see org.openmrs.api.db.CohortDAO#getCohortsContainingPatientId(java.lang.Integer)
	 */
	@Override
	public List<Cohort> getCohortsContainingPatientId(Integer patientId) throws DAOException {
		return getCohortsContainingPatientId(patientId, false, new Date());
	}

	/**
	 * @see org.openmrs.api.db.CohortDAO#getCohortByUuid(java.lang.String)
	 */
	@Override
	public Cohort getCohortByUuid(String uuid) {
		return (Cohort) sessionFactory.getCurrentSession().createQuery("from Cohort c where c.uuid = :uuid").setString(
			"uuid", uuid).uniqueResult();
	}

	/**
	 * @see org.openmrs.api.db.CohortDAO#deleteCohort(org.openmrs.Cohort)
	 */
	@Override
	public Cohort deleteCohort(Cohort cohort) throws DAOException {
		sessionFactory.getCurrentSession().delete(cohort);
		return null;
	}
	
	/**
	 * @see org.openmrs.api.db.CohortDAO#getCohorts(java.lang.String)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Cohort> getCohorts(String nameFragment) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Cohort.class);
		criteria.add(Restrictions.ilike("name", nameFragment, MatchMode.ANYWHERE));
		criteria.addOrder(Order.asc("name"));
		return criteria.list();
	}
	
	/**
	 * @see org.openmrs.api.db.CohortDAO#getAllCohorts(boolean)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Cohort> getAllCohorts(boolean includeVoided) throws DAOException {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Cohort.class);
		
		criteria.addOrder(Order.asc("name"));
		
		if (!includeVoided) {
			criteria.add(Restrictions.eq("voided", false));
		}
		
		return (List<Cohort>) criteria.list();
	}
	
	/**
	 * @see org.openmrs.api.db.CohortDAO#getCohort(java.lang.String)
	 */
	@Override
	public Cohort getCohort(String name) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Cohort.class);
		
		criteria.add(Restrictions.eq("name", name));
		criteria.add(Restrictions.eq("voided", false));
		
		return (Cohort) criteria.uniqueResult();
	}
	
	/**
	 * @see org.openmrs.api.db.CohortDAO#saveCohort(org.openmrs.Cohort)
	 */
	@Override
	public Cohort saveCohort(Cohort cohort) throws DAOException {
		sessionFactory.getCurrentSession().saveOrUpdate(cohort);
		return cohort;
	}
}
