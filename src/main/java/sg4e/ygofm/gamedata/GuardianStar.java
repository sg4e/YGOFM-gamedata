/*
 * The MIT License
 *
 * Copyright 2020 sg4e.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package sg4e.ygofm.gamedata;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 *
 * @author sg4e
 */
public enum GuardianStar {
    SUN("Sun"),
    MOON("Moon"),
    VENUS("Venus"),
    MERCURY("Mercury"),
    
    MARS("Mars"),
    JUPITER("Jupiter"),
    SATURN("Saturn"),
    URANUS("Uranus"),
    PLUTO("Pluto"),
    NEPTUNE("Neptune");
    
    private GuardianStar strong, weak;
    private final String name;
    
    static {
        SUN.strong = MOON;
        SUN.weak = MERCURY;
        
        MOON.strong = VENUS;
        MOON.weak = SUN;
        
        VENUS.strong = MERCURY;
        VENUS.weak = MOON;
        
        MERCURY.strong = SUN;
        MERCURY.weak = VENUS;
        
        MARS.strong = JUPITER;
        MARS.weak = NEPTUNE;
        
        JUPITER.strong = SATURN;
        JUPITER.weak = MARS;
        
        SATURN.strong = URANUS;
        SATURN.weak = JUPITER;
        
        URANUS.strong = PLUTO;
        URANUS.weak = SATURN;
        
        PLUTO.strong = NEPTUNE;
        PLUTO.weak = URANUS;
        
        NEPTUNE.strong = MARS;
        NEPTUNE.weak = PLUTO;
    }
    
    private GuardianStar(String name) {
        this.name = name;
    }
    
    public GuardianStar getStrength() {
        return strong;
    }
    
    public GuardianStar getWeakness() {
        return weak;
    }
    
    public boolean isStrongAgainst(GuardianStar opponent) {
        return getStrength() == opponent;
    }
    
    public boolean isWeakAgainst(GuardianStar opponent) {
        return getWeakness() == opponent;
    }

    @Override
    @JsonValue
    public String toString() {
        return name;
    }
}
