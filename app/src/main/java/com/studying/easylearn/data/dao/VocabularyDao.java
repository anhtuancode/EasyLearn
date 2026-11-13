package com.studying.easylearn.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import com.studying.easylearn.data.entities.Vocabulary;
import java.util.List;

@Dao
public interface VocabularyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertVocabularies(List<Vocabulary> vocabularies);

    @Query("SELECT * FROM vocabularies WHERE word = :word LIMIT 1")
    Vocabulary findByWord(String word);

    @Query("SELECT meaning FROM vocabularies WHERE word != :excludeWord ORDER BY RANDOM() LIMIT 3")
    List<String> getRandomWrongMeanings(String excludeWord);
}