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
    
    @Getter(AccessLevel.PRIVATE)
    @AllArgsConstructor
    public static enum Name {
        BANDIT_KEITH(12, 12),
        DARKNITE(37, 20),
        DESERT_MAGE(27, 14, true),
        DUEL_MASTER_K(39, 15),
        FOREST_MAGE(23, 14, true),
        NEKU(34, 20, true),
        SEBEK(33, 20),
        HEISHIN_1(8, 20),
        HEISHIN_2(35, 20),
        ANUBISIUS(24, 16),
        ATENZA(26, 16),
        KEPURA(30, 16),
        MARTIS(28, 16),
        SECMETON(22, 16),
        ISIS(16, 16),
        JONO_1(3, 5),
        JONO_2(19, 10),
        KAIBA(17, 16),
        LABYRINTH_MAGE(31, 16),
        MAGE_SOLDIER(18, 12),
        MAI(11, 10),
        MEADOW_MAGE(29, 14, true),
        MOUNTAIN_MAGE(25, 14, true),
        NITEMARE(38, 20),
        OCEAN_MAGE(21, 14, true),
        PEGASUS(15, 16),
        REX(9, 8),
        SETO_1(7, 10),
        SETO_2(32, 18),
        SETO_3(36, 20),
        SHADI(13, 12),
        SIMON(1, 5),
        TEANA_1(2, 5),
        TEANA_2(20, 10),
        VILLAGER_1(4, 5),
        VILLAGER_2(5, 5),
        VILLAGER_3(6, 5),
        WEEVIL(10, 8),
        BAKURA(14, 14);
        
        private final int id, handSize;
        private final boolean mage;
        
        private Name(int id, int handSize) {
            this(id, handSize, false);
        }
    }
    
}
