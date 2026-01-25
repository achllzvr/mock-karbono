package com.achllzvr.mockkarbono.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.achllzvr.mockkarbono.db.entities.CarbonReference;
import java.util.List;

@Dao
public interface CarbonReferenceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CarbonReference> references);

    @Query("SELECT * FROM carbon_references WHERE packageName = :pkgName LIMIT 1")
    CarbonReference getByPackage(String pkgName);

    @Query("SELECT * FROM carbon_references")
    List<CarbonReference> getAll();
}