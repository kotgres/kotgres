package io.kotgres.orm.exceptions.transaction

import io.kotgres.orm.exceptions.base.KotgresException

class KotgresTransactionFinalizedException :
    KotgresException("Transaction is already commited or rolled back. Use the method getNewTransaction() to start a new one")
