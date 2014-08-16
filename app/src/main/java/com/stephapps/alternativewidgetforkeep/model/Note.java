package com.stephapps.alternativewidgetforkeep.model;

/**
 * Created by Steph on 16/08/2014.
 */
public class Note {

    String noteText = "";
    int id = -1;
    String color = null;

    public Note(String id) {

    }

    public Note(int id, String note) {
        this.noteText = note;
        this.id = id;
    }

    public Note() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

}
