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

import com.hubspot.rosetta.jdbi3.RosettaRowMapperFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;

/**
 *
 * @author sg4e
 */
public class FMDB {
    
    private static final String SQLITE_PATH = "/fm-sqlite3.db";
    
    private final boolean loadDescriptions;
    private final Jdbi jdbi;
    private final Handle handle;
    private Map<Integer,Card> cardMap;
    private final Map<CardPair,Card> fusionCache;
    private final Map<Duelist.Name,Duelist> duelistMap;
    
    public static final String RITUAL_TYPE = "Ritual";
    public static final String TRAP_TYPE = "Trap";
    public static final String MAGIC_TYPE = "Magic";
    public static final String EQUIP_TYPE = "Equip";
    public static final Set<String> NON_MONSTER_TYPES = Collections.unmodifiableSet(Stream.of(
            RITUAL_TYPE,
            TRAP_TYPE,
            MAGIC_TYPE,
            EQUIP_TYPE
    ).collect(Collectors.toSet()));
    
    private FMDB(boolean loadDescriptions) {
        this.loadDescriptions = loadDescriptions;
        jdbi = Jdbi.create("jdbc:sqlite::resource:" + getClass().getResource(SQLITE_PATH));
        jdbi.registerRowMapper(new RosettaRowMapperFactory());
        handle = jdbi.open();
        cardMap = null;
        fusionCache = new HashMap<>();
        duelistMap = new HashMap<>();
    }
    
    public void close() {
        handle.close();
    }
    
    public Card getCard(int id) {
        if(cardMap == null)
            loadCards();
        return cardMap.get(id);
    }
    
    public Set<Card> getAllCards() {
        if(cardMap == null)
            loadCards();
        return new HashSet<>(cardMap.values());
    }
    
    private void loadCards() {
        String query = String.format("SELECT %s FROM cardinfo", loadDescriptions ? "*" : 
                "CardId, CardName, GuardianStar1, GuardianStar2, Level, Type, Attack, Defense, Attribute, Password, StarchipCost, "
                + "AbcSort, MaxSort, AtkSort, DefSort, TypeSort");
        cardMap = handle.createQuery(query)
                .mapTo(Card.class)
                .collect(Collectors.toMap(Card::getId, Function.identity()));
    }
    
    public Duelist getDuelist(Duelist.Name name) {
        return duelistMap.computeIfAbsent(name, n -> {
            Duelist d = new Duelist(name);
            //load pools
            Arrays.stream(Pool.Type.values()).forEach(poolType -> {
                List<Pool.Entry> poolEntries = handle.createQuery("SELECT CardId, CardProbability FROM droppool WHERE Duelist = :duelist AND PoolType = :type")
                        .bind("duelist", d.getId())
                        .bind("type", poolType.toString())
                        .map((rs, ctx) -> new Pool.Entry(getCard(rs.getInt("CardId")), rs.getInt("CardProbability")))
                        .list();
                d.addPool(poolType, new Pool(poolEntries));
            });
            return d;
        });
    }
    
    public Card fuse(Card firstCard, Card secondCard) {
        CardPair pair = new CardPair(firstCard, secondCard);
        return fusionCache.computeIfAbsent(pair, (i) -> {
            int firstCheck = lookupFusion(firstCard, secondCard);
            if(firstCheck == -1) {
                int inverseCheck = lookupFusion(secondCard, firstCard);
                if(inverseCheck == -1) {
                    return secondCard;
                }
                else {
                    return getCard(inverseCheck);
                }
            }
            else {
                return getCard(firstCheck);
            }
        });
    }
    
    public boolean isEquippable(Card monster, Card equip) {
        if(!"Equip".equals(equip.getType()))
            throw new IllegalArgumentException(equip.getName() + " is not an equip");
        return handle.createQuery("SELECT * FROM equipinfo WHERE EquipId = :equip AND CardId = :monster")
                .bind("equip", equip.getId())
                .bind("monster", monster.getId())
                .mapToMap()
                .findOne()
                .isPresent();
    }
    
    private int lookupFusion(Card firstCard, Card secondCard) {
        Optional<Integer> resultId = handle.createQuery("SELECT Result FROM fusions WHERE Material1 = :id1 AND Material2 = :id2")
                .bind("id1", firstCard.getId())
                .bind("id2", secondCard.getId())
                .mapTo(Integer.class)
                .findOne();
        return resultId.orElse(-1);
    }
    
    public static class Builder {
        private boolean desc = true;
        
        public Builder excludeDescrptions() {
            desc = false;
            return this;
        }
        
        public FMDB build() {
            return new FMDB(desc);
        }
    }
}
