package io.kotgres.orm.exceptions.query

import io.kotgres.orm.exceptions.base.KotgresException

class KotgresNoColumnsReturnedException :
    KotgresException("Query should return only one column, but returned 0.\nA correct query would be something like SELECT <column> FROM users LIMIT 1")
