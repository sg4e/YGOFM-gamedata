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

import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *
 * @author sg4e
 */
public class Duelist {
    
    private final Name name;
    private final Map<Pool.Type, Pool> pools;
    
    Duelist(Name name) {
        this.name = name;
        pools = new HashMap<>();
    }
    
    void addPool(Pool.Type type, Pool pool) {
        pools.put(type, pool);
    }
    
    public int getHandSize() {
        return name.getHandSize();
    }
    
    public int getId() {
        return name.getId();
    }
    
    public boolean isMage() {
        return name.isMage();
    }
    
    public Name getName() {
        return name;
    }
    
    public Pool getPool(Pool.Type type) {
        return pools.get(type);
    }

    @Override
    public String toString() {
        return getName().toString();
    }
    
    @Getter(AccessLevel.PRIVATE)
    @AllArgsConstructor
    public static enum Name {
        SIMON(1, 5, "Simon Muran"),
        TEANA_1(2, 5, "Teana"),
        JONO_1(3, 5, "Jono"),
        VILLAGER_1(4, 5, "Villager1"),
        VILLAGER_2(5, 5, "Villager2"),
        VILLAGER_3(6, 5, "Villager3"),
        SETO_1(7, 10, "Seto"),
        HEISHIN_1(8, 20, "Heishin"),
        REX(9, 8, "Rex Raptor"),
        WEEVIL(10, 8, "Weevil Underwood"),
        MAI(11, 10, "Mai Valentine"),
        BANDIT_KEITH(12, 12, "Bandit Keith"),
        SHADI(13, 12, "Shadi"),
        BAKURA(14, 14, "Yami Bakura"),
        PEGASUS(15, 16, "Pegasus"),
        ISIS(16, 16, "Isis"),
        KAIBA(17, 16, "Kaiba"),
        MAGE_SOLDIER(18, 12, "Mage Soldier"),
        JONO_2(19, 10, "Jono 2nd"),
        TEANA_2(20, 10, "Teana 2nd"),
        OCEAN_MAGE(21, 14, "Ocean Mage", true),
        SECMETON(22, 16, "High Mage Secmeton"),
        FOREST_MAGE(23, 14, "Forest Mage", true),
        ANUBISIUS(24, 16, "High Mage Anubisius"),
        MOUNTAIN_MAGE(25, 14, "Mountain Mage", true),
        ATENZA(26, 16, "High Mage Atenza"),
        DESERT_MAGE(27, 14, "Desert Mage", true),
        MARTIS(28, 16, "High Mage Martis"),
        MEADOW_MAGE(29, 14, "Meadow Mage", true),
        KEPURA(30, 16, "High Mage Kepura"),
        LABYRINTH_MAGE(31, 16, "Labyrinth Mage"),
        SETO_2(32, 18, "Seto 2nd"),
        SEBEK(33, 20, "Guardian Sebek"),
        NEKU(34, 20, "Guardian Neku", true),
        HEISHIN_2(35, 20, "Heishin 2nd"),
        SETO_3(36, 20, "Seto 3rd"),
        DARKNITE(37, 20, "DarkNite"),
        NITEMARE(38, 20, "Nitemare"),
        DUEL_MASTER_K(39, 15, "Duel Master K");
        
        private final int id, handSize;
        private final String pretty;
        private final boolean mage;
        
        private Name(int id, int handSize, String pretty) {
            this(id, handSize, pretty, false);
        }

        @Override
        public String toString() {
            return getPretty();
        }
    }
    
}
