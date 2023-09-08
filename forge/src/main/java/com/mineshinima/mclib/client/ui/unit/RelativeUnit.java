package com.mineshinima.mclib.client.ui.unit;

public class RelativeUnit extends AbsoluteUnit {
    private int offset;

    public RelativeUnit(float value) {
        this.setValue(value);
    }

    public float getValue() {
        return this.value;
    }

    public int getValueInt() {
        if (this.type != UnitType.PIXEL) {
            return 0;
        }

        return super.getValueInt();
    }

    public int getOffset() {
        return this.offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setValue(float value) {
        this.value = value;
        this.type = UnitType.PERCENTAGE;
    }

    public UnitType getType() {
        return this.type;
    }
}

