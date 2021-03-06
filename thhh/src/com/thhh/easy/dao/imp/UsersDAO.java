package com.thhh.easy.dao.imp;

import java.util.List;
import java.util.Set;





import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import com.thhh.easy.dao.IUsersDao;
import com.thhh.easy.entity.Users;

/**
 * A data access object (DAO) providing persistence and search support for Users
 * entities. Transaction control of the save(), update() and delete() operations
 * can directly support Spring container-managed transactions or they can be
 * augmented to handle user-managed Spring transactions. Each of these methods
 * provides additional information for how to configure it for the desired type
 * of transaction control.
 * 
 * @see com.thhh.easy.entity.Users
 * @author MyEclipse Persistence Tools
 */
public class UsersDAO extends HibernateDaoSupport implements IUsersDao {
	private static final Logger log = LoggerFactory.getLogger(UsersDAO.class);
	// property constants
	public static final String NAME = "name";
	public static final String PWD = "pwd";
	public static final String NICKNAME = "nickname";
	public static final String EMAIL = "email";
	public static final String NUMBERS = "numbers";
	public static final String DEPART = "depart";
	public static final String TNAME = "tname";
	public static final String GENDER = "gender";
	public static final String RP = "rp";

	protected void initDao() {
		// do nothing
	}

	public void save(Users transientInstance) {
		log.debug("saving Users instance");
		try {
			getHibernateTemplate().save(transientInstance);
			log.debug("save successful");
		} catch (RuntimeException re) {
			log.error("save failed", re);
			throw re;
		}
	}

	public void delete(Users persistentInstance) {
		log.debug("deleting Users instance");
		try {
			getHibernateTemplate().delete(persistentInstance);
			log.debug("delete successful");
		} catch (RuntimeException re) {
			log.error("delete failed", re);
			throw re;
		}
	}

	/* (non-Javadoc)
	 * @see com.thhh.easy.dao.imp.IUsersDao#findById(java.lang.Integer)
	 */
	public Users findById(java.lang.Integer id) {
		log.debug("getting Users instance with id: " + id);
		try {
			Users instance = (Users) getHibernateTemplate().get(
					"com.thhh.easy.entity.Users", id);
			return instance;
		} catch (RuntimeException re) {
			log.error("get failed", re);
			throw re;
		}
	}

	public List findByExample(Users instance) {
		log.debug("finding Users instance by example");
		try {
			List results = getHibernateTemplate().findByExample(instance);
			log.debug("find by example successful, result size: "
					+ results.size());
			return results;
		} catch (RuntimeException re) {
			log.error("find by example failed", re);
			throw re;
		}
	}

	/* (non-Javadoc)
	 * @see com.thhh.easy.dao.imp.IUsersDao#findByProperty(java.lang.String, java.lang.Object)
	 */
	public List findByProperty(String propertyName, Object value) {
		log.debug("finding Users instance with property: " + propertyName
				+ ", value: " + value);
		try {
			String queryString = "from Users as model where model."
					+ propertyName + "= ?";
			return getHibernateTemplate().find(queryString, value);
		} catch (RuntimeException re) {
			log.error("find by property name failed", re);
			throw re;
		}
	}

	/**
	 * 根据用户名查询用户，用户存在返回user否则返回null
	 */
	@SuppressWarnings("unchecked")
	public Users findByName(String name) {
		String queryString = "from Users where name ='"+name+"'";
		Session session=getHibernateTemplate().getSessionFactory().openSession();
		Query query=session.createQuery(queryString);
		List<Users> listUser =query.list();	
		//	getHibernateTemplate().find(queryString, name) ;
		if(listUser != null && listUser.size() > 0){
			return listUser.get(0) ;
		}
		return null;
	}
	public List findByPwd(Object pwd) {
		return findByProperty(PWD, pwd);
	}

	public List findByNickname(Object nickname) {
		return findByProperty(NICKNAME, nickname);
	}

	public List findByEmail(Object email) {
		return findByProperty(EMAIL, email);
	}

	public List findByNumbers(Object numbers) {
		return findByProperty(NUMBERS, numbers);
	}

	public List findByDepart(Object depart) {
		return findByProperty(DEPART, depart);
	}

	public List findByTname(Object tname) {
		return findByProperty(TNAME, tname);
	}

	public List findByGender(Object gender) {
		return findByProperty(GENDER, gender);
	}

	public List findByRp(Object rp) {
		return findByProperty(RP, rp);
	}

	/* (non-Javadoc)
	 * @see com.thhh.easy.dao.imp.IUsersDao#findAll()
	 */
	public Users findAll() {
		log.debug("finding all Users instances");
		try {
			String queryString = "from Users";
			return (Users) getHibernateTemplate().find(queryString);
		} catch (RuntimeException re) {
			log.error("find all failed", re);
			throw re;
		}
	}

	/* (non-Javadoc)
	 * @see com.thhh.easy.dao.imp.IUsersDao#merge(com.thhh.easy.entity.Users)
	 */
	public Users merge(Users detachedInstance) {
		log.debug("merging Users instance");
		try {
			Users result = (Users) getHibernateTemplate().merge(
					detachedInstance);
			log.debug("merge successful");
			return result;
		} catch (RuntimeException re) {
			log.error("merge failed", re);
			throw re;
		}
	}

	public void attachDirty(Users instance) {
		log.debug("attaching dirty Users instance");
		try {
			getHibernateTemplate().saveOrUpdate(instance);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public void attachClean(Users instance) {
		log.debug("attaching clean Users instance");
		try {
			getHibernateTemplate().lock(instance, LockMode.NONE);
			log.debug("attach successful");
		} catch (RuntimeException re) {
			log.error("attach failed", re);
			throw re;
		}
	}

	public static IUsersDao getFromApplicationContext(ApplicationContext ctx) {
		return (IUsersDao) ctx.getBean("UsersDAO");
	}
}