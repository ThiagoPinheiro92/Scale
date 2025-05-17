package com.th.scala.database;

public class DbContract {
    public static class MachineTable {
        public static final String TABLE_NAME = "machines";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_DIFFICULTY = "difficulty";
        public static final String COLUMN_PRODUCT_ID = "product_id";
        public static final String COLUMN_SPECIAL_TRAINING = "special_training";

        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        COLUMN_NAME + " TEXT NOT NULL," +
                        COLUMN_DIFFICULTY + " INTEGER NOT NULL," +
                        COLUMN_PRODUCT_ID + " INTEGER," +
                        COLUMN_SPECIAL_TRAINING + " INTEGER DEFAULT 0," +
                        "FOREIGN KEY(" + COLUMN_PRODUCT_ID + ") REFERENCES " + 
                        ProductTable.TABLE_NAME + "(" + ProductTable.COLUMN_ID + "))";
    }

    public static class EmployeeTable {
        public static final String TABLE_NAME = "employees";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_IS_PCD = "is_pcd";
        public static final String COLUMN_MAX_DIFFICULTY = "max_difficulty";
        
        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        COLUMN_NAME + " TEXT NOT NULL," +
                        COLUMN_IS_PCD + " INTEGER DEFAULT 0," +
                        COLUMN_MAX_DIFFICULTY + " INTEGER DEFAULT 3)";
    }

    public static class EmployeeRestrictionsTable {
        public static final String TABLE_NAME = "employee_restrictions";
        public static final String COLUMN_EMPLOYEE_ID = "employee_id";
        public static final String COLUMN_PRODUCT_TYPE = "product_type";
        
        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_EMPLOYEE_ID + " INTEGER," +
                        COLUMN_PRODUCT_TYPE + " INTEGER," +
                        "PRIMARY KEY(" + COLUMN_EMPLOYEE_ID + "," + COLUMN_PRODUCT_TYPE + ")," +
                        "FOREIGN KEY(" + COLUMN_EMPLOYEE_ID + ") REFERENCES " + 
                        EmployeeTable.TABLE_NAME + "(" + EmployeeTable.COLUMN_ID + "))";
    }
}