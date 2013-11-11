
CREATE TABLE users (
	id char(64) NOT NULL PRIMARY KEY
);

CREATE SEQUENCE artworks_id_seq;

CREATE TABLE artworks (
	id integer NOT NULL PRIMARY KEY default nextval('artworks_id_seq'),
	user_id char(64) NOT NULL,
        pid varchar(10),
	url varchar(512) NOT NULL,
	inspiration_url varchar(512) NOT NULL,
        config varchar(5000),
	created timestamp DEFAULT 'now()'
);

ALTER SEQUENCE artworks_id_seq owned by artworks.id;
