package com.mineshinima.mclib.client.ui.unit;

public class AbsoluteUnit {
    protected float value;
    protected UnitType type;

    public AbsoluteUnit(int value) {
        this.setValue(value);
    }

    protected AbsoluteUnit() { }

    public int getValueInt() {
        return (int) this.value;
    }

    public void setValue(int value) {
        this.value = value;
        this.type = UnitType.PIXEL;
    }
}
