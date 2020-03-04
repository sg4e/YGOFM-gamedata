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

import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 * @author sg4e
 */
public class Pool {

    private final SortedMap<Integer, Entry> entries;

    Pool(Collection<Entry> data) {
        entries = new TreeMap<>(data.stream().collect(Collectors.toMap(e -> e.getCard().getId(), Function.identity())));
    }

    /**
     *
     * @param rand the value returned by FM's {@code rand()} function (before
     * modulo)
     * @return
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
    
    public Card getDrop(RNG seed) {
        return getDrop(seed.rand());
    }
    
    public Entry getEntry(int cardId) {
        return entries.get(cardId);
    }
    
    public Entry getEntry(Card card) {
        return getEntry(card.getId());
    }

    @Data
    @AllArgsConstructor(access = AccessLevel.PACKAGE)
    public static class Entry {
        private final Card card;
        private final int probability; // probability out of 2048
    }

    @AllArgsConstructor
    public static enum Type {
        //@JsonProperty("Deck")
        DECK("Deck"),
        //@JsonProperty("SAPow")
        SA_POW("SAPow"),
        //@JsonProperty("BCD")
        BCD("BCD"),
        //@JsonProperty("SATec")
        SA_TEC("SATec");
        
        private final String name;

        @Override
        public String toString() {
            return name;
        }
    }
}
