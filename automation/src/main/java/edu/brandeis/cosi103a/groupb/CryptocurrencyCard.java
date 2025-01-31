package edu.brandeis.cosi103a.groupb;

public class CryptocurrencyCard extends Card {
    private int value;

    public CryptocurrencyCard(String name, int cost, int value) {
        super(name, cost);
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
