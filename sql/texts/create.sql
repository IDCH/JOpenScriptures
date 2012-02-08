-- Defines works that are recorded in a local database
CREATE TABLE IF NOT EXISTS texts_works (
    work_id             SERIAL  PRIMARY KEY,
    uuid                CHAR(38) NOT NULL UNIQUE,
    
    title               VARCHAR(255),
    abbreviation        VARCHAR(32),
    description         TEXT,
    
    creator             VARCHAR(255),
    publisher           VARCHAR(255),
    language            VARCHAR(3),
    work_type           VARCHAR(16),
    copyright           TEXT,
    scope               VARCHAR(255),
    ref_system          VARCHAR(16),
    soruce_url          VARCHAR(255),
    
    publication_date    CHAR(10),
    import_date         DATETIME
    
) ENGINE=InnoDB CHARSET utf8;

-- Defines the tokens for works stored in this database 
CREATE TABLE IF NOT EXISTS texts_tokens (
    token_id       SERIAL  PRIMARY KEY,
    uuid           CHAR(38) NOT NULL UNIQUE,
    work_id        BIGINT UNSIGNED NOT NULL,
    
    token_pos      INTEGER,
    token_text     VARCHAR(255) NOT NULL,
    token_type     ENUM('WORD', 'WHITESPACE', 'PUNCTUATION'), 
    
    UNIQUE (work_id, token_pos),
    
    FOREIGN KEY (work_id)
      REFERENCES texts_works (work_id)
      ON DELETE CASCADE
) ENGINE=InnoDB CHARSET utf8;

CREATE TABLE IF NOT EXISTS texts_structures (
    structure_id        SERIAL  PRIMARY KEY,
    uuid                CHAR(38) NOT NULL UNIQUE,
    work_uuid           CHAR(38) NOT NULL,
    
    structure_name      VARCHAR(255),
    perspective         VARCHAR(255),
    
    start_pos           INTEGER,
    end_pos             INTEGER
    
) ENGINE=InnoDB CHARSET utf8;

-- Attributes for a structure
CREATE TABLE IF NOT EXISTS texts_structureattributes (
    structure_id   BIGINT UNSIGNED NOT NULL,
    attr_key       VARCHAR(255),
    attr_value     TEXT,
    
    UNIQUE (structure_id, attr_key),
    
    FOREIGN KEY (structure_id)
      REFERENCES texts_structures (structure_id)
      ON DELETE CASCADE
    
) ENGINE=InnoDB CHARSET utf8;