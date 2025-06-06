package com.th.scala.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "schedule_db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DbContract.MachineTable.SQL_CREATE_TABLE);
        db.execSQL(DbContract.EmployeeTable.SQL_CREATE_TABLE);
        db.execSQL(DbContract.ProductTable.SQL_CREATE_TABLE);
        db.execSQL(DbContract.ScheduleTable.SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Implementar atualização do banco de dados se necessário
    }
}