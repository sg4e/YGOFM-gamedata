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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author sg4e
 */
public class FMDB {
    
    private final Map<Integer,Card> cardMap;
    private final Map<Integer,Duelist> duelistMap;
    // first card id -> second card id -> result card
    private final Map<Integer,Map<Integer,Card>> fusionMap;
    private final Map<Integer,Set<Integer>> equipMap;
    
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

    private static FMDB singleton;
    
    private FMDB() {
        ObjectMapper jsonMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        jsonMapper.findAndRegisterModules();
        List<Card> cardList;
        List<JsonPool> rawPools;
        List<JsonFusion> rawFusions;
        List<JsonEquip> rawEquips;
        try {
            cardList = jsonMapper.readValue(getClass().getResourceAsStream("/cardinfo.json"),
                new TypeReference<List<Card>>(){});
            rawPools = jsonMapper.readValue(getClass().getResourceAsStream("/droppool.json"),
                new TypeReference<List<JsonPool>>(){});
            rawFusions = jsonMapper.readValue(getClass().getResourceAsStream("/fusions.json"),
                new TypeReference<List<JsonFusion>>(){});
            rawEquips = jsonMapper.readValue(getClass().getResourceAsStream("/equipinfo.json"),
                new TypeReference<List<JsonEquip>>(){});
        }
        catch(Exception e) {
            throw new RuntimeException("Failed to load FM database info from JSON files", e);
        }
        cardMap = cardList.stream().collect(Collectors.toMap(Card::id, Function.identity()));
        Map<Integer,List<JsonPool>> duelistIdToPools = rawPools.stream().collect(Collectors.groupingBy(JsonPool::duelist));
        Map<Integer,Duelist.Name> idToName = Arrays.stream(Duelist.Name.values()).collect(Collectors.toMap(Duelist.Name::getId, Function.identity()));
        duelistMap = duelistIdToPools.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                e -> new Duelist(idToName.get(e.getKey()), e.getValue().stream().collect(
                    Collectors.groupingBy(JsonPool::type)).entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                    e2 -> new Pool(e2.getValue().stream().map(p -> new Pool.Entry(cardMap.get(p.cardId()), p.probability())).collect(Collectors.toList())))))));
        fusionMap = rawFusions.stream().collect(Collectors.groupingBy(JsonFusion::material1, Collectors.toMap(JsonFusion::material2, i -> cardMap.get(i.result()))));
        equipMap = rawEquips.stream().collect(Collectors.groupingBy(JsonEquip::equipId, Collectors.mapping(JsonEquip::cardId, Collectors.toSet())));
    }
    
    public Card getCard(int id) {
        return cardMap.get(id);
    }
    
    public Set<Card> getAllCards() {
        return new HashSet<>(cardMap.values());
    }
    
    public Duelist getDuelist(Duelist.Name name) {
        return getDuelist(name.getId());
    }

    public Duelist getDuelist(int id) {
        return duelistMap.get(id);
    }

    private Card fuseSimple(int firstCardId, int secondCardId) {
        Map<Integer,Card> availableFusions = fusionMap.get(firstCardId);
        if(availableFusions == null)
            return null;
        return availableFusions.get(secondCardId);
    }

    private Card fuseImpl(int firstCardId, int secondCardId) {
        Card firstCheck = fuseSimple(firstCardId, secondCardId);
        if(firstCheck != null)
            return firstCheck;
        return fuseSimple(secondCardId, firstCardId);
    }

    /**
     * 
     * @param firstCard
     * @param secondCard
     * @return the result of fusing the two cards, or the second card if no fusion is possible
     */
    public Card fuse(Card firstCard, Card secondCard) {
        Card result = fuseImpl(firstCard.id(), secondCard.id());
        if(result == null)
            return secondCard;
        return result;
    }

    /**
     * 
     * @param firstCard
     * @param secondCard
     * @return the result of fusing the two cards, or null if no fusion is possible
     */
    public Card fuseOrNull(Card firstCard, Card secondCard) {
        return fuseImpl(firstCard.id(), secondCard.id());
    }
    
    /**
     * 
     * @param monster
     * @param equip
     * @return true if the monster can be equipped with the equip
     */
    public boolean isEquippable(Card monster, Card equip) {
        if(!EQUIP_TYPE.equals(equip.type()))
            throw new IllegalArgumentException(equip.name() + " is not an equip");
        if(NON_MONSTER_TYPES.contains(monster.type()))
            throw new IllegalArgumentException(monster.name() + " is not a monster");
        Set<Integer> equippableCards = equipMap.get(equip.id());
        if(equippableCards == null)
            return false;
        return equippableCards.contains(monster.id());
    }
    
    /**
     * 
     * @return the singleton instance of FMDB
     */
    public static synchronized FMDB getInstance() {
        if(singleton == null) {
            singleton = new FMDB();
        }
        return singleton;
    }
}
