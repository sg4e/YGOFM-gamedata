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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The entry point for querying FM game data. This class is a singleton and
 * should be accessed via the {@link #getInstance()} method.
 * <br><br>
 * Use this class to instantiate Card and Duelist objects.
 * <br><br>
 * This class is thread-safe. All classes in this library are thread-safe unless
 * otherwise noted, even if they do not explicitly state they are thread-safe.
 * @author sg4e
 */
public class FMDB {
    
    private final Map<Integer,Card> cardMap;
    private final Map<Integer,Duelist> duelistMap;
    // first card id -> second card id -> result card
    private final Map<Integer,Map<Integer,Card>> fusionMap;
    private final Map<Integer,Set<Integer>> equipMap;
    private final Map<Integer,Ritual> ritualMap;
    
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

    private static volatile FMDB singleton;
    
    private FMDB() {
        Card[] cardList = RawDatabase.getCard();
        RawDropPool[] rawPools = RawDatabase.getRawDropPool();
        RawFusion[] rawFusions = RawDatabase.getRawFusion();
        RawEquip[] rawEquips = RawDatabase.getRawEquip();
        cardMap = Arrays.stream(cardList).collect(Collectors.toMap(Card::getId, Function.identity()));
        Map<Integer,List<RawDropPool>> duelistIdToPools = Arrays.stream(rawPools).collect(Collectors.groupingBy(RawDropPool::getDuelist));
        Map<Integer,Duelist.Name> idToName = Arrays.stream(Duelist.Name.values()).collect(Collectors.toMap(Duelist.Name::getId, Function.identity()));
        duelistMap = duelistIdToPools.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                e -> new Duelist(idToName.get(e.getKey()), e.getValue().stream().collect(
                    Collectors.groupingBy(RawDropPool::getType)).entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                    e2 -> new Pool(e2.getValue().stream().map(p -> new Pool.Entry(cardMap.get(p.getCardId()), p.getProbability())).collect(Collectors.toList())))))));
        fusionMap = Arrays.stream(rawFusions).collect(Collectors.groupingBy(RawFusion::getMaterial1, Collectors.toMap(RawFusion::getMaterial2, i -> cardMap.get(i.getResult()))));
        equipMap = Arrays.stream(rawEquips).collect(Collectors.groupingBy(RawEquip::getEquipId, Collectors.mapping(RawEquip::getCardId, Collectors.toSet())));
        ritualMap = Arrays.stream(RawDatabase.getRawRitual()).collect(Collectors.toMap(Ritual::getRitualCardId, r -> new Ritual(r.getRitualCardId(), r.getMaterial1(), r.getMaterial2(), r.getMaterial3(), r.getResultId())));
        //rituals still need to initialize some state that relies on the FMDB instance having been set up
        //this is done in the getInstance() static method
    }
    
    /**
     * Gets a card by its id.
     * @param id a card id
     * @return the card with the given id, or null if no such card exists
     */
    public Card getCard(int id) {
        return cardMap.get(id);
    }
    
    /**
     * Gets all FM cards.
     * @return all cards in the game
     */
    public Set<Card> getAllCards() {
        return new HashSet<>(cardMap.values());
    }
    
    /**
     * Gets a duelist by name.
     * @param name a duelist name
     * @return the duelist with the given name, never null
     * @throws IllegalArgumentException if name is null
     */
    public Duelist getDuelist(Duelist.Name name) {
        if(name == null)
            throw new IllegalArgumentException("name cannot be null");
        return getDuelist(name.getId());
    }

    /**
     * Gets a duelist by id. Consider using {@link #getDuelist(Duelist.Name)} instead.
     * @param id the id of a duelist as specified by the FM database
     * @return the duelist with the given id, or null if no such duelist exists
     */
    public Duelist getDuelist(int id) {
        return duelistMap.get(id);
    }

    /**
     * Gets all FM duelists.
     * @return all duelists in the game
     */
    public Set<Duelist> getAllDuelists() {
        return new HashSet<>(duelistMap.values());
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
     * Performs a fusion or returns the second card if no fusion is possible. This
     * behavior mirrors the in-game mechanic when selecting two cards to fuse.
     * @param firstCard the first card of the fusion
     * @param secondCard the second card of the fusion
     * @return the result of fusing the two cards, or the second card if no fusion is possible,
     * never null
     * @throws IllegalArgumentException if either card is null
     */
    public Card fuse(Card firstCard, Card secondCard) {
        if(firstCard == null || secondCard == null)
            throw new IllegalArgumentException("Cards cannot be null");
        Card result = fuseImpl(firstCard.getId(), secondCard.getId());
        if(result == null)
            return secondCard;
        return result;
    }

    /**
     * Performs a fusion or returns null if no fusion is possible.
     * @param firstCard the first card of the fusion
     * @param secondCard the second card of the fusion
     * @return the result of fusing the two cards, or null if no fusion is possible
     */
    public Card fuseOrNull(Card firstCard, Card secondCard) {
        return fuseImpl(firstCard.getId(), secondCard.getId());
    }
    
    /**
     * Determines whether a monster can be equipped with an equip card.
     * Consider using {@link Card#equips(Card)} or {@link Card#canBeEquippedWith(Card)}
     * instead to avoid parameter-ordering errors.
     * @param monster the monster to equip
     * @param equip the equip card
     * @return true if the monster can be equipped with the equip
     * @throws IllegalArgumentException the first card is not a monster card,
     * or the second card is not an equip card, or either card is null
     */
    public boolean isEquippable(Card monster, Card equip) {
        if(monster == null || equip == null)
            throw new IllegalArgumentException("Cards cannot be null");
        if(!EQUIP_TYPE.equals(equip.getType()))
            throw new IllegalArgumentException(equip.getName() + " is not an equip");
        if(NON_MONSTER_TYPES.contains(monster.getType()))
            throw new IllegalArgumentException(monster.getName() + " is not a monster");
        Set<Integer> equippableCards = equipMap.get(equip.getId());
        if(equippableCards == null)
            return false;
        return equippableCards.contains(monster.getId());
    }

    /**
     * Returns the {@code Ritual} that is activated by the given ritual card id.
     * @param ritualCardId the id of the ritual card
     * @return the ritual, or null if {@code ritualCardId} is not a ritual card id
     */
    public Ritual getRitual(int ritualCardId) {
        return ritualMap.get(ritualCardId);
    }

    /**
     * Returns the {@code Ritual} that is activated by the given ritual card.
     * @param ritualCard the ritual card
     * @return the ritual, or null if {@code ritualCard} is not a ritual card
     * @throws IllegalArgumentException if {@code ritualCard} is null
     */
    public Ritual getRitual(Card ritualCard) {
        if(ritualCard == null)
            throw new IllegalArgumentException("Card cannot be null");
        return getRitual(ritualCard.getId());
    }

    /**
     * Returns all rituals in the game.
     * @return all rituals in the game
     */
    public Set<Ritual> getAllRituals() {
        return new HashSet<>(ritualMap.values());
    }
    
    /**
     * Gets the singleton instance of FMDB. If the database has not been loaded yet,
     * this method will load it.
     * <p>
     * This method can safely be called from any thread;
     * initialization is synchronized and will only occur once. Access after initialization
     * does not incur any synchronization overhead since the double-checked locking
     * optimization idiom is used internally.
     * @return the singleton instance of FMDB
     */
    public static FMDB getInstance() {
        FMDB localRef = singleton;
        if(localRef == null) {
            synchronized(FMDB.class) {
                localRef = singleton;
                if(localRef == null) {
                    FMDB initRituals = localRef = new FMDB();
                    localRef.ritualMap.values().forEach(r -> r.initializeReferences(initRituals));
                    singleton = localRef;
                }
            }
        }
        return localRef;
    }
}
