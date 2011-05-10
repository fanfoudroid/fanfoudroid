package com.ch_linghu.fanfoudroid.db.dao;

import java.util.ArrayList;
import java.util.Arrays;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * Wrapper of SQliteDatabse#query, OOP style.
 * 
 * Usage:
 * ------------------------------------------------
 * Query select = new Query(SQLiteDatabase);
 * 
 * // SELECT
 * query.from("tableName", new String[] { "colName" })
 *      .where("id = ?", 123456)
 *      .where("name = ?", "jack")
 *      .orderBy("created_at DESC")
 *      .limit(1);
 * Cursor cursor = query.select();
 * 
 * // DELETE
 * query.from("tableName")
 *      .where("id = ?", 123455);
 *      .delete();
 * 
 * // UPDATE
 * query.setTable("tableName")
 *      .values(contentValues)
 *      .update();
 * 
 * // INSERT
 * query.into("tableName")
 *      .values(contentValues)
 *      .insert();
 * ------------------------------------------------
 * 
 * @see SQLiteDatabase#query(String, String[], String, String[], String, String, String, String)
 */
public class Query
{
    private static final String TAG = "Query-Builder";
    
    /** TEMP list for selctionArgs */
    private ArrayList<String> binds = new ArrayList<String>();
    private SQLiteDatabase mDb = null;
    
    private String mTable;
    private String[] mColumns;
    private String mSelection = null;
    private String[] mSelectionArgs = null;
    private String mGroupBy = null;
    private String mHaving = null;
    private String mOrderBy = null;
    private String mLimit = null;
    
    private ContentValues mValues = null;
    private String mNullColumnHack = null;
    
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
     * @param db SQLitedatabase
     * @return A Cursor object, which is positioned before the first entry, or NULL
     */
    public Cursor select() {
        if ( preCheck() ) {
            buildQuery();
            return mDb.query(mTable, mColumns, mSelection, mSelectionArgs,
                        mGroupBy, mHaving, mOrderBy, mLimit);
        } else {
            //throw new SelectException("Cann't build the query . " + toString());
            Log.e(TAG, "Cann't build the query " + toString());
            return null;
        }
    }
    
    /**
     * @return the number of rows affected if a whereClause is passed in, 0
     *         otherwise. To remove all rows and get a count pass "1" as the
     *         whereClause.
     */
    public int delete() {
        if ( preCheck() ) {
            buildQuery();
            return mDb.delete(mTable, mSelection, mSelectionArgs);
        } else {
            Log.e(TAG, "Cann't build the query " + toString());
            return -1;
        }
    }

    
    /**
     * Set FROM
     * 
     * @param table
     *            The table name to compile the query against.
     * @param columns
     *            A list of which columns to return. Passing null will return
     *            all columns, which is discouraged to prevent reading data from
     *            storage that isn't going to be used.
     * @return self
     * 
     */
    public Query from(String table, String[] columns) {
        mTable = table;
        mColumns = columns;
        return this;
    }
    
    /**
     * @see Query#from(String table, String[] columns)
     * @param table
     * @return self
     */
    public Query from(String table) {
        return from(table, null); // all columns
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
     * @return self
     */
    public Query where(String selection, String[] selectionArgs) {
        addSelection(selection);
        binds.addAll(Arrays.asList(selectionArgs));
        return this;
    }
    
    /**
     * @see Query#where(String selection, String[] selectionArgs)
     */
    public Query where(String selection, String selectionArg) {
        addSelection(selection);
        binds.add(selectionArg);
        return this;
    }
    
    /**
     * @see Query#where(String selection, String[] selectionArgs)
     */
    public Query where(String selection) {
        addSelection(selection);
        return this;
    }
    
    /**
     * add selection part
     * 
     * @param selection
     */
    private void addSelection(String selection) {
        if (null == mSelection) {
            mSelection = selection;
        } else {
            mSelection += " AND " + selection;
        }
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
     * @return self
     */
    public Query having(String having) {
        this.mHaving = having;
        return this;
    }

    /**
     * Set GROUP BY
     * 
     * @param groupBy
     *            A filter declaring how to group rows, formatted as an SQL
     *            GROUP BY clause (excluding the GROUP BY itself). Passing null
     *            will cause the rows to not be grouped.
     * @return self
     */
    public Query groupBy(String groupBy) {
        this.mGroupBy = groupBy;
        return this;
    }

    /**
     * Set ORDER BY
     * 
     * @param orderBy
     *            How to order the rows, formatted as an SQL ORDER BY clause
     *            (excluding the ORDER BY itself). Passing null will use the
     *            default sort order, which may be unordered.
     * @return self
     */
    public Query orderBy(String orderBy) {
        this.mOrderBy = orderBy;
        return this;
    }

    /**
     * @param limit
     *            Limits the number of rows returned by the query, formatted as
     *            LIMIT clause. Passing null denotes no LIMIT clause.
     * @return self
     */
    public Query limit(String limit) {
        this.mLimit = limit;
        return this;
    }

    /**
     * @see Query#limit(String limit)
     */
    public Query limit(int limit) {
        return limit(limit + "");
    }
    
    /**
     * Merge selectionArgs
     */
    private void buildQuery() {
        mSelectionArgs = new String[binds.size()];
        binds.toArray(mSelectionArgs);
        
        Log.v(TAG, toString()); 
    }
    
    private boolean preCheck() {
        return (mTable != null && mDb != null);
    }
    
    // For Insert
    
    /**
     * set insert table
     * 
     * @param table table name
     * @return self
     */
    public Query into(String table) {
        return setTable(table);
    }
    
    /**
     * Set new values
     * 
     * @param values new values
     * @return self
     */
    public Query values(ContentValues values) {
        mValues = values;
        return this;
    }
    
    /**
     * Insert a row
     * 
     * @return the row ID of the newly inserted row, or -1 if an error occurred
     */
    public long insert() {
        return mDb.insert(mTable, mNullColumnHack, mValues);
    }
    
    // For update
    
    /**
     * Set target table
     * 
     * @param table table name
     * @return self
     */
    public Query setTable(String table) {
        mTable = table;
        return this;
    }
    
    /**
     * Update a row
     * 
     * @return the number of rows affected, or -1 if an error occurred
     */
    public int update() {
        if ( preCheck() ) {
            buildQuery();
            return mDb.update(mTable, mValues, mSelection, mSelectionArgs);
        } else {
            Log.e(TAG, "Cann't build the query " + toString());
            return -1;
        }
    }
    
    /**
     * Set back-end database
     * @param db
     */
    public void setDb(SQLiteDatabase db) {
        if (null == this.mDb) {
            this.mDb = db;
        }
    }

    @Override
    public String toString() {
        return "Query [table=" + mTable + ", columns="
                + Arrays.toString(mColumns) + ", selection=" + mSelection
                + ", selectionArgs=" + Arrays.toString(mSelectionArgs)
                + ", groupBy=" + mGroupBy + ", having=" + mHaving + ", orderBy="
                + mOrderBy + "]";
    }
    
    /** for debug */
    public ContentValues getContentValues() {
        return mValues;
    }

}
