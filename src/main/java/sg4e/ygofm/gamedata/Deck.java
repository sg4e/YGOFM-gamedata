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

import java.util.Arrays;
import java.util.Comparator;

/**
 *
 * @author sg4e
 */
public class Deck {
    private final Card[] cards;
    private int drawIndex;
    
    public static final int DECK_SIZE = 40;
    public static final Comparator<Card> CARD_ID_ORDER = (c1, c2) -> c1.getId() - c2.getId();
    
    public Deck() {
        cards = new Card[DECK_SIZE];
        drawIndex = 0;
    }
    
    public Card draw() {
        return cards[drawIndex++];
    }
    
    public int getDrawIndex() {
        return drawIndex;
    }
    
    public void resetDrawIndex(int newIndex) {
        if(newIndex < 0 || newIndex >= DECK_SIZE) {
            throw new IllegalArgumentException("Invalid drawIndex: " + newIndex);
        }
        drawIndex = newIndex;
    }
    
    public void shuffle(RNG seed, Comparator<? super Card> deckOrderBeforeDuel) {
        /*
        Quoted from GenericMadScientist in the FM discord:
        
        If this is the player's deck, first sort it according to the sort 
        selected for the deck before entering the duel.
        */
        Arrays.sort(cards, deckOrderBeforeDuel);
        //the FM shuffle algorithm:
        for (int i = 0; i < 160; i++) {
            int x = seed.rand() % 40;
            int y = seed.rand() % 40;
            Card holder = cards[x];
            cards[x] = cards[y];
            cards[y] = holder;
        }
    }
    
    public void shuffle(RNG seed) {
        shuffle(seed, CARD_ID_ORDER);
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
        for(int i = 0; i < DECK_SIZE; i++) {
            deck.cards[i] = pool.getDrop(seed);
        }
        return deck;
    }
}
