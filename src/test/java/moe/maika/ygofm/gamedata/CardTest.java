/*
 * The MIT License
 *
 * Copyright 2024 sg4e.
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
package moe.maika.ygofm.gamedata;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author sg4e
 */
public class CardTest {
    FMDB db;
    Card mysticalSand;
    Card electroWhip;
    Card dt;
    Card babyDragon;
    Card boltPenguin;
    Card thunderDragon;
    Card thtd;
    
    public CardTest() {
    }
    
    @BeforeEach
    public void init() {
        db = FMDB.getInstance();
        mysticalSand = db.getCard(531);
        electroWhip = db.getCard(316);
        dt = db.getCard(315);
        babyDragon = db.getCard(4);
        boltPenguin = db.getCard(461);
        thunderDragon = db.getCard(425);
        thtd = db.getCard(613);
    }

    @Test
    public void testEquipsTrue() {
        assertTrue(electroWhip.equips(mysticalSand));
    }

    @Test
    public void testEquipsFalse() {
        assertFalse(dt.equips(mysticalSand));
    }

    @Test
    public void testMonsterCardIsEquipException() {
        assertThrows(IllegalArgumentException.class, () -> {
            mysticalSand.equips(electroWhip);
        });
    }

    @Test
    public void testEquipCardIsNotEquipException() {
        assertThrows(IllegalArgumentException.class, () -> {
            electroWhip.equips(dt);
        });
    }

    @Test
    public void testEquipNullException() {
        assertThrows(IllegalArgumentException.class, () -> {
            electroWhip.equips(null);
        });
    }

    @Test
    public void testCanBeEquippedWithTrue() {
        assertTrue(mysticalSand.canBeEquippedWith(electroWhip));
    }

    @Test
    public void testCanBeEquippedWithFalse() {
        assertFalse(mysticalSand.canBeEquippedWith(dt));
    }

    @Test
    public void testCanBeEquippedWithMonsterCardIsEquipException() {
        assertThrows(IllegalArgumentException.class, () -> {
            mysticalSand.canBeEquippedWith(mysticalSand);
        });
    }

    @Test
    public void testCanBeEquippedWithEquipCardIsNotEquipException() {
        assertThrows(IllegalArgumentException.class, () -> {
            dt.canBeEquippedWith(dt);
        });
    }

    @Test
    public void testCanBeEquippedWithNullException() {
        assertThrows(IllegalArgumentException.class, () -> {
            mysticalSand.canBeEquippedWith(null);
        });
    }

    @Test
    public void testFuseWith() {
        assertEquals(thunderDragon, babyDragon.fuseWith(boltPenguin));
    }

    @Test
    public void testFuseBackwards() {
        assertEquals(thunderDragon, boltPenguin.fuseWith(babyDragon));
    }

    @Test
    public void testFuseWithNullException() {
        assertThrows(IllegalArgumentException.class, () -> {
            babyDragon.fuseWith(null);
        });
    }

    @Test
    public void testNoFusion() {
        assertEquals(babyDragon, babyDragon.fuseWith(babyDragon));
    }

    @Test
    public void testTripleFusion() {
        assertEquals(thtd, boltPenguin.fuseWith(babyDragon).fuseWith(boltPenguin));
    }

    @Test
    public void testFuseOrNull() {
        assertEquals(thunderDragon, babyDragon.fuseOrNull(boltPenguin));
    }

    @Test
    public void testFuseOrNullBackwards() {
        assertEquals(thunderDragon, boltPenguin.fuseOrNull(babyDragon));
    }

    @Test
    public void testFuseOrNullNoFusion() {
        assertNull(babyDragon.fuseOrNull(babyDragon));
    }

}
