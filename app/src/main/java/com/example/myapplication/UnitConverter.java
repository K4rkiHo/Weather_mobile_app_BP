package com.example.myapplication;

public class UnitConverter {

    public double convertValueToSavedUnit(double value, String originalUnit, String savedUnit) {
        // Pokud jsou jednotky stejné, není potřeba provádět žádný převod
        if (originalUnit.equals(savedUnit)) {
            return value;
        }

        // Převod mezi jednotkami teploty
        if (originalUnit.equals("°C")) {
            if (savedUnit.equals("°F")) {
                return (value * 9 / 5) + 32; // Převod z °C na °F
            }
        } else if (originalUnit.equals("°F")) {
            if (savedUnit.equals("°C")) {
                return (value - 32) * 5 / 9; // Převod z °F na °C
            }
        }

        // Převod mezi jednotkami tlaku
        if (originalUnit.equals("inHg")) {
            if (savedUnit.equals("hPa")) {
                return value * 33.863886666667; // Převod z inHg na hPa
            } else if (savedUnit.equals("mmHg")) {
                return value * 25.4; // Převod z inHg na mmHg
            } else if (savedUnit.equals("atm")) {
                return value * 0.0334211; // Převod z inHg na atm
            } else if (savedUnit.equals("mbar")) {
                return value * 33.863886666667; // Převod z inHg na mbar
            }
        } else if (originalUnit.equals("hPa")) {
            if (savedUnit.equals("inHg")) {
                return value / 33.863886666667; // Převod z hPa na inHg
            }
        } else if (originalUnit.equals("mmHg")) {
            if (savedUnit.equals("inHg")) {
                return value / 25.4; // Převod z mmHg na inHg
            }
        } else if (originalUnit.equals("atm")) {
            if (savedUnit.equals("inHg")) {
                return value / 0.0334211; // Převod z atm na inHg
            }
        } else if (originalUnit.equals("mbar")) {
            if (savedUnit.equals("inHg")) {
                return value / 33.863886666667; // Převod z mbar na inHg
            }
        }

        // Převod mezi jednotkami rychlosti větru
        if (originalUnit.equals("m/s")) {
            if (savedUnit.equals("km/h")) {
                return value * 3.6; // Převod z m/s na km/h
            } else if (savedUnit.equals("mph")) {
                return value * 2.23694; // Převod z m/s na mph
            } else if (savedUnit.equals("knot")) {
                return value * 1.94384; // Převod z m/s na knot
            }
        } else if (originalUnit.equals("km/h")) {
            if (savedUnit.equals("m/s")) {
                return value / 3.6; // Převod z km/h na m/s
            }
        } else if (originalUnit.equals("mph")) {
            if (savedUnit.equals("m/s")) {
                return value / 2.23694; // Převod z mph na m/s
            }
        } else if (originalUnit.equals("knot")) {
            if (savedUnit.equals("m/s")) {
                return value / 1.94384; // Převod z knot na m/s
            }
        }

        // Převod mezi jednotkami srážek
        if (originalUnit.equals("mm")) {
            if (savedUnit.equals("cm")) {
                return value / 10; // Převod z mm na cm
            } else if (savedUnit.equals("in")) {
                return value / 25.4; // Převod z mm na in
            }
        } else if (originalUnit.equals("cm")) {
            if (savedUnit.equals("mm")) {
                return value * 10; // Převod z cm na mm
            } else if (savedUnit.equals("in")) {
                return value / 2.54; // Převod z cm na in
            }
        } else if (originalUnit.equals("in")) {
            if (savedUnit.equals("mm")) {
                return value * 25.4; // Převod z in na mm
            } else if (savedUnit.equals("cm")) {
                return value * 2.54; // Převod z in na cm
            }
        }

        // Převod mezi jednotkami slunečního záření
        if (originalUnit.equals("W/m²")) {
            if (savedUnit.equals("lux")) {
                return value * 10000; // Převod z W/m² na lux
            }
        } else if (originalUnit.equals("lux")) {
            if (savedUnit.equals("W/m²")) {
                return value / 10000; // Převod z lux na W/m²
            }
        }

        // Pokud není definována žádná uložená jednotka nebo není žádný převod, vraťte původní hodnotu
        return value;
    }

}
