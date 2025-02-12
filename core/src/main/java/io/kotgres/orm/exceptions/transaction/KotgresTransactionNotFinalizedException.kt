package io.kotgres.orm.exceptions.transaction

import io.kotgres.orm.exceptions.base.KotgresException

class KotgresTransactionNotFinalizedException :
    KotgresException("Transaction has not been finalized before the end of the useTransaction block.\nMake sure to use commit() or rollback() before the block. If you don't want to finalize the transaction straight away, you can use Dao.getNewTransaction()")
