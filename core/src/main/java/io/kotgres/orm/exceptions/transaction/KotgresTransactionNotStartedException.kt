package io.kotgres.orm.exceptions.transaction

import io.kotgres.orm.exceptions.base.KotgresException

class KotgresTransactionNotStartedException :
    KotgresException("You can only run this method after starting a transaction. Use Dao.getNewTransaction()")
