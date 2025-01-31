package edu.brandeis.cosi103a.groupb;

public class AutomationCard extends Card {
    private int value;

    public AutomationCard(String name, int cost, int value) {
        super(name, cost);
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}