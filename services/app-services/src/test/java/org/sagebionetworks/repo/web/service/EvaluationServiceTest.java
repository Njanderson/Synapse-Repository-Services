package org.sagebionetworks.repo.web.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sagebionetworks.evaluation.manager.EvaluationManager;
import org.sagebionetworks.evaluation.manager.EvaluationPermissionsManager;
import org.sagebionetworks.evaluation.manager.ParticipantManager;
import org.sagebionetworks.evaluation.manager.SubmissionManager;
import org.sagebionetworks.evaluation.model.Submission;
import org.sagebionetworks.repo.manager.MessageToUserAndBody;
import org.sagebionetworks.repo.manager.NotificationManager;
import org.sagebionetworks.repo.manager.UserManager;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.UserInfo;
import org.sagebionetworks.repo.model.message.MessageToUser;
import org.sagebionetworks.repo.model.query.QueryDAO;

public class EvaluationServiceTest {

	private EvaluationServiceImpl evaluationService;
	private ServiceProvider mockServiceProvider;
	private EntityBundleService mockEntityBundleService;
	private EvaluationManager mockEvaluationManager;
	private ParticipantManager mockParticipantManager;
	private SubmissionManager mockSubmissionManager;
	private EvaluationPermissionsManager mockEvaluationPermissionsManager;
	private UserManager mockUserManager;
	private QueryDAO mockQueryDAO;
	private NotificationManager mockNotificationManager;

	
	
	@Before
	public void before() throws Exception {
		mockServiceProvider = Mockito.mock(ServiceProvider.class);
		mockEvaluationManager = Mockito.mock(EvaluationManager.class);
		mockParticipantManager = Mockito.mock(ParticipantManager.class);
		mockSubmissionManager = Mockito.mock(SubmissionManager.class);
		mockEvaluationPermissionsManager = Mockito.mock(EvaluationPermissionsManager.class);
		mockUserManager = Mockito.mock(UserManager.class);
		mockQueryDAO = Mockito.mock(QueryDAO.class);
		mockNotificationManager = Mockito.mock(NotificationManager.class);
		mockEntityBundleService = Mockito.mock(EntityBundleService.class);

		this.evaluationService = new EvaluationServiceImpl(
				mockServiceProvider,
				mockEvaluationManager,
				mockParticipantManager,
				mockSubmissionManager,
				mockEvaluationPermissionsManager,
				mockUserManager,
				mockQueryDAO,
				mockNotificationManager);
	}

	@Test
	public void testCreateSubmission() throws Exception {
		Long userId = 111L;
		String challengeEndpoint = "challengeEndpoint:";
		String notificationUnsubscribeEndpoint = "notificationUnsubscribeEndpoint:";
		UserInfo userInfo = new UserInfo(false); 
		userInfo.setId(userId);
		when(mockUserManager.getUserInfo(userId)).thenReturn(userInfo);
		MessageToUser mtu = new MessageToUser();
		mtu.setRecipients(Collections.singleton("222"));
		String content = "foo";
		List<MessageToUserAndBody> result = Collections.singletonList(new MessageToUserAndBody(mtu, content, "text/plain"));
		Submission submission = new Submission();
		when(mockSubmissionManager.createSubmission(eq(userInfo), eq(submission), anyString(), 
				anyString(), any(EntityBundle.class))).thenReturn(submission);
		when(mockSubmissionManager.createSubmissionNotifications(
				eq(userInfo), eq(submission), anyString(), 
				eq(challengeEndpoint), eq(notificationUnsubscribeEndpoint))).thenReturn(result);

		when(mockServiceProvider.getEntityBundleService()).thenReturn(mockEntityBundleService);
		evaluationService.createSubmission(userId, submission, "123", "987", null,
				challengeEndpoint, notificationUnsubscribeEndpoint);
		verify(mockUserManager).getUserInfo(userId);
		verify(mockSubmissionManager).createSubmission(eq(userInfo), eq(submission), eq("123"), eq("987"), 
				any(EntityBundle.class));
		verify(mockSubmissionManager).createSubmissionNotifications(
				eq(userInfo), any(Submission.class), eq("987"),
				eq(challengeEndpoint), eq(notificationUnsubscribeEndpoint));
		
		ArgumentCaptor<List> mtuArg = ArgumentCaptor.forClass(List.class);
		verify(mockNotificationManager).sendNotifications(eq(userInfo), mtuArg.capture());
		assertEquals(result, mtuArg.getValue());		
	}

}
