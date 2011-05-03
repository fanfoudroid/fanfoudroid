package com.ch_linghu.fanfoudroid.db.dao;

import java.util.ArrayList;
import java.util.Arrays;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Wrapper of SQliteDatabse#query, OOP style.
 * 
 * Usage:
 * ------------------------------------------------
 * Query select = new Query(SQLiteDatabase);
 * select.from("tableName", new String[] { "colName" })
 *       .where("id = ?", 123456)
 *       .where("name = ?", "jack")
 *       .orderBy("created_at DESC")
 *       .limit(1);
 * Cursor cursor = select.query();
 * ------------------------------------------------
 */
public class Query {
    private static final String TAG = "Query-Builder";
    private static boolean debug = true;
    
    private ArrayList<String> select = new ArrayList<String>();
    private ArrayList<String> binds = new ArrayList<String>();
    private SQLiteDatabase db = null;
    
    private String table;
    private String[] columns;
    private String selection;
    private String[] selectionArgs;
    private String groupBy = null;
    private String having = null;
    private String orderBy = null;
    private String limit = null;
    
    public Query() { }
    
    /**
     * Construct 
     * 
     * @param db 
     */
    public Query(SQLiteDatabase db) {
        this.setDb(db);
    }
    
    /**
     * Query the given table, returning a Cursor over the result set.
     * 
     * @see android.database.sqlite.SQLiteDatabase.query
     * @return
     */
    public Cursor query() {
        Cursor cursor = null;
        if (db != null) {
            cursor = query(db);
        }
        return cursor;
    }

    /**
     * Query the given table, returning a Cursor over the result set.
     * 
     * @see android.database.sqlite.SQLiteDatabase.query
     * @param db
     * @return
     */
    public Cursor query(SQLiteDatabase db) {
        if ( preCheck() ) {
            buildQuery();
            return db.query(table, columns, selection, selectionArgs,
                        groupBy, having, orderBy, limit);
        } else {
            //throw new SelectException("Cann't build the query . " + toString());
            Log.e(TAG, "Cann't build the query " + toString());
            return null;
        }
    }
    
    /**
     * Set FROM
     * 
     * @see android.database.sqlite.SQLiteDatabase.query
     * @param table
     *            The table name to compile the query against.
     * @param columns
     *            A list of which columns to return. Passing null will return
     *            all columns, which is discouraged to prevent reading data from
     *            storage that isn't going to be used.
     * @return
     */
    public Query from(String table, String[] columns) {
        this.table = table;
        this.columns = columns;
        return this;
    }
    
    /**
     * @see Query#from(String table, String[] columns)
     * @param table
     * @return
     */
    public Query from(String table) {
        return from(table, null);
    }

    /**
     * Add WHERE
     * 
     * @param selection
     *            A filter declaring which rows to return, formatted as an SQL
     *            WHERE clause (excluding the WHERE itself). Passing null will
     *            return all rows for the given table.
     * @param selectionArgs
     *            You may include ?s in selection, which will be replaced by the
     *            values from selectionArgs, in order that they appear in the
     *            selection. The values will be bound as Strings.
     * @return
     */
    public Query where(String selection, String[] selectionArgs) {
        select.add(selection);
        binds.addAll(Arrays.asList(selectionArgs));
        return this;
    }
    
    /**
     * @see Query#where(String selection, String[] selectionArgs)
     */
    public Query where(String selection, String selectionArg) {
        select.add(selection);
        binds.add(selectionArg);
        return this;
    }
    
    /**
     * @see Query#where(String selection, String[] selectionArgs)
     */
    public Query where(String selection) {
        select.add(selection);
        return this;
    }
    
    /**
     * set HAVING
     * 
     * @param having
     *            A filter declare which row groups to include in the cursor, if
     *            row grouping is being used, formatted as an SQL HAVING clause
     *            (excluding the HAVING itself). Passing null will cause all row
     *            groups to be included, and is required when row grouping is
     *            not being used.
     * @return
     */
    public Query having(String having) {
        this.having = having;
        return this;
    }

    /**
     * Set GROUP BY
     * 
     * @param groupBy
     *            A filter declaring how to group rows, formatted as an SQL
     *            GROUP BY clause (excluding the GROUP BY itself). Passing null
     *            will cause the rows to not be grouped.
     * @return
     */
    public Query groupBy(String groupBy) {
        this.groupBy = groupBy;
        return this;
    }

    /**
     * Set ORDER BY
     * 
     * @param orderBy
     *            How to order the rows, formatted as an SQL ORDER BY clause
     *            (excluding the ORDER BY itself). Passing null will use the
     *            default sort order, which may be unordered.
     * @return
     */
    public Query orderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    /**
     * @param limit
     *            Limits the number of rows returned by the query, formatted as
     *            LIMIT clause. Passing null denotes no LIMIT clause.
     * @return
     */
    public Query limit(String limit) {
        this.limit = limit;
        return this;
    }

    /**
     * @see Query#limit(String limit)
     */
    public Query limit(int limit) {
        return limit(limit + "");
    }
    
    /**
     * Build selection
     */
    private void buildQuery() {
        StringBuilder sb = new StringBuilder(); 
        for (int i = 0, l = select.size(); i < l; i++) {
            sb.append( ((0 == i) ? "" : " AND ") + select.get(i));
        }
        selection = sb.toString();
        
        selectionArgs = new String[binds.size()];
        binds.toArray(selectionArgs);
        
        if (debug) {
            Log.d(TAG, toString());
        }
    }
    
    private boolean preCheck() {
        return (table != null);
    }
    
    public void setDb(SQLiteDatabase db) {
        this.db = db;
    }

    @Override
    public String toString() {
        return "Query [table=" + table + ", columns="
                + Arrays.toString(columns) + ", selection=" + selection
                + ", selectionArgs=" + Arrays.toString(selectionArgs)
                + ", groupBy=" + groupBy + ", having=" + having + ", orderBy="
                + orderBy + "]";
    }

}
