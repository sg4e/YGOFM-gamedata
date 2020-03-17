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

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;

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
        db = new FMDB.Builder().build();
    }
    
    @AfterEach
    public void tearDown() {
        db.close();
    }

    @Test
    public void testLoadCard() {
        FMDB db2 = new FMDB.Builder().build();
        Card c = db2.getCard(1);
        assertTrue(c.getDescription().startsWith("An extremely rare"));
        checkBlueEyes(c);
        db2.close();
    }
    
    @Test
    public void testLoadCardWithoutDesciption() {
        FMDB db2 = new FMDB.Builder()
                .excludeDescrptions()
                .build();
        Card c = db2.getCard(1);
        assertNull(c.getDescription());
        checkBlueEyes(c);
        db2.close();
    }
    
    @Test
    public void testGetAllCards() {
        Map<Integer,Card> cardMap = db.getAllCards().stream().collect(Collectors.toMap(Card::getId, Function.identity()));
        //test some arbitrary cards
        assertEquals("Mystical Sand", cardMap.get(531).getName());
        assertEquals("Nekogal #2", cardMap.get(627).getName());
        assertEquals("Nemuriko", cardMap.get(129).getName());
    }
    
    private void checkBlueEyes(Card c) {
        assertEquals("Blue-eyes White Dragon", c.getName());
        assertEquals(1, c.getId());
        assertEquals(8, c.getLevel());
        assertEquals(3000, c.getAttack());
        assertEquals(2500, c.getDefense());
        assertEquals(999_999, c.getStarchips());
        assertEquals(GuardianStar.SUN, c.getFirstGuardianStar());
        assertEquals(GuardianStar.MARS, c.getSecondGuardianStar());
        assertEquals("Dragon", c.getType());
        assertEquals("Light", c.getAttribute());
        assertEquals("89631139", c.getPassword());
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
        assertEquals(458, kaminariAttack.getId());
    }
    
    @Test
    public void testFailedFusion() {
        Card mysticalELf = db.getCard(2);
        Card bewd = db.getCard(1);
        Card unfused = db.fuse(mysticalELf, bewd);
        assertEquals(bewd, unfused);
    }
    
    @Test
    public void testInvertedFusion() {
        Card spikeSeadra = db.getCard(448);
        Card rightArmOfTheForbiddenOne = db.getCard(19);
        Card kaminariAttack = db.fuse(spikeSeadra, rightArmOfTheForbiddenOne);
        assertEquals(458, kaminariAttack.getId());
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
    public void testDuelistDeckPool() {
        Duelist seto3 = db.getDuelist(Duelist.Name.SETO_3);
        Pool.Entry beud = seto3.getPool(Pool.Type.DECK).getEntry(380);
        assertEquals(380, beud.getCard().getId());
        assertEquals(80, beud.getProbability());
    }
    
    @Test
    public void testDuelistSATec() {
        Duelist pegasus = db.getDuelist(Duelist.Name.PEGASUS);
        Pool.Entry megamorph = pegasus.getPool(Pool.Type.SA_TEC).getEntry(657);
        assertEquals(657, megamorph.getCard().getId());
        assertEquals(64, megamorph.getProbability());
    }
    
    @Test
    public void testDuelistSAPow() {
        Duelist meadow = db.getDuelist(Duelist.Name.MEADOW_MAGE);
        Pool.Entry mbd = meadow.getPool(Pool.Type.SA_POW).getEntry(713);
        assertEquals(713, mbd.getCard().getId());
        assertEquals(20, mbd.getProbability());
    }
    
    @Test
    public void testDuelistBCD() {
        Duelist v2 = db.getDuelist(Duelist.Name.VILLAGER_2);
        Pool.Entry dragonZombie = v2.getPool(Pool.Type.BCD).getEntry(97);
        assertEquals(97, dragonZombie.getCard().getId());
        assertEquals(26, dragonZombie.getProbability());
    }
    
    @Test
    public void testDeckDrop() {
        Duelist seto3 = db.getDuelist(Duelist.Name.SETO_3);
        Card bewd = seto3.getPool(Pool.Type.DECK).getDrop(119);
        Card summonedSkull = seto3.getPool(Pool.Type.DECK).getDrop(120);
        assertEquals(1, bewd.getId());
        assertEquals(22, summonedSkull.getId());
    }
    
}
