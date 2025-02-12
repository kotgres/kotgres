CREATE TABLE table_with_real
(
    id   int GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    real real
);

CREATE TABLE table_with_bool
(
    id   int GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    bool bool
);

CREATE TABLE table_with_timestamp
(
    id   int GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    date timestamp
);

CREATE TABLE table_with_timestamptz
(
    id   int GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    date timestamptz
);

CREATE TABLE table_with_float
(
    id    int GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    float float
);


CREATE TABLE table_with_double
(
    id     int GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    double double precision
);

CREATE TABLE table_with_bigint
(
    id     int GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    long bigint
);

CREATE TABLE table_with_decimal
(
    id      int GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    decimal decimal
);

CREATE TABLE table_with_numeric
(
    id      int GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    numeric numeric
);
