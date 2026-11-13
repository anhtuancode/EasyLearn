package com.studying.easylearn.data.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "vocabularies",
        indices = {@Index(value = {"word"}, unique = true)}
)
public class Vocabulary {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "word")
    private String word; // Chữ thường (lowercase)

    @ColumnInfo(name = "phonetic")
    private String phonetic; // Phiên âm IPA

    @ColumnInfo(name = "meaning")
    private String meaning; // Nghĩa tiếng Việt

    @ColumnInfo(name = "example")
    private String example; // Ví dụ minh họa

    public Vocabulary(String word, String phonetic, String meaning, String example) {
        this.word = word;
        this.phonetic = phonetic;
        this.meaning = meaning;
        this.example = example;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }
    public String getPhonetic() { return phonetic; }
    public void setPhonetic(String phonetic) { this.phonetic = phonetic; }
    public String getMeaning() { return meaning; }
    public void setMeaning(String meaning) { this.meaning = meaning; }
    public String getExample() { return example; }
    public void setExample(String example) { this.example = example; }
}