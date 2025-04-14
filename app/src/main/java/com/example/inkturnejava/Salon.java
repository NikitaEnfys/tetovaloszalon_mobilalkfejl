package com.example.inkturnejava;

import java.io.Serializable;

public class Salon implements Serializable {
    private String name;
    private String address;
    private String description;
    private String openingHours;
    private String style;

    public Salon() {
    }

    public Salon(String name, String address, String description, String openingHours, String style) {
        this.name = name;
        this.address = address;
        this.description = description;
        this.openingHours = openingHours;
        this.style = style;
    }

    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getDescription() { return description; }
    public String getOpeningHours() { return openingHours; }
    public String getStyle() { return style; }
}
