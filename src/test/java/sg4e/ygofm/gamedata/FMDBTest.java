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
package sg4e.ygofm.gamedata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author sg4e
 */
public class FMDBTest {
    
    FMDB db;
    
    public FMDBTest() {
    }
    
    @BeforeEach
    public void init() {
        db = FMDB.getInstance();
    }
    
    @Test
    public void testGetAllCards() {
        Map<Integer,Card> cardMap = db.getAllCards().stream().collect(Collectors.toMap(Card::id, Function.identity()));
        //test some arbitrary cards
        assertEquals("Mystical Sand", cardMap.get(531).name());
        assertEquals("Nekogal #2", cardMap.get(627).name());
        assertEquals("Nemuriko", cardMap.get(129).name());
    }
    
    @Test
    public void checkBlueEyes() {
        Card c = db.getCard(1);
        assertEquals("Blue-eyes White Dragon", c.name());
        assertEquals(1, c.id());
        assertEquals(8, c.level());
        assertEquals(3000, c.attack());
        assertEquals(2500, c.defense());
        assertEquals(999_999, c.starchips());
        assertEquals(GuardianStar.SUN, c.firstGuardianStar());
        assertEquals(GuardianStar.MARS, c.secondGuardianStar());
        assertEquals("Dragon", c.type());
        assertEquals("Light", c.attribute());
        assertEquals("89631139", c.password());
        assertEquals(74, c.abcSort());
        assertEquals(7, c.maxSort());
        assertEquals(7, c.atkSort());
        assertEquals(11, c.defSort());
        assertEquals(6, c.typeSort());
        assertEquals(5, c.aiSort());
        assertEquals(524, c.jpAbcSort());
        assertEquals(7, c.jpMaxSort());
        assertEquals(7, c.jpAtkSort());
        assertEquals(11, c.jpDefSort());
        assertEquals(18, c.jpTypeSort());
    }
    
    @Test
    public void testLoadedCardsAreEquals() {
        Card one = db.getCard(123);
        Card same = db.getCard(123);
        assertEquals(one, same);
    }
    
    @Test
    public void testSuccessfulFusion() {
        Card spikeSeadra = db.getCard(448);
        Card rightArmOfTheForbiddenOne = db.getCard(19);
        Card kaminariAttack = db.fuse(spikeSeadra, rightArmOfTheForbiddenOne);
        assertEquals(458, kaminariAttack.id());
    }
    
    @Test
    public void testFailedFusion() {
        Card mysticalELf = db.getCard(2);
        Card bewd = db.getCard(1);
        Card unfused = db.fuse(mysticalELf, bewd);
        assertEquals(bewd, unfused);
    }

    @Test
    public void testFuseOrNull() {
        Card mysticalELf = db.getCard(2);
        Card bewd = db.getCard(1);
        Card unfused = db.fuseOrNull(mysticalELf, bewd);
        assertNull(unfused);
    }
    
    @Test
    public void testInvertedFusion() {
        Card spikeSeadra = db.getCard(448);
        Card rightArmOfTheForbiddenOne = db.getCard(19);
        Card kaminariAttack = db.fuse(spikeSeadra, rightArmOfTheForbiddenOne);
        assertEquals(458, kaminariAttack.id());
        Card sameKaminariAttack = db.fuse(rightArmOfTheForbiddenOne, spikeSeadra);
        assertEquals(kaminariAttack, sameKaminariAttack);
    }
    
    @Test
    public void testSelfFusion() {
        Card thunderDragon = db.getCard(425);
        Card thtd = db.getCard(613);
        assertEquals(thtd, db.fuse(thunderDragon, thunderDragon));
    }
    
    @Test
    public void testIsEquippableTrue() {
        Card thtd = db.getCard(613);
        Card dt = db.getCard(315);
        assertTrue(db.isEquippable(thtd, dt));
    }
    
    @Test
    public void testIsEquippableFalse() {
        Card thtd = db.getCard(613);
        Card electrowhip = db.getCard(316);
        assertFalse(db.isEquippable(thtd, electrowhip));
    }
    
    @Test
    public void testIsEquippableThrowsException() {
        Card thtd = db.getCard(613);
        Card dt = db.getCard(315);
        assertThrows(IllegalArgumentException.class, () -> db.isEquippable(dt, thtd));
    }

    @Test
    public void testGetAllDuelists() {
        assertEquals(Arrays.stream(Duelist.Name.values()).collect(Collectors.toSet()),
                db.getAllDuelists().stream().map(Duelist::getName).collect(Collectors.toSet()));
    }
    
    @Test
    public void testDuelistDeckPool() {
        Duelist seto3 = db.getDuelist(Duelist.Name.SETO_3);
        Pool.Entry beud = seto3.getPool(Pool.Type.DECK).getEntry(380);
        assertEquals(380, beud.card().id());
        assertEquals(80, beud.probability());
    }
    
    @Test
    public void testDuelistSATec() {
        Duelist pegasus = db.getDuelist(Duelist.Name.PEGASUS);
        Pool.Entry megamorph = pegasus.getPool(Pool.Type.SA_TEC).getEntry(657);
        assertEquals(657, megamorph.card().id());
        assertEquals(64, megamorph.probability());
    }
    
    @Test
    public void testDuelistSAPow() {
        Duelist meadow = db.getDuelist(Duelist.Name.MEADOW_MAGE);
        Pool.Entry mbd = meadow.getPool(Pool.Type.SA_POW).getEntry(713);
        assertEquals(713, mbd.card().id());
        assertEquals(20, mbd.probability());
    }
    
    @Test
    public void testDuelistBCD() {
        Duelist v2 = db.getDuelist(Duelist.Name.VILLAGER_2);
        Pool.Entry dragonZombie = v2.getPool(Pool.Type.BCD).getEntry(97);
        assertEquals(97, dragonZombie.card().id());
        assertEquals(26, dragonZombie.probability());
    }
    
    @Test
    public void testDeckDrop() {
        Duelist seto3 = db.getDuelist(Duelist.Name.SETO_3);
        Card bewd = seto3.getPool(Pool.Type.DECK).getDrop(119);
        Card summonedSkull = seto3.getPool(Pool.Type.DECK).getDrop(120);
        assertEquals(1, bewd.id());
        assertEquals(22, summonedSkull.id());
    }
    
}
