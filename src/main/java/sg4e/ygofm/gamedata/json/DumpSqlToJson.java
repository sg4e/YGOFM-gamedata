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
package sg4e.ygofm.gamedata.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlite3.SQLitePlugin;
import sg4e.ygofm.gamedata.Card;

/**
 *
 * @author sg4e
 */
public class DumpSqlToJson {
    
    private static final String SQLITE_PATH = "fm-sqlite3.db";
    private static final String RAW_PREFIX = "json" + File.separator + "raw" + File.separator;
    
    public static void main(String[] args) throws IOException {
        ObjectMapper json = new ObjectMapper();
        json.findAndRegisterModules();
        json.enable(SerializationFeature.INDENT_OUTPUT);
        
        Jdbi jdbi = Jdbi.create("jdbc:sqlite:" + SQLITE_PATH).installPlugin(new SQLitePlugin());
        Handle handle = jdbi.open();
        
        List<CardJson> cards = handle.createQuery("SELECT * FROM cardinfo")
                .map((rs, ctx) -> new CardJson(rs.getInt("CardId"), rs.getInt("Level"), rs.getInt("Attack"), rs.getInt("Defense"), rs.getInt("StarchipCost"), rs.getString("CardName"), rs.getString("Description"), rs.getString("GuardianStar1"), rs.getString("GuardianStar2"), rs.getString("Type"), rs.getString("Attribute"), rs.getString("Password")))
                .list();
        json.writeValue(new File(RAW_PREFIX + "cards.json"), cards);
        
        List<DuelistJson> duelists = handle.createQuery("SELECT * FROM duelistinfo")
                .map((rs, ctx) -> new DuelistJson(rs.getInt("DuelistId"), rs.getInt("HandSize"), rs.getString("Duelist")))
                .list();
        json.writeValue(new File(RAW_PREFIX + "duelists.json"), duelists);
        
        List<DropPoolJson> dropPools;
        Map<Integer,DuelistJson> idToDuelist = duelists.stream().collect(Collectors.toMap(DuelistJson::getId, Function.identity()));
        Map<Integer,CardJson> idToCard = cards.stream().collect(Collectors.toMap(CardJson::getId, Function.identity()));
        dropPools = handle.createQuery("SELECT * FROM droppool")
                .map((rs, ctx) -> {
                    try {
                        return new DropPoolJson(rs.getInt("PoolId"), rs.getInt("Duelist"), rs.getInt("CardId"), rs.getInt("CardProbability"), json.readValue("\"" + rs.getString("PoolType") + "\"", PoolType.class));
                    }
                    catch(JsonProcessingException ex) {
                        ex.printStackTrace();
                    }
                    return null;
                })
                .list();
        json.writeValue(new File(RAW_PREFIX + "droppools.json"), dropPools);
        
        List<EquipJson> equips = handle.createQuery("SELECT * FROM equipinfo")
                .map((rs, ctx) -> new EquipJson(rs.getInt("EquipId"), rs.getInt("CardId")))
                .list();
        json.writeValue(new File(RAW_PREFIX + "equips.json"), equips);
        
        List<FusionJson> fusions = handle.createQuery("SELECT * FROM fusions")
                .map((rs, ctx) -> new FusionJson(rs.getInt("Material1"), rs.getInt("Material2"), rs.getInt("Result")))
                .list();
        json.writeValue(new File(RAW_PREFIX + "fusions.json"), fusions);
        
        List<RitualJson> rituals = handle.createQuery("SELECT * FROM ritualinfo")
                .map((rs, ctx) -> new RitualJson(idToCard.get(rs.getInt("RitualCardId")).getName(), idToCard.get(rs.getInt("Material1")).getName(), idToCard.get(rs.getInt("Material2")).getName(), idToCard.get(rs.getInt("Material3")).getName(), idToCard.get(rs.getInt("Result")).getName()))
                .list();
        json.writeValue(new File(RAW_PREFIX + "rituals.json"), rituals);
        
        handle.close();
        
        Card.loadCards();
        System.out.println(Card.CARD_DB);
    }
    
}
