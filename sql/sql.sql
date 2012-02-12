PRAGMA foreign_keys=ON;

DROP TABLE IF EXISTS settings;

CREATE TABLE settings (
	id INTEGER PRIMARY KEY,
	name TEXT UNIQUE NOT NULL,
	value NONE NOT NULL
);

DROP TABLE IF EXISTS data;

CREATE TABLE data (
	id INTEGER PRIMARY KEY,
	parent_id INTEGER REFERENCES data (id) ON DELETE CASCADE,
	name TEXT NOT NULL,
	text_content TEXT,
	date_created INTEGER NOT NULL,
	date_modified INTEGER NOT NULL,
	type INTEGER NOT NULL
);


INSERT INTO settings(name, value) VALUES ('password_sha1_hash', '"DA39A3EE5E6B4B0D3255BFEF95601890AFD80709');
INSERT INTO data (id, parent_id, name, date_created, date_modified, type) VALUES (0, -1, 'ROOT', 0, 0, 0);