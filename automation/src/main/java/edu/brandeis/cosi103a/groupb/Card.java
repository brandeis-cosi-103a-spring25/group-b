package edu.brandeis.cosi103a.groupb;

public abstract class Card {
    private String name;
    private int cost;

    public Card(String name, int cost) {
        this.name = name;
        this.cost = cost;
    }

    public String getName() {
        return name;
    }

    public int getCost() {
        return cost;
    }
}
