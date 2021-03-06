package org.sagebionetworks.repo.model.dbo.dao.table;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.Row;
import org.sagebionetworks.repo.model.table.TableConstants;
import org.sagebionetworks.table.cluster.utils.TableModelUtils;
import org.sagebionetworks.util.csv.CsvNullReader;

/**
 * Provides an Iterator<Row> abstraction over a raw CSV reader.
 * 
 * @author jmhill
 * 
 */
public class CSVToRowIterator implements Iterator<Row> {

	private List<ColumnModel> resultSchema;
	private List<Long> ids;
	private CsvNullReader reader;
	private String[] lastRow;
	private int rowLineNumber;
	private Integer rowIdIndex;
	private Integer rowVersionIndex;
	private int[] indexMapping;

	/**
	 * Create a new object for each use.
	 * 
	 * @param resultSchema
	 *            Each row returned will match this schema.
	 * @param reader
	 *            The CSV stream that contains the source data. Data will be
	 *            read from this stream and translated into rows. It is the job
	 *            of the caller to close this stream when finished.
	 * @param progressReporter
	 * @throws IOException
	 */
	public CSVToRowIterator(List<ColumnModel> resultSchema, CsvNullReader reader, boolean isFirstLineHeader)
			throws IOException {
		this.resultSchema = resultSchema;
		this.reader = reader;
		this.ids = TableModelUtils.getIds(resultSchema);
		this.rowLineNumber = 1;
		// We need to read the first row to determine if it a header
		lastRow = reader.readNext();
		// We need a map of column ID to index.
		Map<Long, Integer> idToIndexMap;
		if (isFirstLineHeader) {
			idToIndexMap = TableModelUtils.createColumnIdToIndexMapFromFirstRow(lastRow, resultSchema);
			if (idToIndexMap == null) {
				throw new IllegalArgumentException(
						"The first line was expected to be a header but the values did not match the names of of the columns of the table. Header row: "
								+ StringUtils.join(lastRow, ','));
			}
			// Since the first row was a header, we need to next row to start.
			lastRow = reader.readNext();
			rowLineNumber++;
		} else {
			// This means the row is not a header. So just map from the schema
			idToIndexMap = TableModelUtils.createColumnIdToIndexMap(this.ids);
		}
		// Does contain RowId?
		rowIdIndex = idToIndexMap.get(TableConstants.ROW_ID_ID);
		rowVersionIndex = idToIndexMap.get(TableConstants.ROW_VERSION_ID);
		// build the index mapping
		this.indexMapping = new int[resultSchema.size()];
		int index = 0;
		for(ColumnModel cm: this.resultSchema){
			// Lookup the mapping for this column
			Integer valueIndex = idToIndexMap.get(Long.parseLong(cm.getId()));
			if(valueIndex == null){
				throw new IllegalArgumentException("Expected a column with the name: "+cm.getName()+" but did not find it");
			}
			indexMapping[index] = valueIndex;
			index++;
		}
	}

	@Override
	public boolean hasNext() {
		return lastRow != null;
	}

	@Override
	public Row next() {
		// Convert the row.
		Row row = new Row();
		// Do we have ROW_ID?
		if(rowIdIndex != null){
			String value = lastRow[rowIdIndex];
			if(value != null){
				row.setRowId(new Long(value));
			}

		}
		// Do we have a row version?
		if(rowVersionIndex != null){
			String value = lastRow[rowVersionIndex];
			if(value != null){
				row.setVersionNumber(new Long(value));
			}
		}
		List<String> values = new ArrayList<String>(this.resultSchema.size());
		// Copy over the values according to the mapping
		for(int i=0; i<this.resultSchema.size(); i++){
			int index = this.indexMapping[i];
			if (index >= lastRow.length) {
				throw new IllegalArgumentException("Line number " + rowLineNumber + ": column index " + index + " out of bounds for row "
						+ lastRow);
			}
			values.add(lastRow[index]);
		}
		row.setValues(values);

		// Net the next row
		try {
			lastRow = this.reader.readNext();
			rowLineNumber++;
		} catch (IOException e) {
			throw new RuntimeException("Line number " + rowLineNumber + ": " + e.getMessage(), e);
		}
		return row;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Not supported");
	}

}
