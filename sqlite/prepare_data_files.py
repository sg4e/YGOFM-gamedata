#
# The MIT License (MIT)
# Copyright (c) 2024 sg4e
#
# Permission is hereby granted, free of charge, to any person obtaining a copy of this software
# and associated documentation files (the "Software"), to deal in the Software without restriction,
# including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
# and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
# subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all copies or substantial
# portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
# TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
# THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
# TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
#
import sqlite3
import json


class_names = {
    "cardinfo": "Card",
    "droppool": "RawDropPool",
    "equipinfo": "RawEquip",
    "fusions": "RawFusion",
    "ritualinfo": "RawRitual"
}


guardian_stars = {
    '"Sun"': "GuardianStar.SUN",
    '"Moon"': "GuardianStar.MOON",
    '"Mars"': "GuardianStar.MARS",
    '"Mercury"': "GuardianStar.MERCURY",
    '"Jupiter"': "GuardianStar.JUPITER",
    '"Venus"': "GuardianStar.VENUS",
    '"Saturn"': "GuardianStar.SATURN",
    '"Uranus"': "GuardianStar.URANUS",
    '"Neptune"': "GuardianStar.NEPTUNE",
    '"Pluto"': "GuardianStar.PLUTO"
}

duel_ranks = {
    '"Deck"': "Pool.Type.DECK",
    '"SAPow"': "Pool.Type.SA_POW",
    '"BCD"': "Pool.Type.BCD",
    '"SATec"': "Pool.Type.SA_TEC",
}


def dump_all_tables(database_path):
    conn = sqlite3.connect(database_path)
    cursor = conn.cursor()
    cursor.execute("SELECT name FROM sqlite_master WHERE type='table';")
    tables = [table[0] for table in cursor.fetchall()]
    with open("java/RawDatabase.java", "w") as file:
        file.write("package moe.maika.ygofm.gamedata;\n\n")
        file.write("class RawDatabase ")
        file.write("{\n")
        for table_name in tables:
            method_extension = 1
            cursor.execute(f"SELECT * FROM {table_name}")
            rows = cursor.fetchall()
            columns = [col[0][0].lower() + col[0][1:] for col in cursor.description]
            data = [dict(zip(columns, row)) for row in rows]
            with open(f"json/{table_name}.json", "w") as json_file:
                json.dump(data, json_file, indent=4)
            if table_name in class_names:
                file.write(f"    public static {class_names[table_name]}[] get{class_names[table_name]}()")
                file.write("{\n")
                file.write(f"        {class_names[table_name]}[] arr = new {class_names[table_name]}[{len(rows)}];\n")
                #  Infer type based on other values in the same column
                type_count = {}
                for index in range(len(rows)):
                    row = rows[index]
                    for i in range(len(row)):
                        if i not in type_count:
                            type_count[i] = {}
                        type_count[i][type(row[i])] = type_count[i].get(type(row[i]), 0) + 1
                    line = f"        arr[{index}] = new {class_names[table_name]}("
                    constructor_args = ['"' + repr(value)[1:][:-1] + '"' if isinstance(value, str) else str(value) for value in row]
                    for i in range(len(row)):
                        if constructor_args[i] == "None":
                            if columns[i].startswith("guardianStar"):
                                constructor_args[i] = "null"
                            else:
                                int_count = type_count[i][int] if int in type_count[i] else 0
                                str_count = type_count[i][str] if str in type_count[i] else 0
                                constructor_args[i] = "0" if int_count > str_count else '""'
                    # substitute Guardian Star enums
                    constructor_args = [guardian_stars[arg] if arg in guardian_stars else arg for arg in constructor_args]
                    constructor_args = [arg[0] + arg[1:-1].replace('"', '\\"') + arg[-1] if arg.startswith('"') else arg for arg in constructor_args]
                    constructor_args = [duel_ranks[arg] if arg in duel_ranks else arg for arg in constructor_args]
                    if table_name == "equipinfo" or table_name == "fusions":
                        constructor_args = constructor_args[1:]  # First column is an unused ID
                    line += (", ".join(constructor_args) + ")")
                    file.write(f"{line};\n")
                    if index != 0 and index % 1000 == 0:
                        file.write(f"        return get{class_names[table_name]}{method_extension}(arr);\n")
                        file.write("    }\n")
                        file.write(f"    private static {class_names[table_name]}[] get{class_names[table_name]}{method_extension}({class_names[table_name]}[] arr) ")
                        file.write("{\n")
                        method_extension += 1
                file.write("        return arr;\n    }\n")
        file.write("}\n")
    conn.close()


if __name__ == "__main__":
    database_path = "fm-sqlite3.db"
    dump_all_tables(database_path)
