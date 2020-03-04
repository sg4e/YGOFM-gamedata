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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import sg4e.ygofm.gamedata.json.EquipJson;

/**
 *
 * @author sg4e
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Card {
    private int id, starchips;
    private String name, description, type, password;
    
    private static final String DEFAULT_CARD_JSON_LOCATION = "json/raw/cards.json";
    private static final String DEFAULT_EQUIP_JSON_LOCATION = "json/raw/equips.json";
    public static final Map<Integer,Card> CARD_DB = new HashMap<>();
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
    
    Card() {
        
    }
    
    public static void loadCards() {
        synchronized(CARD_DB) {
            try {
                if(CARD_DB.isEmpty()) {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode cardArray = mapper.readTree(new File(DEFAULT_CARD_JSON_LOCATION));
                    cardArray.elements().forEachRemaining(c -> {
                        try {
                            Card card;
                            if(NON_MONSTER_TYPES.contains(c.get("type").asText()))
                                card = mapper.readValue(c.traverse(), Card.class);
                            else
                                card = mapper.readValue(c.traverse(), MonsterCard.class);
                            CARD_DB.put(card.getId(), card);
                            }
                        catch(Throwable thr) {
                            failLoadCards(thr);
                        }
                    });
                    //load equip data
                    EquipJson[] equipArray = mapper.readValue(new File(DEFAULT_EQUIP_JSON_LOCATION), EquipJson[].class);
                    Arrays.stream(equipArray).forEach(equipData -> {
                        ((MonsterCard)getCardById(equipData.getEquipper())).addEquip(getCardById(equipData.getEquip()));
                    });
                }
            }
            catch(Throwable thr) {
                failLoadCards(thr);
            }
        }
    }
    
    public static Card getCardById(int id) {
        if(CARD_DB.isEmpty())
            loadCards();
        return CARD_DB.get(id);
    }
    
    private static void failLoadCards(Throwable thr) {
        thr.printStackTrace();
        CARD_DB.clear();
    }
}
