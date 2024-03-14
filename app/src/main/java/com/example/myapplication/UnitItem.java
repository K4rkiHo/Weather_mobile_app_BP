package com.example.myapplication;

public class UnitItem {
    private String unitsName;
    private String units;
    private boolean switchState;

    public UnitItem(String unitsName, String units, boolean switchState) {
        this.unitsName = unitsName;
        this.units = units;
        this.switchState = switchState;
    }

    public String getUnitsName() {
        return unitsName;
    }

    public String getUnits() {
        return units;
    }

    public boolean getSwitchState() {
        return switchState;
    }

    public void setSwitchState(boolean switchState) {
        this.switchState = switchState;
    }
}
