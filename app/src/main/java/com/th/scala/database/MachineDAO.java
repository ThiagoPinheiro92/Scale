package com.th.scala.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.th.scala.models.Machine;

import java.util.ArrayList;
import java.util.List;

public class MachineDAO {
    private final SQLiteDatabase db;

    public MachineDAO(SQLiteDatabase db) {
        this.db = db;
    }

    public long insertMachine(Machine machine) {
        ContentValues values = new ContentValues();
        values.put(DbContract.MachineTable.COLUMN_NAME, machine.getName());
        values.put(DbContract.MachineTable.COLUMN_DIFFICULTY, machine.getDifficulty());
        values.put(DbContract.MachineTable.COLUMN_SPECIAL_TRAINING, machine.isRequiresSpecialTraining() ? 1 : 0);
        
        return db.insert(DbContract.MachineTable.TABLE_NAME, null, values);
    }

    public List<Machine> getAllMachines() {
        List<Machine> machines = new ArrayList<>();
        
        Cursor cursor = db.query(DbContract.MachineTable.TABLE_NAME,
                null, null, null, null, null, null);
        
        if (cursor.moveToFirst()) {
            do {
                Machine machine = new Machine(
                    cursor.getInt(cursor.getColumnIndexOrThrow(DbContract.MachineTable.COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DbContract.MachineTable.COLUMN_NAME)),
                    cursor.getInt(cursor.getColumnIndexOrThrow(DbContract.MachineTable.COLUMN_DIFFICULTY))
                );
                machine.setRequiresSpecialTraining(
                    cursor.getInt(cursor.getColumnIndexOrThrow(DbContract.MachineTable.COLUMN_SPECIAL_TRAINING)) == 1
                );
                machines.add(machine);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return machines;
    }
}