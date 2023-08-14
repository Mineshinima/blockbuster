package com.mineshinima.mclib.client.ui.unit;

public class Unit extends RelativeUnit {
    public Unit(float value) {
        super(value);
    }

    public Unit(int value) {
        super(value);
    }

    public float getValue() {
        return this.type == UnitType.AUTO ? 0F : this.value;
    }

    public void setAuto() {
        this.type = UnitType.AUTO;
    }
}
