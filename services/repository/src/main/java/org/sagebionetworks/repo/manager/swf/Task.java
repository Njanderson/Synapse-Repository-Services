package org.sagebionetworks.repo.manager.swf;

import com.amazonaws.services.simpleworkflow.model.TaskList;

/**
 * An abstraction for a task.  Both Decider and Activity extend this interface.
 * @author John
 *
 */
public interface Task {
	
	/**
	 * The task list that this decider works from.
	 * 
	 * @return
	 */
	public TaskList getTaskList();

	/**
	 * The name of the domain that this decider works from.
	 * @return
	 */
	public String getDomainName();

}