package com.example.jogodamemoria2;

public class MemoryCard {
    private int imageResource;
    private boolean isFlipped;
    private boolean isMatched;
    private int id;

    public MemoryCard(int imageResource, int id) {
        this.imageResource = imageResource;
        this.id = id;
        this.isFlipped = false;
        this.isMatched = false;
    }

    public int getImageResource() {
        return imageResource;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }

    public boolean isFlipped() {
        return isFlipped;
    }

    public void setFlipped(boolean flipped) {
        this.isFlipped = flipped;
    }

    public boolean isMatched() {
        return isMatched;
    }

    public void setMatched(boolean matched) {
        this.isMatched = matched;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void flip() {
        this.isFlipped = !this.isFlipped;
    }
}