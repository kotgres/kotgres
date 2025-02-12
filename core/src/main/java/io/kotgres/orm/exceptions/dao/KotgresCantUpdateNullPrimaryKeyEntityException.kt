package io.kotgres.orm.exceptions.dao

import io.kotgres.orm.exceptions.base.KotgresException

class KotgresCantUpdateNullPrimaryKeyEntityException :
    KotgresException("Entity has null for its primary key. Cannot update those kind of entities, make sure it has a non-null value.")
