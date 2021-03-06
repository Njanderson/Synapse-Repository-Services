package org.sagebionetworks.table.query.model;

import org.sagebionetworks.table.query.model.visitors.Visitor;

/**
 * This matches &ltmatch value&gt  in: <a href="http://savage.net.au/SQL/sql-92.bnf">SQL-92</a>
 */
public class MatchValue extends SQLElement {
	
	CharacterValueExpression characterValueExpression;

	
	public MatchValue(CharacterValueExpression characterValueExpression) {
		this.characterValueExpression = characterValueExpression;
	}

	public CharacterValueExpression getCharacterValueExpression() {
		return characterValueExpression;
	}

	public void visit(Visitor visitor) {
		visit(characterValueExpression, visitor);
	}
}