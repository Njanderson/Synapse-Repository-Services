package org.sagebionetworks.repo.web.controller.metadata;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.Project;

public class ProjectMetadataProviderTest {
	
	Project mockProject;
	HttpServletRequest mockRequest;
	
	@Before
	public void before(){
		// Build the mocks
		mockProject = Mockito.mock(Project.class);
		when(mockProject.getId()).thenReturn("101");
		// Now the request
		mockRequest = Mockito.mock(HttpServletRequest.class);
		when(mockRequest.getServletPath()).thenReturn("/repo/v1");
		when(mockRequest.getRequestURI()).thenReturn("/project");
		
	}
	
	@Test
	public void testValidate(){
		ProjectMetadataProvider provider = new ProjectMetadataProvider();
		// Add more here.
		provider.validateEntity(mockProject);;
	}
	
	@Test
	public void testAddTypeSpecificMetadata(){
		ProjectMetadataProvider provider = new ProjectMetadataProvider();
		// Mock the dataset and the request
		Project project = new Project();
		project.setId("101");
		provider.addTypeSpecificMetadata(project, mockRequest);
		assertEquals("/repo/v1/project/101/annotations", project.getAnnotations());
	}

}
