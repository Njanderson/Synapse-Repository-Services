package org.sagebionetworks.repo.web.filter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.cloudwatch.Consumer;
import org.sagebionetworks.cloudwatch.ProfileData;
import org.sagebionetworks.repo.model.AuthorizationConstants;
import org.sagebionetworks.repo.model.AuthorizationConstants.BOOTSTRAP_PRINCIPAL;
import org.sagebionetworks.repo.model.dao.semaphore.CountingSemaphoreDao;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

public class UserThrottleFilterTest {

	private UserThrottleFilter filter;

	private CountingSemaphoreDao userThrottleGate;

	@Before
	public void setupFilter() throws Exception {
		userThrottleGate = mock(CountingSemaphoreDao.class);
		filter = new UserThrottleFilter();
		filter.setUserThrottleGate(userThrottleGate);
	}

	@Test
	public void testAnonymous() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter(AuthorizationConstants.USER_ID_PARAM, BOOTSTRAP_PRINCIPAL.ANONYMOUS_USER.getPrincipalId().toString());
		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain filterChain = mock(FilterChain.class);

		filter.doFilter(request, response, filterChain);

		verify(filterChain).doFilter(request, response);
		verifyNoMoreInteractions(filterChain, userThrottleGate);
	}

	@Test
	public void testNotAnonymous() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter(AuthorizationConstants.USER_ID_PARAM, "111");
		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain filterChain = mock(FilterChain.class);

		when(userThrottleGate.attemptToAcquireLock("111")).thenReturn("token");
		filter.doFilter(request, response, filterChain);

		verify(filterChain).doFilter(request, response);
		verify(userThrottleGate).attemptToAcquireLock("111");
		verify(userThrottleGate).releaseLock("token", "111");
		verifyNoMoreInteractions(filterChain, userThrottleGate);
	}

	@Test(expected = ServletException.class)
	public void testException() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter(AuthorizationConstants.USER_ID_PARAM, "111");
		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain filterChain = mock(FilterChain.class);

		when(userThrottleGate.attemptToAcquireLock("111")).thenThrow(new RuntimeException());
		try {
			filter.doFilter(request, response, filterChain);
		} finally {
			verify(userThrottleGate).attemptToAcquireLock("111");
			verifyNoMoreInteractions(filterChain, userThrottleGate);
		}
	}

	@Test
	public void testNoEmptySlots() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setParameter(AuthorizationConstants.USER_ID_PARAM, "111");
		MockHttpServletResponse response = new MockHttpServletResponse();
		FilterChain filterChain = mock(FilterChain.class);
		Consumer consumer = mock(Consumer.class);
		ReflectionTestUtils.setField(filter, "consumer", consumer);

		when(userThrottleGate.attemptToAcquireLock("111")).thenReturn(null);

		filter.doFilter(request, response, filterChain);
		assertEquals(503, response.getStatus());

		verify(userThrottleGate).attemptToAcquireLock("111");
		verify(consumer).addProfileData(any(ProfileData.class));
		verifyNoMoreInteractions(filterChain, userThrottleGate, consumer);
	}
}
