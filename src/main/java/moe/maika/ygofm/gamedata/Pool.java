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

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Implementation of drop pools.
 * @author sg4e
 */
public class Pool {

    private final SortedMap<Integer, Entry> entries;

    Pool(Collection<Entry> data) {
        entries = new TreeMap<>(data.stream().collect(Collectors.toMap(e -> e.getCard().getId(), Function.identity())));
    }

    /**
     * Returns the card dropped by the AI when using this pool for the given RNG value.
     * @param rand the value returned by FM's {@code rand()} function (before
     * modulo)
     * @return the card dropped, or the card added to the AI's deck if called on the Deck pool
     */
    public Card getDrop(int rand) {
        /*
        Quoted from GenericMadScientist in the FM discord:
        
        AI DECK GENERATION:
        Start with an empty array. To generate a new card, get a number from 0 to 2047 
        inclusive by doing rand() % 2048. Use this to select a card from the AI's deck pool. 
        For example, in Seto 3's deck pool, 0-119 is BEWD, 120-135 is Summoned Skull, 
        136-167 is Dark Magician, etc. (note this is in order of card ID). 
        If the deck has less than three copies of the generated card, add it. 
        Repeat until the deck has 40 cards.
        
        As an aside if you wish to extend this to mods, the card is automatically 
        skipped if it is 721 or 722, and if the pool doesn't add up to 2048 and you 
        'jump off' the end of the pool, the card is also automatically skipped. 
        This does not come up in vanilla.
         */
        rand = rand % 2048;
        for(Map.Entry<Integer,Entry> mapEntry : entries.entrySet()) {
            Entry e = mapEntry.getValue();
            rand -= e.getProbability();
            if(rand < 0)
                return e.getCard();
        }
        //returns null if bugged or if droppool is missing entries
        return null;
    }
    
    /**
     * Returns the card dropped by the AI when using this pool for the given RNG seed.
     * @param seed the RNG seed
     * @return the card dropped, or the card added to the AI's deck if called on the Deck pool
     */
    public Card getDrop(RNG seed) {
        return getDrop(seed.rand());
    }
    
    /**
     * Returns the {@link Entry} for the given card ID.
     * @param cardId
     * @return the entry for the given card ID, or null if not a valid id
     */
    public Entry getEntry(int cardId) {
        return entries.get(cardId);
    }
    
    /**
     * Returns the {@link Entry} for the given card.
     * @param card
     * @return the entry for the given card
     */
    public Entry getEntry(Card card) {
        return getEntry(card.getId());
    }

    /**
     * Returns all entries in this pool.
     * @return all entries in this pool
     */
    public Set<Entry> getAllEntries() {
        return new HashSet<>(entries.values());
    }

    /**
     * Returns all entries in this pool that match the given filter.
     * @param filter
     * @return all entries in this pool that match the given filter
     */
    public Set<Entry> getAllEntries(Predicate<Entry> filter) {
        return entries.values().stream().filter(filter).collect(Collectors.toSet());
    }

    /**
     * An immutable class representing a card and its probability of being dropped. The
     * probability is out of 2048.
     */
    public static class Entry {
        private final Card card;
        private final int probability;

        Entry(Card card, int probability) {
            this.card = card;
            this.probability = probability;
        }

        /**
         * Returns the card encapsulated by this entry.
         * @return the card
         */
        public Card getCard() {
            return card;
        }

        /**
         * Returns the probability of the card being dropped (out of 2048).
         * @return the probability of the card being dropped (out of 2048)
         */
        public int getProbability() {
            return probability;
        }
    }

    /**
     * The type of drop pool.
     */
    public static enum Type {

        /**
         * The pool of cards is sampled to create the AI's deck.
         */
        DECK("Deck"),
        /**
         * The pool of cards the generate the drop for an SA_POW duel rank.
         */
        SA_POW("SAPow"),
        /**
         * The pool of cards the generate the drop for a BCD duel rank.
         */
        BCD("BCD"),
        /**
         * The pool of cards the generate the drop for an SA_TEC duel rank.
         */
        SA_TEC("SATec");
        
        private final String name;

        private Type(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
