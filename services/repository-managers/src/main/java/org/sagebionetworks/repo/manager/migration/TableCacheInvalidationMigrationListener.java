package org.sagebionetworks.repo.manager.migration;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sagebionetworks.StackConfiguration;
import org.sagebionetworks.repo.model.dao.table.TableRowTruthDAO;
import org.sagebionetworks.repo.model.dao.table.TableStatusDAO;
import org.sagebionetworks.repo.model.dbo.DatabaseObject;
import org.sagebionetworks.repo.model.jdo.KeyFactory;
import org.sagebionetworks.repo.model.migration.MigrationType;
import org.sagebionetworks.table.cluster.ConnectionFactory;
import org.sagebionetworks.table.cluster.TableIndexDAO;
import org.springframework.beans.factory.annotation.Autowired;

public class TableCacheInvalidationMigrationListener implements MigrationTypeListener {

	private static final Logger log = LogManager.getLogger(TableCacheInvalidationMigrationListener.class);

	@Autowired
	TableRowTruthDAO tableRowTruthDAO;

	@Autowired
	TableStatusDAO tableStatusDAO;

	@Autowired
	ConnectionFactory tableConnectionFactory;

	@Override
	public <D extends DatabaseObject<?>> void afterCreateOrUpdate(MigrationType type, List<D> delta) {
		// nothing here
	}

	@Override
	public void beforeDeleteBatch(MigrationType type, List<Long> idsToDelete) {
		// we only deal with table change sets here
		if (type != MigrationType.TABLE_CHANGE) {
			return;
		}

		if (!StackConfiguration.singleton().getTableEnabled()) {
			return;
		}

		try {
			// the idsToDelete is a list of table ids for this type
			for (Long tableId : idsToDelete) {
				log.info("Deleting table " + tableId + " from index due to migration");
				String tableIdString = KeyFactory.keyToString(tableId);
				TableIndexDAO indexDAO = tableConnectionFactory.getConnection(tableIdString);
				tableRowTruthDAO.removeCaches(tableId);
				indexDAO.deleteTable(tableIdString);
				indexDAO.deleteStatusTable(tableIdString);
				// we must also delete the table status here. If we don't, a stale reset token in the status table entry
				// could prevent the table from being rebuilt
				// see PLFM-3077
				tableStatusDAO.deleteTableStatus(tableIdString);
			}
		} catch (IOException e) {
			// we want the migration to fail if any of the caches cannot be cleared
			throw new RuntimeException(e.getMessage(), e);
		}
	}
}
