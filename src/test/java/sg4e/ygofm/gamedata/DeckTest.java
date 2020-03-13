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

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 *
 * @author sg4e
 */
public class DeckTest {
    
    FMDB db;
    final int seed1 = -1060881141;
    final int seed2 = 1086602507;
    
    Deck playersDeck, aisDeck;
    
    
    public DeckTest() {
    }
    
    @BeforeEach
    public void init() {
        db = new FMDB.Builder().build();
        //get a dump memory dump of a shuffled deck from the game:
        try(Stream<String> stream = Files.lines(Paths.get(getClass().getResource("/FM-memory-dump.txt").toURI()))) {
            List<Card> allCards = stream.map(Integer::parseInt).map(db::getCard).collect(Collectors.toList());
            playersDeck = new Deck(allCards.subList(0, Deck.DECK_SIZE));
            aisDeck = new Deck(allCards.subList(Deck.DECK_SIZE, Deck.DECK_SIZE * 2));
        }
        catch(Exception ex) {
            fail(ex);
        }
    }
    
    @AfterEach
    public void tearDown() {
        db.close();
    }

    @ParameterizedTest
    @ValueSource(ints = { seed1, seed2 })
    public void testShuffle(int seed) {
        RNG realSeed = new RNG(seed);
        Deck regeneratedPlayersDeck = new Deck(playersDeck);
        regeneratedPlayersDeck.shuffle(realSeed, Deck.CARD_ID_ORDER);
        Deck generatedAiDeck = Deck.createDuelistDeck(db.getDuelist(Duelist.Name.HEISHIN_1), realSeed);
        generatedAiDeck.shuffle(realSeed);
        assertEquals(playersDeck, regeneratedPlayersDeck);
        assertEquals(aisDeck, generatedAiDeck);
    }
    
    @Test
    public void testSeeds() {
        RNG rng = new RNG();
        int seed = 0;
        for(int i = 0; i < Deck.SEARCH_SPACE; i++) {
            rng.rand();
            seed = rng.getSeed();
            if(seed == seed1 || seed == seed2)
                break;
        }
        if(seed != seed1 && seed != seed2)
            fail();
    }
    
    @Test
    public void testFindPossibleSeeds() {
        //these data were taken from a deck that was sorted by card id before entering the duel,
        //so reconstruct starting state, then determine RNG seed with first 3/4 of deck
        List<Card> firstThreeQuarters = playersDeck.getRange(0, Deck.DECK_SIZE * 3 / 4);
        Set<RNG> seeds = playersDeck.findPossibleSeeds(Deck.CARD_ID_ORDER, firstThreeQuarters);
        //now, take the seed and verify it was used to shuffle the player's deck and generate and shuffle ai's deck
        assertEquals(1, seeds.size());
        RNG realSeed = seeds.stream().findFirst().get();
        testShuffle(realSeed.getSeed());
    }
    
}
