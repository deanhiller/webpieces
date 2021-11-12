package org.webpieces.router.api.extensions;

import java.util.Map;
import org.webpieces.util.futures.XFuture;

/**
 * SimpleStorage is a simple standard interface for Plugins to save their settings to the database
 * 
 * Your table in a JDBC database will be
 * id column= primary key
 * key column = column key matching key param below
 * subKey column = column subKey matching subKey param below
 * (Unique constraint on key/subKey together)
 * value column = column to store the value
 * 
 * NoSql is a bunch of very wide rows(or narrow) so your
 * primary key = key param below
 * column name = subKey param below
 * column value = value param below
 * 
 */
public interface SimpleStorage {

	//YES, this looks ALOT like noSQL to start with so it works with nosql and RDBMS so your backend
	//storage for plugin data is what you decide you want it to be!!!
	//table can be a column or an actual table(your choice).  In the case of noSql, it can be a column family
	//or just prepended to the key.
	public XFuture<Void> save(String key, String subKey, String value);
	
	//OR in noSQL update many pieces of the row (or in RDBMS, this updates many rows)
	public XFuture<Void> save(String key, Map<String, String> properties);

	//READ the entire row in noSQL (and in RDBMS, read all the rows that have that key)
	public XFuture<Map<String, String>> read(String key);
	
	//DELETE any rows in DB where the key matches the key column or in noSql, just delete the long row
	public XFuture<Void> delete(String key);
	
	//DELETE one row in DB where key/subkey match or in noSql, delete the column with pkey=key and columnName=subkey
	public XFuture<Void> delete(String key, String subKey);
	
}
