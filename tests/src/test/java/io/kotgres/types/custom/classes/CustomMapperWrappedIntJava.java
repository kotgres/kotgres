package io.kotgres.types.custom.classes;

import io.kotgres.orm.types.base.CustomMapper;

import java.util.Collections;
import java.util.List;

public class CustomMapperWrappedIntJava extends CustomMapper<WrappedInt> {

    public CustomMapperWrappedIntJava() {
        super(WrappedInt.class);
    }

    @Override
    public WrappedInt fromSql(String string) {
        return new WrappedInt(Integer.parseInt(string));
    }

    @Override
    public String toSql(WrappedInt value) {
        return String.valueOf(value.getInt());
    }

    @Override
    public List<String> getPostgresTypes() {
        return Collections.singletonList("integer");
    }
}
