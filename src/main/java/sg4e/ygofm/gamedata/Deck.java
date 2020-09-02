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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.AllArgsConstructor;

/**
 *
 * @author sg4e
 */
public class Deck {

    private final Card[] cards;

    public static final int DECK_SIZE = 40;
    /**
     * How many {@code rand()} calls to consider when searching for a seed. Value to be tuned.
     */
    static final int SEARCH_SPACE = 5_000_000;
    
    @AllArgsConstructor
    private static class ComparatorStringDecorator<T> implements Comparator<T> {
        
        private final Comparator<T> comparator;
        private final String string;

        @Override
        public int compare(T o1, T o2) {
            return comparator.compare(o1, o2);
        }

        @Override
        public String toString() {
            return string;
        }
        
    }
    
    public static final Comparator<Card> CARD_ID_ORDER = new ComparatorStringDecorator<>((c1, c2) -> c1.getId() - c2.getId(), "ID Sort");
    public static final Comparator<Card> ALPHABETICAL_ORDER = new ComparatorStringDecorator<>((c1, c2) -> c1.getAbcSort() - c2.getAbcSort(), "Alphabetical Sort");
    public static final Comparator<Card> MAX_ORDER = new ComparatorStringDecorator<>((c1, c2) -> c1.getMaxSort() - c2.getMaxSort(), "Max Sort");
    public static final Comparator<Card> ATTACK_ORDER = new ComparatorStringDecorator<>((c1, c2) -> c1.getAtkSort() - c2.getAtkSort(), "Attack Sort");
    public static final Comparator<Card> DEFENSE_ORDER = new ComparatorStringDecorator<>((c1, c2) -> c1.getDefSort() - c2.getDefSort(), "Defense Sort");
    public static final Comparator<Card> TYPE_ORDER = new ComparatorStringDecorator<>((c1, c2) -> c1.getTypeSort() - c2.getTypeSort(), "Type Sort");
    private static final List<Comparator<Card>> SORTS = new ArrayList<>(6);
    static {
        SORTS.add(CARD_ID_ORDER);
        SORTS.add(ALPHABETICAL_ORDER);
        SORTS.add(MAX_ORDER);
        SORTS.add(ATTACK_ORDER);
        SORTS.add(DEFENSE_ORDER);
        SORTS.add(TYPE_ORDER);
    }

    public Deck() {
        cards = new Card[DECK_SIZE];
    }
    
    public Deck(Collection<Card> composition) {
        this();
        if(composition.size() != DECK_SIZE)
            throw new IllegalArgumentException(String.format("Deck must contain %s cards; given %s instead", DECK_SIZE, composition.size()));
        composition.toArray(cards);
    }
    
    public Deck(Deck toCopy) {
        cards = Arrays.copyOf(toCopy.cards, DECK_SIZE);
    }
    
    public Card get(int index) {
        if(index > DECK_SIZE) 
            throw new IllegalArgumentException(String.format("Index %s exceeds deck size %s", index, DECK_SIZE));
        return cards[index];
    }
    
    /**
     * 
     * @param start inclusive
     * @param end exclusive
     * @return 
     */
    public List<Card> getRange(int start, int end) {
        if(start >= DECK_SIZE)
            throw new IllegalArgumentException("Starting index " + start + " >= deck size");
        if(end > DECK_SIZE)
            throw new IllegalArgumentException("Ending index " + end + " > deck size");
        if(start > end)
            throw new IllegalArgumentException(String.format("Starting index %s > ending index %s", start, end));
        return Arrays.asList(Arrays.copyOfRange(cards, start, end));
    }
    
    public List<Card> toList() {
        //Arrays.asList doesn't create a copy; guard against external editing of internal array
        return new ArrayList<>(Arrays.asList(cards));
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + Arrays.deepHashCode(this.cards);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }
        if(obj == null) {
            return false;
        }
        if(getClass() != obj.getClass()) {
            return false;
        }
        final Deck other = (Deck) obj;
        if(!Arrays.deepEquals(this.cards, other.cards)) {
            return false;
        }
        return true;
    }
    
    public void sort(Comparator<? super Card> comparator) {
        Arrays.sort(cards, comparator);
    }

    public void shuffle(RNG seed, Comparator<? super Card> deckOrderBeforeDuel) {
        /*
        Quoted from GenericMadScientist in the FM discord:
        
        If this is the player's deck, first sort it according to the sort 
        selected for the deck before entering the duel.
         */
        Arrays.sort(cards, deckOrderBeforeDuel);
        shuffle(seed);
    }

    /**
     * Shuffles the deck, with its current order as the starting point. Use this
     * shuffle method for AI decks.
     *
     * @param seed
     */
    public void shuffle(RNG seed) {
        //the FM shuffle algorithm:
        for(int i = 0; i < 160; i++) {
            int x = seed.rand() % 40;
            int y = seed.rand() % 40;
            Card holder = cards[x];
            cards[x] = cards[y];
            cards[y] = holder;
        }
    }
    
    public Set<RNG> findPossibleSeeds(Comparator<? super Card> initialState, List<Card> drawnCards) {
        return findPossibleSeeds(initialState, drawnCards, new RNG());
    }
    
    public Set<RNG> findPossibleSeeds(Comparator<? super Card> initialState, List<Card> drawnCards, RNG baseSeed) {
        if(drawnCards.size() > DECK_SIZE)
            throw new IllegalArgumentException("Cards drawn exceeds deck size: " + drawnCards.size());
        //make a copy of the deck and order it to the initial state
        Deck startingDeck = new Deck(this);
        startingDeck.sort(initialState);
        //copy seed to prevent side effects
        RNG seedCopy = new RNG(baseSeed);
        Set<RNG> validSeeds = Collections.synchronizedSet(new HashSet<>());
        IntStream.range(0, SEARCH_SPACE).mapToObj(i -> {
            seedCopy.rand();
            return new RNG(seedCopy);
        }).parallel().forEach(seed -> {
            /*
            Quoted from GenericMadScientist in the FM discord:
            
            ORDER OF EVENTS:
            Shuffle player deck, generate AI deck, shuffle AI deck.
            */
            Deck testDeck = new Deck(startingDeck);
            testDeck.shuffle(new RNG(seed));
            if(testDeck.startsWith(drawnCards))
                validSeeds.add(new RNG(seed));
        });
        return validSeeds;
    }
    
    public boolean startsWith(List<Card> cards) {
        if(cards.size() > DECK_SIZE)
            throw new IllegalArgumentException("List of cards cannot exceed deck size: " + cards.size());
        for(int i = 0, n = cards.size(); i < n; i++) {
            if(!cards.get(i).equals(this.cards[i]))
                return false;
        }
        return true;
    }

    /**
     *
     * @param duelist
     * @param seed
     * @return an unshuffled deck
     */
    public static Deck createDuelistDeck(Duelist duelist, RNG seed) {
        Deck deck = new Deck();
        Pool pool = duelist.getPool(Pool.Type.DECK);
        int i = 0;
        while(i < DECK_SIZE) {
            Card dropped = pool.getDrop(seed);
            //decks are limited to 3 copies of a card
            if(Arrays.stream(deck.cards).filter(Objects::nonNull).filter(c -> c.equals(dropped)).count() < 3L) {
                deck.cards[i++] = dropped;
            }
        }
        return deck;
    }
    
    public static List<Comparator<Card>> getAllSorts() {
        return new ArrayList<>(SORTS);
    }
    
    @Override
    public String toString() {
        return "[ " + Arrays.stream(cards).map(Card::getName).collect(Collectors.joining(", ")) + " ]";
    }
}
