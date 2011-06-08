package org.sagebionetworks.repo.model.jdo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sagebionetworks.repo.model.AuthorizationConstants;
import org.sagebionetworks.repo.model.UserGroupDAO;
import org.sagebionetworks.repo.model.UserGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:jdomodels-test-context.xml" })

public class JDOUserGroupDAOImplTest {
	
	@Autowired
	private UserGroupDAO userGroupDAO;

	@Before
	public void setUp() throws Exception {
		cleanUpGroups();
	}

	@After
	public void tearDown() throws Exception {
		Collection<String> groupNames = new HashSet<String>();
		groupNames.add(GROUP_NAME);
		Map<String,UserGroup> map = userGroupDAO.getGroupsByNames(groupNames);
		UserGroup toDelete = map.get(GROUP_NAME);
		if (toDelete!=null) {
			userGroupDAO.delete(toDelete.getId());
		}
		cleanUpGroups();
	}
	
	public void cleanUpGroups() throws Exception {
		for (UserGroup g: userGroupDAO.getAll()) {
			if (g.getName().equals(AuthorizationConstants.ADMIN_GROUP_NAME)) {
				// leave it
			} else if (g.getName().equals(AuthorizationConstants.PUBLIC_GROUP_NAME)) {
				// leave it
			} else if (g.getName().equals(AuthorizationConstants.ANONYMOUS_USER_ID)) {
				// leave it
			} else {
				userGroupDAO.delete(g.getId());
			}
		}
	}
	
	@Test
	public void findAnonymousUser() throws Exception {
		assertNotNull(userGroupDAO.findGroup(AuthorizationConstants.ANONYMOUS_USER_ID, true));
	}

	private static final String GROUP_NAME = "test-group";
	@Test
	public void testGetGroupsByNames() throws Exception {
		Collection<UserGroup> allGroups = null; 
		allGroups = userGroupDAO.getAll();
		assertEquals(allGroups.toString(), 2, allGroups.size()); // Public and Anonymous
	
		Collection<String> groupNames = new HashSet<String>();
		groupNames.add(GROUP_NAME);
		Map<String,UserGroup> map = null;
		map = userGroupDAO.getGroupsByNames(groupNames);
		assertFalse(map.containsKey(GROUP_NAME));
		
		UserGroup group = new UserGroup();
		group.setName(GROUP_NAME);
		userGroupDAO.create(group);
		
		allGroups = userGroupDAO.getAll();
		assertEquals(allGroups.toString(), 3, allGroups.size()); // now the new group should be there
			
		groupNames.clear(); 	groupNames.add(GROUP_NAME);	
		map = userGroupDAO.getGroupsByNames(groupNames);
		assertTrue(groupNames.toString()+" -> "+map.toString(), map.containsKey(GROUP_NAME));
		
		
		groupNames.clear(); groupNames.add(AuthorizationConstants.PUBLIC_GROUP_NAME);
		map = userGroupDAO.getGroupsByNames(groupNames);
		assertTrue(map.toString(), map.containsKey(AuthorizationConstants.PUBLIC_GROUP_NAME));

	}

}
