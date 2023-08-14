package com.mineshinima.mclib.client.ui.transformation;

import com.mineshinima.mclib.client.ui.UIPanelGrid;
import com.mineshinima.mclib.client.ui.unit.RelativeUnit;
import com.mineshinima.mclib.client.ui.unit.UnitType;

public class UIPanelGridTransformation extends UITransformation<UIPanelGrid> {
    public UIPanelGridTransformation(UIPanelGrid target) {
        super(target);
    }


    /**
     * Don't math.floor() the pixels. For panel grids the results will be rounded
     * to avoid borders getting thicker with a lot of subdivisions due to floor resulting in 1 pixel errors.
     * @param relative the value to use when the unit is in percentage.
     * @param unit the unit to calculate the pixels
     * @return
     */
    @Override
    protected int calculatePixels(int relative, RelativeUnit unit) {
        if (unit.getType() == UnitType.PERCENTAGE) {
            return (int) Math.round(relative * unit.getValue());
        } else {
            return (int) unit.getValue();
        }
    }
}
