import sqlite3
import json


def dump_table_to_json(cursor, table_name):
    cursor.execute(f"SELECT * FROM {table_name}")
    rows = cursor.fetchall()
    columns = [col[0][0].lower() + col[0][1:] for col in cursor.description]
    data = [dict(zip(columns, row)) for row in rows]
    with open(f"{table_name}.json", "w") as json_file:
        json.dump(data, json_file, indent=4)


def dump_all_tables_to_json(database_path):
    conn = sqlite3.connect(database_path)
    cursor = conn.cursor()
    cursor.execute("SELECT name FROM sqlite_master WHERE type='table';")
    tables = [table[0] for table in cursor.fetchall()]
    for table_name in tables:
        dump_table_to_json(cursor, table_name)
    conn.close()


if __name__ == "__main__":
    database_path = "fm-sqlite3.db"
    dump_all_tables_to_json(database_path)
