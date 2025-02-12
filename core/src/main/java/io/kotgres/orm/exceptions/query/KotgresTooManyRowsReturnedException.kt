package io.kotgres.orm.exceptions.query

import io.kotgres.orm.exceptions.base.KotgresException

class KotgresTooManyRowsReturnedException :
    KotgresException("Query should return only one row, but returned more.\nA correct query would be something like SELECT <column> FROM users LIMIT 1")
