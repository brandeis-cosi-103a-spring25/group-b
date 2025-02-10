package edu.brandeis.cosi.atg.api.cards;

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

    public Category getCategory() {
        return type.getCategory();
    }

    public int getValue() {
        return type.getValue();
    }

    public int getCost() {
        return type.getCost();
    }

    public String getDescription() {
        return type.getDescription();
    }

    @Override
    public String toString() {
        return "Card{" +
                "type=" + type +
                ", id=" + id +
                ", description=" + getDescription() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Card card = (Card) o;

        if (id != card.id) return false;
        return type == card.type;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + id;
        return result;
    }

    public enum Type {
        BITCOIN(Category.MONEY, 1, 0, "A money card worth 1 money."),
        ETHEREUM(Category.MONEY, 2, 1, "A money card worth 2 money."),
        DOGECOIN(Category.MONEY, 3, 2, "A money card worth 3 money."),
        METHOD(Category.VICTORY, 0, 0, "A victory card worth 1 automation point."),
        MODULE(Category.VICTORY, 0, 0, "A victory card worth 3 automation points."),
        FRAMEWORK(Category.VICTORY, 0, 0, "A victory card worth 6 automation points.");

        private final Category category;
        private final int value;
        private final int cost;
        private final String description;

        Type(Category category, int value, int cost, String description) {
            this.category = category;
            this.value = value;
            this.cost = cost;
            this.description = description;
        }

        public Category getCategory() {
            return category;
        }

        public int getValue() {
            return value;
        }

        public int getCost() {
            return cost;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum Category {
        MONEY("Money"),
        VICTORY("Victory");

        private final String name;

        Category(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
