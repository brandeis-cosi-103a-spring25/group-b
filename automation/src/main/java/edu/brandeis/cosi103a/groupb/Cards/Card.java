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

    public enum Type {
        BITCOIN(1, Category.MONEY),
        ETHEREUM(2, Category.MONEY),
        DOGECOIN(3, Category.MONEY),
        METHOD(1, Category.VICTORY),
        MODULE(3, Category.VICTORY),
        FRAMEWORK(6, Category.VICTORY);

        private final int value;
        private final Category category;

        Type(int value, Category category) {
            this.value = value;
            this.category = category;
        }

        public int getValue() {
            return value;
        }

        public Category getCategory() {
            return category;
        }

        public enum Category {
            MONEY, VICTORY;
        }
    }
}
