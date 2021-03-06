package org.sagebionetworks.table.query.model;

import org.sagebionetworks.table.query.model.visitors.ToSimpleSqlVisitor;
import org.sagebionetworks.table.query.model.visitors.Visitor;


/**
 * This matches &ltsort specification&gt   in: <a href="http://savage.net.au/SQL/sql-92.bnf">SQL-92</a>
 */
public class SortSpecification extends SQLElement {
	
    SortKey sortKey;
    OrderingSpecification orderingSpecification;
	public SortSpecification(SortKey sortKey,
			OrderingSpecification orderingSpecification) {
		this.sortKey = sortKey;
		this.orderingSpecification = orderingSpecification;
	}
	public SortKey getSortKey() {
		return sortKey;
	}
	public OrderingSpecification getOrderingSpecification() {
		return orderingSpecification;
	}

	public void visit(Visitor visitor) {
		visit(sortKey, visitor);
	}

	public void visit(ToSimpleSqlVisitor visitor) {
		visit(sortKey, visitor);
		if(orderingSpecification != null){
			visitor.append(" ");
			visitor.append(orderingSpecification.name());
		}
	}
}
