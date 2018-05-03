package de.disk0.dbutil.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import de.disk0.dbutil.api.Aggregate;
import de.disk0.dbutil.api.Comparator;
import de.disk0.dbutil.api.Condition;
import de.disk0.dbutil.api.JoinTable;
import de.disk0.dbutil.api.Operator;
import de.disk0.dbutil.api.Select;
import de.disk0.dbutil.api.SubSelect;
import de.disk0.dbutil.api.TableReference;
import de.disk0.dbutil.api.exceptions.SqlException;
import de.disk0.dbutil.impl.mysql.MysqlStatementBuilder;

public class ExampleRepository extends AbstractGuidRepository<ExampleEntity> {
	
	// this class already inherits a bunch of methods from above:
	// get, save, save(with specific id), find(sql,params), delete()
	// 
	//  beforeSave(), afterSave(), beforeDelete(), afterDelete, have empty implementations
	//  and can be overridden
	//
	
	@Override
	protected void beforeSave(ExampleEntity t) throws Exception {
		// check validity? check if it already exists?
		super.beforeSave(t);
	}

	// for more complex methods, we can use the statement builder:
	
	public List<ExampleEntity> find(String parentId, List<String> names, String search, String userId, int offset, int max) throws SqlException {
		Select s = new MysqlStatementBuilder().createSelect();
		
		TableReference trEx = s.fromTable("example");
		
		if(!StringUtils.isEmpty(parentId)) {
			s.condition(Operator.AND,trEx.field("parent_id"),Comparator.EQ,trEx.value(parentId));
		}
		
		if(names!=null && names.size()>0) {
			// this should be an OR, so first we create a nested condition:
			Condition c = s.condition(Operator.AND);
			
			// and inside, we add the ORed names:
			for(String n : names) {
				c.condition(Operator.OR, trEx.field(Aggregate.UPPER, trEx.field("name")), Comparator.EQ, trEx.value(n.toUpperCase()));
			}
			
		}
		
		// lets do a JOIN as well:
		JoinTable jtPermissions = trEx.join("permissions");
		jtPermissions.addOn(Operator.AND,trEx.field("id"),Comparator.EQ,jtPermissions.field("object_id"));
		jtPermissions.addOn(Operator.AND,jtPermissions.field("user_id"),Comparator.EQ,jtPermissions.value(userId));
		

		// we can GROUP:
		s.group(trEx.field("name"));

		// we can ORDER:
		s.order(trEx.field("name"), true);
		
		// ... and LIMIT
		s.limit(offset, max);
		
		// the real fun begins when we do subselects:
		SubSelect sub = s.from();

		// the defaul top-level is to select "*", but we can change that (but then we have to add ALL fields we want:

		s.addSelect(trEx.field("name"),"name");
		s.addSelect(trEx.field("description"),"description");
		s.addSelect(Aggregate.AVG, "avg", trEx.field("size"));
		// etc ...
		
		return find(s.getSql(), s.getParams());
		
	}
	
	
	// you can override the map method:
	
	@Override
	public ExampleEntity mapRow(ResultSet rs, int c) throws SQLException {
		// so something else, such as load dependent objects
		return super.mapRow(rs, c);
	}
	
	
	// and you can override the generated statements to get, insert, etc:
	@Override
	protected String getDeleteOneStatement() {
		// maybe return a statement that also deletes all child objects?
		return super.getDeleteOneStatement();
	}
	

}
