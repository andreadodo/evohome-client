package com.jamierf.evohome.model;

public enum Units {
    CELSIUS('C') {
        public double toCelsius(final double value) {
            return value;
        }

        public double toFahrenheit(final double value) {
            return ((9F / 5F) * value) + 32F;
        }
    },
    FAHRENHEIT('F') {
        public double toCelsius(final double value) {
            return (5F / 9F) * (value - 32F);
        }

        public double toFahrenheit(final double value) {
            return value;
        }
    };

    private final char symbol;

    private Units(final char symbol) {
        this.symbol = symbol;
    }

    public double toCelsius(final double value) {
        throw new AbstractMethodError();
    }

    public double toFahrenheit(final double value) {
        throw new AbstractMethodError();
    }

    @Override
    public String toString() {
        return String.format("%s", symbol);
    }
}
