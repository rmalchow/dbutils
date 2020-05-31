package de.disk0.dbutil.impl.mysql.entities;

import de.disk0.dbutil.impl.AbstractGuidRepository;

import javax.sql.DataSource;

public class BigDecimalEntityRepository extends AbstractGuidRepository<BigDecimalEntity> {

    public BigDecimalEntityRepository(DataSource dataSource) {
        super(dataSource);
    }

}
