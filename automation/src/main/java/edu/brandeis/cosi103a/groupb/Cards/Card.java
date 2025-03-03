package edu.brandeis.cosi103a.groupb.Cards;

public class Card {
    private final Type type;
    private final int id;

    public Card(Type type, int id) {
        this.type = type;
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return this.getType() + " " + this.getId();
    }

    public enum Type {
        BITCOIN(0, 1, Category.MONEY),
        ETHEREUM(3, 2, Category.MONEY),
        DOGECOIN(6, 3, Category.MONEY),
        METHOD(2, 1, Category.VICTORY),
        MODULE(5, 3, Category.VICTORY),
        FRAMEWORK(8, 6, Category.VICTORY);

        private final int cost;
        private final int value;
        private final Category category;

        Type(int cost, int value, Category category) {
            this.cost = cost;
            this.value = value;
            this.category = category;
        }

        public int getValue() {
            return value;
        }

        public int getCost() {
            return cost;
        }

        public Category getCategory() {
            return category;
        }

        public enum Category {
            MONEY,
            VICTORY;
        }
    }
    
}
