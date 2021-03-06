package org.sagebionetworks.audit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.sagebionetworks.repo.model.ACCESS_TYPE;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.audit.AccessRecord;
import org.sagebionetworks.repo.model.audit.AclRecord;
import org.sagebionetworks.repo.model.audit.ResourceAccessRecord;
import org.sagebionetworks.repo.model.message.ChangeType;

/**
 * 
 * @author jmhill
 *
 */
public class AuditTestUtils {

	/**
	 * Create a list for use in testing that is sorted on timestamp
	 * @param count
	 * @param startTimestamp
	 * @return
	 */
	public static List<AccessRecord> createAccessRecordList(int count, long startTimestamp){
		List<AccessRecord> list = new LinkedList<AccessRecord>();
		for(int i=0; i<count; i++){
			AccessRecord ar = new AccessRecord();
			ar.setUserId((long) i);
			ar.setElapseMS((long) (10*i));
			ar.setTimestamp(startTimestamp+i);
			ar.setMethod(Method.values()[i%4].toString());
			if(i%2 > 0){
				ar.setSuccess(true);
				ar.setResponseStatus(201L);
			}else{
				ar.setSuccess(false);
				ar.setResponseStatus(401L);
			}
			ar.setRequestURL("/url/"+i);
			ar.setSessionId(UUID.randomUUID().toString());
			ar.setHost("localhost:8080");
			ar.setOrigin("http://www.example-social-network.com");
			ar.setUserAgent("The bat-mobile OS");
			ar.setThreadId(Thread.currentThread().getId());
			ar.setVia("1 then two");
			ar.setStack("stack");
			ar.setInstance("0001");
			ar.setVmId("vmId");
			ar.setQueryString("value=bar");
			ar.setReturnObjectId("syn123");
			list.add(ar);
		}
		return list;
	}
	
	enum Method{
		GET,POST,PUT,DELETE
	}

	/**
	 * create and return numberOfRecords AclRecords
	 */
	public static List<AclRecord> createAclRecordList(int numberOfRecords) {
		List<AclRecord> list = new ArrayList<AclRecord>();
		for (int i = 0; i < numberOfRecords; i++) {
			AclRecord newRecord = new AclRecord();
			newRecord.setTimestamp(System.currentTimeMillis());
			newRecord.setChangeNumber(-1L);
			newRecord.setChangeType(ChangeType.CREATE);
			newRecord.setOwnerId("-1");
			newRecord.setEtag("etag");
			newRecord.setAclId("-1");
			newRecord.setOwnerType(ObjectType.ENTITY);

			list.add(newRecord);
		}
		return list;
	}

	/**
	 * create and return numberOfRecords ResourceAccessRecords
	 */
	public static List<ResourceAccessRecord> createResourceAccessRecordList(int numberOfRecords) {
		List<ResourceAccessRecord> list = new ArrayList<ResourceAccessRecord>();
		for (int i = 0; i < numberOfRecords; i++) {
			ResourceAccessRecord newRecord = new ResourceAccessRecord();
			newRecord.setAccessType(ACCESS_TYPE.READ);
			newRecord.setChangeNumber(-1L);
			newRecord.setPrincipalId(-1L);

			list.add(newRecord);
		}
		return list;
	}
}
